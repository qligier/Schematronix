package ch.qligier.schematronix.converter;

import ch.qligier.schematronix.models.*;
import ch.qligier.schematronix.schematron.SchematronConstants;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A specialized class that can write a Schematronix file from a Schematron definition.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
@SuppressWarnings("squid:S1905")
public class SchematronixWriter {

    /**
     * The Schematronix document builder.
     */
    private final DocumentBuilder documentBuilder;

    /**
     * The transformer of {@link Document} to a stream of the rendered XML content.
     */
    private final Transformer xmlTransformer;

    /**
     * The Schematronix XPath transformer.
     */
    private final XPathTransformer xPathTransformer;

    /**
     * Constructor.
     *
     * @throws ParserConfigurationException      if the implementation is not available or cannot be instantiated.
     * @throws TransformerConfigurationException if the implementation is not available or cannot be instantiated.
     */
    public SchematronixWriter() throws ParserConfigurationException, TransformerConfigurationException {
        this.documentBuilder = newSafeDocumentBuilder();

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        this.xmlTransformer = transformerFactory.newTransformer();
        this.xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        this.xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        this.xPathTransformer = new XPathTransformer();
    }

    /**
     * Writes a Schematron definition as a specialized Schematronix files. Transformations occur to conform to the Schematronix
     * specificity.
     *
     * @param definition      The original Schematron definition
     * @param destinationFile The Schematronix file that will be written.
     * @throws TransformerException if an unrecoverable error occurs during the course of the XML rendering.
     */
    public void writeSchematronix(@NonNull final SchematronDefinition definition,
                                  @NonNull final File destinationFile) throws TransformerException {
        if ((destinationFile.exists() && !Files.isWritable(destinationFile.toPath()))
            || !Files.isWritable(destinationFile.getParentFile().toPath())) {
            throw new IllegalArgumentException("The destination file is not writable");
        }

        final Document document = this.documentBuilder.newDocument();
        final Element rootElement = document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.ROOT_TAG_NAME);
        rootElement.setAttribute("queryBinding", definition.getQueryBinding());
        document.appendChild(rootElement);

