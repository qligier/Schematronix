package ch.qligier.schematronix.models;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.w3c.dom.Element;

/**
 * The extends element allows reference to the contents of other declarations.
 *
 * @author Quentin Ligier
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class SchematronExtends extends SchematronRuleChild implements Cloneable {

    /**
     * The ID of the extended rule.
     */
    private String extendsRuleId;

    /**
     * Clones the current object.
     *
     * @return the cloned object.
     */
    public SchematronExtends clone() {
        return new SchematronExtends(extendsRuleId);
    }

    /**
     * Constructs a new instance from the parsed Schematron element.
     *
     * @param extendsElement The element parsed from a {@code extends} tag.
     * @return a new instance of {@link SchematronExtends} initialized from the parsed element.
     * @throws SchematronParsingException if the given element is not a valid {@code extends} tag.
     */
    static SchematronExtends fromExtendsElement(@NonNull final Element extendsElement) throws SchematronParsingException {
        if (!SchematronConstants.EXTENDS_TAG_NAME.equalsIgnoreCase(extendsElement.getTagName())) {
            throw new SchematronParsingException("This is not an 'extends' element");
        }
        if (!extendsElement.hasAttribute("rule")) {
            throw new SchematronParsingException("Missing attribute 'rule' in 'extends' element");
        }

        return new SchematronExtends(extendsElement.getAttribute("rule"));
    }
}
