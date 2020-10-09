package ch.qligier.schematronix.definition;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A list of assertions tested within the context specified by the required context attribute. The context attribute specifies the rule
 * context expression.
 *
 * @author Quentin Ligier
 */
@Data
@AllArgsConstructor
@Log
public class SchematronRule implements Cloneable {

    /**
     * The parent pattern ID if any, or {@code null}.
     */
    private String pattern;

    /**
     * The rule ID. If it isn't set in the definition, the parser will generate one.
     */
    @NonNull
    private String id;

    /**
     * The rule context if it's not abstract, or {@code null}.
     */
    private String context;

    /**
     * The list of the rule children (assertions, reports, extends, variables). The order in which the children are defined is kept.
     */
    @NonNull
    private List<SchematronRuleChild> children = new ArrayList<>();

    /**
     * Whether the rule is abstract. An abstract rule shall not have a context attribute but shall have an ID.
     */
    private boolean isAbstract = false;

    /**
     * Returns whether this rule extends another one or not.
     *
     * @return {@code true} if this rule extends another one, {@code false} otherwise.
     */
    public boolean hasExtends() {
        return this.children.stream().anyMatch(child -> child instanceof SchematronExtends);
    }

    /**
     * Returns the 'extends' element if it exists.
     *
     * @return an optional that may contain the {@link SchematronExtends} element if any.
     */
    public Optional<SchematronExtends> getExtends() {
        return this.children.stream()
            .filter(child -> child instanceof SchematronExtends)
            .map(extendChild -> (SchematronExtends) extendChild)
            .findAny();
    }

    /**
     * Constructs a new instance from the parsed Schematron element.
     *
     * @param ruleElement The element parsed from a {@code rule} tag.
     * @return a new instance of {@link SchematronRule} initialized from the parsed element.
     * @throws SchematronParsingException if the Schematron file is invalid.
     */
    public static SchematronRule fromRuleElement(@NonNull final Element ruleElement) throws SchematronParsingException {
        if (!SchematronConstants.RULE_TAG_NAME.equalsIgnoreCase(ruleElement.getTagName())) {
            throw new SchematronParsingException("The given node is not a 'rule' element");
        }

        boolean isAbstract;
        if (ruleElement.hasAttribute("abstract") && "true".equalsIgnoreCase(ruleElement.getAttribute("abstract"))) {
            isAbstract = true;
        } else if (ruleElement.hasAttribute("abstract") && "false".equalsIgnoreCase(ruleElement.getAttribute("abstract"))) {
            isAbstract = false;
        } else if (ruleElement.hasAttribute("abstract")) {
            throw new SchematronParsingException("The 'abstract' attribute of the 'rule' element shall be either true or false");
        } else {
            // Default value
            isAbstract = false;
        }
        if (isAbstract && ruleElement.hasAttribute("context")) {
            throw new SchematronParsingException("An abstract 'rule' element shall not have a context");
        }
        if (!isAbstract && !ruleElement.hasAttribute("context")) {
            throw new SchematronParsingException("A non-abstract 'rule' element shall have a context");
        }
        if (isAbstract && !ruleElement.hasAttribute("id")) {
            throw new SchematronParsingException("An abstract 'rule' element shall have an ID");
        }

        final List<SchematronRuleChild> children = new ArrayList<>();
        final NodeList nodes = ruleElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            final Element element = (Element) nodes.item(i);
            switch (element.getNodeName()) {
                case SchematronConstants.EXTENDS_TAG_NAME:
                    children.add(SchematronExtends.fromExtendsElement(element));
                    break;
                case SchematronConstants.LET_TAG_NAME:
                    children.add(SchematronLet.fromLetElement(element));
                    break;
                case SchematronConstants.ASSERT_TAG_NAME:
                    children.add(SchematronAssert.fromAssertElement(element));
                    break;
                case SchematronConstants.REPORT_TAG_NAME:
                    children.add(SchematronReport.fromReportElement(element));
                    break;
                default:
                    break;
            }
        }

        final String id;
        if (ruleElement.getAttribute("id").isEmpty()) {
            id = UUID.randomUUID().toString();
        } else {
            id = ruleElement.getAttribute("id");
        }

        return new SchematronRule(
            null,
            id,
            ruleElement.hasAttribute("context") ? ruleElement.getAttribute("context") : null,
            children,
            isAbstract
        );
    }

    /**
     * Constructs a new instance from the parsed Schematron element with a specified pattern ID.
     *
     * @param ruleElement The element parsed from a {@code rule} tag.
     * @param patternId   The pattern ID.
     * @return a new instance of {@link SchematronRule} initialized from the parsed element.
     * @throws SchematronParsingException if the Schematron file is invalid.
     */
    public static SchematronRule fromRuleElement(@NonNull final Element ruleElement,
                                                 final String patternId) throws SchematronParsingException {
        final SchematronRule rule = SchematronRule.fromRuleElement(ruleElement);
        rule.setPattern(patternId);
        return rule;
    }

    /**
     * Clones the current object.
     *
     * @return the deep-cloned object.
     */
    public SchematronRule clone() {
        final SchematronRule clonedRule;
        try {
            clonedRule = (SchematronRule) super.clone();
        } catch (final CloneNotSupportedException e) {
            // This should never occur
            log.severe("SchematronRule does not implement Cloneable");
            return null;
        }
        if (this.children != null) {
            clonedRule.setChildren(this.children.stream().map(SchematronRuleChild::clone).collect(Collectors.toList()));
        }
        return clonedRule;
    }
}
