package ch.qligier.schematronix;

import ch.qligier.schematronix.exceptions.SchematronixFastStopException;
import ch.qligier.schematronix.exceptions.SchematronixParsingException;
import ch.qligier.schematronix.exceptions.SchematronixValidationException;
import ch.qligier.schematronix.definition.SchematronConstants;
import ch.qligier.schematronix.validation.ValidationReport;
import ch.qligier.schematronix.validation.ValidationRule;
import ch.qligier.schematronix.validation.ValidatorConfiguration;
import lombok.NonNull;
import lombok.extern.java.Log;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main class of the Schematronix validator.
 * <p>
 * A Schematronix file is a valid Schematron file with additional requirements:
 * <ul>
 * <li>The Schematronix file shall not contain <code>phase</code> or <code>include</code> elements;
 * <li>Namespaces shall be declared before other elements (exceptions are otherwise to be expected);
 * <li>Patterns shall not be abstract;
 * <li>Patterns shall have an id;
 * <li>Rules shall only appears in a pattern;
 * <li>Rules shall not be abstract;
 * <li>Rules shall contain only assertions and variables, no reports or extend;
 * <li>Assertions should not have roles, they all are treated as errors.
 * </ul>
 *
 * @author Quentin Ligier
 */
@Log
public class SchematronixValidator {

    /**
     * The XPath processor.
     */
    private final Processor processor;

    /**
     * The XPath compiler.
     */
    private final XPathCompiler xpathCompiler;

    /**
     * The root element of the target element.
     */
    private final XdmNode domRoot;

    /**
     * The current pattern ID or {@code null} if we are outside a pattern.
     */
    private String currentPatternId = null;

    /**
     * A list of the hashes of already evaluated nodes in the current pattern. Once a node has been evaluated by a rule, it shall not be
     * evaluated by another rule of the same pattern.
     */
    private final List<Integer> assertedNodes = new ArrayList<>();

    /**
     * The current validation rule or {@code null} if we are outside a rule.
     */
    private ValidationRule currentRule = null;

    /**
     * The list of defined namespaces. The key is the namespace prefix, the value is the namespace URI.
     */
    private final Map<String, String> namespaces = new HashMap<>();

    /**
     * The StAX cursor reader factory.
     */
    private final XMLInputFactory schematronixReaderFactory;

    /**
     * The path of the Schematronix file parent directory.
     */
    private final URI schematronixDirectoryPath;

    /**
     * The Schematron file source.
     */
    private final Source schematronFileSource;

    /**
     * Constructs a new Schematronix validator.
     *
     * @param xmlFileSource  The XML file to be validated (target).
     * @param schematronFile The Schematronix definition file.
     * @throws SaxonApiException if the XPath processor fails to build the XML document of the target file.
     */
    public SchematronixValidator(@NonNull final Source xmlFileSource,
                                 @NonNull final File schematronFile) throws SaxonApiException {
        this.processor = getProcessor();
        this.xpathCompiler = this.processor.newXPathCompiler();
        this.schematronixDirectoryPath = schematronFile.getParentFile().toURI();
        this.domRoot = this.processor.newDocumentBuilder().build(xmlFileSource);
        this.xpathCompiler.setBaseURI(this.schematronixDirectoryPath);
        this.schematronixReaderFactory = getXmlInputFactory();
        this.schematronFileSource = new StreamSource(schematronFile);
    }

    /**
     * Constructs a new Schematronix validator.
     *
     * @param xmlFileSource        The XML file to be validated (target).
     * @param schematronFileSource The Schematronix definition file.
     * @throws SaxonApiException if the XPath processor fails to build the XML document of the target file.
     */
    public SchematronixValidator(@NonNull final Source xmlFileSource,
                                 @NonNull final Source schematronFileSource) throws SaxonApiException {
        this.processor = getProcessor();
        this.xpathCompiler = this.processor.newXPathCompiler();
        this.schematronixDirectoryPath = null;
        this.domRoot = this.processor.newDocumentBuilder().build(xmlFileSource);
        this.schematronixReaderFactory = getXmlInputFactory();
        this.schematronFileSource = schematronFileSource;
    }