        // Add the title if it was defined
        if (definition.getTitle() != null) {
            final Element titleElement =
                document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.TITLE_TAG_NAME);
            titleElement.setTextContent(definition.getTitle());
            rootElement.appendChild(titleElement);
        }

        // Add namespaces, they don't need any transformation
        for (final Map.Entry<String, String> namespace : definition.getNamespaces().entrySet()) {
            final Element namespaceElement =
                document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.NAMESPACE_TAG_NAME);
            namespaceElement.setAttribute("prefix", namespace.getKey());
            namespaceElement.setAttribute("uri", namespace.getValue());
            rootElement.appendChild(namespaceElement);
        }

        // Add patterns and rules
        for (final String patternId : definition.getEnabledPatterns()) {
            final Element patternElement =
                document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.PATTERN_TAG_NAME);
            patternElement.setAttribute("id", patternId);

            for (final SchematronRule rule : getSchematronixRulesForPattern(definition, patternId)) {
                final Element ruleElement =
                    document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.RULE_TAG_NAME);
                ruleElement.setAttribute("id", rule.getId());
                ruleElement.setAttribute("context", rule.getContext());

                for (final SchematronRuleChild child : rule.getChildren()) {
                    if (child instanceof SchematronAssert) {
                        final Element assertElement =
                            document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.ASSERT_TAG_NAME);
                        assertElement.setAttribute("test", ((SchematronAssert) child).getTest());
                        ruleElement.appendChild(assertElement);
                    } else if (child instanceof SchematronLet) {
                        final Element letElement =
                            document.createElementNS(SchematronConstants.SCHEMATRON_NAMESPACE, SchematronConstants.LET_TAG_NAME);
                        letElement.setAttribute("name", ((SchematronLet) child).getName());
                        letElement.setAttribute("value", ((SchematronLet) child).getValue());
                        ruleElement.appendChild(letElement);
                    }
                }

                // Only add the rule if it's not empty
                if (ruleElement.getChildNodes().getLength() > 0) {
                    patternElement.appendChild(ruleElement);
                }
            }

            // Only add the pattern if it's not empty
            if (patternElement.getChildNodes().getLength() > 0) {
                rootElement.appendChild(patternElement);
            }
        }

        // Write result to file
        this.xmlTransformer.transform(new DOMSource(document), new StreamResult(destinationFile));
    }

    /**
     * Returns the Schematronix rules that are contained in a pattern.
     *
     * @param definition The original Schematron definition.
     * @param patternId  The pattern ID.
     * @return the list of Schematronix rules.
     */
    private List<SchematronRule> getSchematronixRulesForPattern(@NonNull final SchematronDefinition definition,
                                                                @NonNull final String patternId) {
        if (!definition.getRulesPerPattern().containsKey(patternId)) {
            throw new IllegalArgumentException("The pattern cannot be found in the definition");
        }

        // List of rules to process and return
        return definition.getRulesPerPattern().get(patternId).stream()
            .map(ruleId -> definition.getDefinedRules().get(ruleId))
            .filter(rule -> !rule.isAbstract())
            .map(rule -> convertRuleToSchematronix(definition, rule))
            .collect(Collectors.toList());
    }

    /**
     * Transforms a regular rule to a Schematronix rule by resolving extends, transforming XPath expressions and removing non-error
     * assertions.
     *
     * @param definition The original Schematron definition.
     * @param rule       The rule to transform to a Schematronix one.
     * @return the transformed rule.
     */
    private SchematronRule convertRuleToSchematronix(@NonNull final SchematronDefinition definition,
                                                     @NonNull final SchematronRule rule) {
        final SchematronRule schematronixRule = rule.clone();

        // Remove children that are 'extends' elements and replace them by their own list of children
        schematronixRule.setChildren(resolveExtendedChildren(definition, rule));

        // Transform the XPath expression of assertions and variables
        schematronixRule.setContext(this.xPathTransformer.transform(schematronixRule.getContext()));

        // Remove assertions that are not errors
        schematronixRule.getChildren()
            .removeIf((SchematronRuleChild ruleChild) -> ruleChild instanceof SchematronAssert && ((SchematronAssert) ruleChild).getRole() != SchematronAssertRole.ERROR);

        return schematronixRule;
    }

    /**
     * Resolves the list of children of an extended rule.
     *
     * @param definition The original Schematron definition.
     * @param rule       The extended rule.
     * @return the list of children of the parent rule and all extended rules.
     */
    private List<SchematronRuleChild> resolveExtendedChildren(@NonNull final SchematronDefinition definition,
                                                              @NonNull final SchematronRule rule) {
        final List<SchematronRuleChild> children = rule.getChildren();
        if (children.stream().anyMatch(child -> child instanceof SchematronExtends)) {
            final int index = Iterables.indexOf(children, child -> child instanceof SchematronExtends);
            final SchematronExtends extend = (SchematronExtends) children.get(index);
            children.remove(index);
            final List<SchematronRuleChild> newChildren =
                resolveExtendedChildren(definition, definition.getDefinedRules().get(extend.getExtendsRuleId()));
            Lists.reverse(newChildren)
                .forEach(newChild -> children.add(index, newChild));
        }
        return children;
    }

    /**
     * Initializes and configures a {@link DocumentBuilder} that is not vulnerable to XXE injections (XInclude, Billions Laugh Attack, ...).
     *
     * @return a configured DocumentBuilder.
     * @throws ParserConfigurationException if the parser is not Xerces2 compatible.
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j">XML External Entity Prevention Cheat Sheet</a>
     */
    private static DocumentBuilder newSafeDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://apache.org/xml/features/xinclude", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        return factory.newDocumentBuilder();
    }
}