    /**
     * Validates the target file with the provided Schematronix definition and returns a report that contains the eventual validation
     * errors.
     *
     * @param configuration The validator configuration to use.
     * @return the validation report containing the error messages, if any.
     * @throws SaxonApiException               if there is an error while compiling/executing an XPath expression.
     * @throws SchematronixParsingException    if there is an error with the Schematronix file.
     * @throws SchematronixValidationException if the validation has encountered an exception.
     * @throws XMLStreamException              if StAX fails to create a reader for the Schematronix file.
     */
    @NonNull
    public ValidationReport validate(@NonNull final ValidatorConfiguration configuration) throws SaxonApiException, SchematronixParsingException, XMLStreamException, SchematronixValidationException {
        final ValidationReport report = new ValidationReport();
        final XMLEventReader schematronixReader = this.schematronixReaderFactory.createXMLEventReader(this.schematronFileSource);

        while (schematronixReader.hasNext()) {
            final XMLEvent event;
            try {
                event = schematronixReader.nextEvent();
            } catch (final XMLStreamException exception) {
                schematronixReader.close();
                throw new SchematronixParsingException("Error while parsing the Schematronix file: " + exception.getMessage());
            }

            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                switch (startElement.getName().getLocalPart()) {
                    case SchematronConstants.NAMESPACE_TAG_NAME:
                        this.addNamespace(
                            getAttributeValue(startElement, "prefix"),
                            getAttributeValue(startElement, "uri")
                        );
                        break;
                    case SchematronConstants.PATTERN_TAG_NAME:
                        this.startPattern(getAttributeValue(startElement, "id"));
                        break;
                    case SchematronConstants.RULE_TAG_NAME:
                        this.createRule(
                            getAttributeValue(startElement, "context"),
                            getAttributeValue(startElement, "id"),
                            configuration
                        );
                        break;
                    case SchematronConstants.ASSERT_TAG_NAME:
                        this.addAssertToRule(
                            getAttributeValue(startElement, "test"),
                            getAttributeValue(startElement, "role"),
                            this.parseAssertionMessageNodes(SchematronConstants.ASSERT_TAG_NAME, schematronixReader)
                        );
                        break;
                    case SchematronConstants.REPORT_TAG_NAME:
                        this.addReportToRule(
                            getAttributeValue(startElement, "test"),
                            getAttributeValue(startElement, "role"),
                            this.parseAssertionMessageNodes(SchematronConstants.REPORT_TAG_NAME, schematronixReader)
                        );
                        break;
                    case SchematronConstants.LET_TAG_NAME:
                        this.addVariableToRule(
                            getAttributeValue(startElement, "name"),
                            getAttributeValue(startElement, "value")
                        );
                        break;
                    case SchematronConstants.ROOT_TAG_NAME:
                    case SchematronConstants.TITLE_TAG_NAME:
                    case SchematronConstants.VALUE_OF_TAG_NAME:
                        // Allowed elements that are ignored
                        break;
                    default:
                        schematronixReader.close();
                        throw new SchematronixParsingException("Unknown '" + startElement.getName().getLocalPart() + "' element");
                }
            } else if (event.isEndElement()) { //NOSONAR, we don't care about other cases
                final EndElement endElement = event.asEndElement();
                switch (endElement.getName().getLocalPart()) {
                    case SchematronConstants.PATTERN_TAG_NAME:
                        this.closePattern();
                        break;
                    case SchematronConstants.RULE_TAG_NAME:
                        try {
                            this.currentRule.execute(report, configuration); //NOSONAR, the parser ensures that a rule was opened before that
                        } catch (final SchematronixFastStopException exception) {
                            // A SchematronixValidationException is thrown at the first error in fast failing mode
                            return report;
                        }
                        this.currentRule = null;
                        break;
                    default:
                        break;
                }
            }
        }

        schematronixReader.close();
        return report;
    }

    /**
     * Parses the content of an assertion tag ({@code <assert>} or {@code <report>}).
     *
     * @param assertionTagName   The local name of the assertion tag (either 'assert' or 'report').
     * @param schematronixReader The StAX reader for the Schematronix file.
     * @return the list of parsed nodes.
     * @throws XMLStreamException           if StAX fails to read a part of the Schematronix file.
     * @throws SchematronixParsingException if the {@code <value-of>} is missing its 'select' attribute.
     */
    private List<ValidationRule.MessageNode> parseAssertionMessageNodes(@NonNull final String assertionTagName,
                                                                        @NonNull final XMLEventReader schematronixReader) throws XMLStreamException, SchematronixParsingException {
        final List<ValidationRule.MessageNode> messageNodes = new ArrayList<>();
        // Process the content of the assert
        while (true) {
            final XMLEvent innerEvent = schematronixReader.nextEvent();
            if (innerEvent.isEndElement() && ((EndElement) innerEvent).getName().getLocalPart().equals(assertionTagName)) {
                break; // Exit the inner event loop, the assert has bee closed
            } else if (innerEvent.isCharacters()) {
                messageNodes.add(new ValidationRule.MessageTextNode(((Characters) innerEvent).getData()));
            } else if (innerEvent.isStartElement() && ((StartElement) innerEvent).getName().getLocalPart().equals(SchematronConstants.VALUE_OF_TAG_NAME)) {
                final Attribute select = ((StartElement) innerEvent).getAttributeByName(new QName("select"));
                if (select == null) {
                    throw new SchematronixParsingException("A 'value-of' element is missing its 'select' attribute");
                }
                messageNodes.add(new ValidationRule.MessageValueOfNode(select.getValue()));
            } else if (innerEvent.isStartElement() && ((StartElement) innerEvent).getName().getLocalPart().equals(SchematronConstants.VALUE_NAME_NAME)) {
                messageNodes.add(new ValidationRule.MessageNameNode());
            }
        }
        return messageNodes;
    }

    /**
     * Defines a new namespace.
     *
     * @param prefix The namespace prefix.
     * @param uri    The namespace URI.
     * @throws SchematronixParsingException if the namespace {@code prefix} or {@code uri} is {@code null}.
     */
    private void addNamespace(final String prefix,
                              final String uri) throws SchematronixParsingException {
        if (prefix == null) {
            throw new SchematronixParsingException("The namespace prefix is mandatory");
        }
        if (uri == null) {
            throw new SchematronixParsingException("The namespace URI is mandatory");
        }
        this.namespaces.put(prefix, uri);
        this.xpathCompiler.declareNamespace(prefix, uri);
    }

    /**
     * Starts a pattern.
     *
     * @param patternId The pattern ID.
     * @throws SchematronixParsingException if the {@code patternId} is {@code null}.
     */
    private void startPattern(final String patternId) throws SchematronixParsingException {
        if (patternId == null) {
            throw new SchematronixParsingException("A 'pattern' element is missing its 'id' attribute");
        }
        if (this.currentPatternId != null) {
            throw new SchematronixParsingException("A 'pattern' element appears inside another pattern");
        }

        this.currentPatternId = patternId;
        this.assertedNodes.clear();
    }

    /**
     * Closes the current pattern.
     */
    private void closePattern() {
        this.currentPatternId = null;
        this.assertedNodes.clear();
    }

    /**
     * Creates a new validation rule.
     *
     * @param ruleContextXpath The XPath expression of the context.
     * @param ruleId           The rule ID or {@code null} if it's not defined.
     * @param configuration    The validator configuration to use.
     * @throws SaxonApiException            if an error is encountered when compiling an XPath expression.
     * @throws SchematronixParsingException if the rule {@code context} or {@code pattern} is {@code null}.
     */
    private void createRule(final String ruleContextXpath,
                            final String ruleId,
                            @NonNull final ValidatorConfiguration configuration) throws SaxonApiException, SchematronixParsingException {
        if (ruleContextXpath == null) {
            throw new SchematronixParsingException("A 'rule' element is missing its 'context' attribute");
        }
        if (this.currentPatternId == null) {
            throw new SchematronixParsingException("A 'rule' element appears outside a pattern");
        }

        final XdmValue ruleContext = this.xpathCompiler.evaluate(ruleContextXpath, this.domRoot);
        this.currentRule = new ValidationRule(
            this.processor,
            this.namespaces,
            ruleContext,
            this.schematronixDirectoryPath,
            this.assertedNodes,
            configuration,
            ruleId,
            this.currentPatternId,
            ruleContextXpath
        );
    }

    /**
     * Adds an assertion to the current validation rule.
     *
     * @param test         The assertion test as an XPath expression.
     * @param role         The assertion role.
     * @param messageNodes The assertion message as a list of nodes.
     * @throws SaxonApiException            if an error is encountered when compiling an XPath expression.
     * @throws SchematronixParsingException if the assert appears outside a rule or is missing its {@code test}.
     */
    private void addAssertToRule(final String test,
                                 String role,
                                 @NonNull final List<ValidationRule.MessageNode> messageNodes) throws SaxonApiException, SchematronixParsingException {
        if (this.currentRule == null) {
            throw new SchematronixParsingException("An 'assert' element appears outside a 'rule' element");
        }
        if (test == null) {
            throw new SchematronixParsingException("An 'assert' element is missing its 'test' attribute");
        }
        if (role == null) {
            role = "";
        }

        this.currentRule.addAssert(test, role, messageNodes);
    }

    /**
     * Adds a report to the current validation rule.
     *
     * @param test         The report test as an XPath expression.
     * @param role         The report role.
     * @param messageNodes The report message as a list of nodes.
     * @throws SaxonApiException            if an error is encountered when compiling an XPath expression.
     * @throws SchematronixParsingException if the assert appears outside a rule or is missing its {@code test}.
     */
    private void addReportToRule(final String test,
                                 String role,
                                 @NonNull final List<ValidationRule.MessageNode> messageNodes) throws SaxonApiException, SchematronixParsingException {
        if (this.currentRule == null) {
            throw new SchematronixParsingException("A 'report' element appears outside a 'rule' element");
        }
        if (test == null) {
            throw new SchematronixParsingException("A 'report' element is missing its 'test' attribute");
        }
        if (role == null) {
            role = "";
        }

        this.currentRule.addReport(test, role, messageNodes);
    }

    /**
     * Adds a variable to the current validation rule.
     *
     * @param name  The variable name.
     * @param value The variable value as an XPath expression.
     * @throws SaxonApiException            if an error is encountered when compiling an XPath expression.
     * @throws SchematronixParsingException if the variable appears outside a rule or is missing its {@code name} or {@code value}.
     */
    private void addVariableToRule(final String name,
                                   final String value) throws SaxonApiException, SchematronixParsingException {
        if (this.currentRule == null) {
            throw new SchematronixParsingException("A 'let' element appears outside a 'rule' element");
        }
        if (name == null) {
            throw new SchematronixParsingException("A 'let' element is missing its 'name' attribute");
        }
        if (value == null) {
            throw new SchematronixParsingException("A 'let' element is missing its 'value' attribute");
        }

        this.currentRule.addVariable(name, value);
    }

    /**
     * Returns the attribute value of a node opening tag.
     *
     * @param element       The opening tag.
     * @param attributeName The attribute name.
     * @return the attribute value or {@code null} if the attribute is not declared.
     */
    private static String getAttributeValue(@NonNull final StartElement element,
                                            @NonNull final String attributeName) {
        final Attribute attribute = element.getAttributeByName(new javax.xml.namespace.QName(attributeName));
        return (attribute != null) ? attribute.getValue() : null;
    }

    /**
     * Creates and configures a new XML input factory against XXE injections.
     *
     * @return a safe {@link XMLInputFactory}.
     */
    private static XMLInputFactory getXmlInputFactory() {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newDefaultFactory();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return xmlInputFactory;
    }

    /**
     * Creates and configures a Saxon processor against XXE injections.
     *
     * @return a safe {@link Processor}.
     * @see <a href="https://www.saxonica.com/documentation/index.html#!configuration/config-features">Configuration Features</a>
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity
     * Prevention Cheat Sheet</a>
     */
    @SuppressWarnings("deprecation")
    private static Processor getProcessor() {
        final Processor processor = new Processor(false);
        processor.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        processor.setConfigurationProperty(Feature.DTD_VALIDATION, false);
        processor.setConfigurationProperty(Feature.XINCLUDE, false);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + "http://apache.org/xml/features/disallow-doctype-decl", true);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + "http://xml.org/sax/features/external-general-entities", false);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + "http://xml.org/sax/features/external-parameter-entities", false);
        processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return processor;
    }
}
