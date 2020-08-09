package ch.qligier.schematronix.models;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.w3c.dom.Element;

/**
 * A declaration of a named variable.
 *
 * @author Quentin Ligier
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class SchematronLet extends SchematronRuleChild implements Cloneable {

    /**
     * The name of the variable.
     */
    private String name;

    /**
     * The value (expression) of the variable.
     */
    private String value;

    /**
     * Clones the current object.
     *
     * @return the cloned object.
     */
    public SchematronLet clone() {
        return new SchematronLet(name, value);
    }

    /**
     * Constructs a new instance from the parsed Schematron element.
     *
     * @param letElement The element parsed from a {@code let} tag.
     * @return a new instance of {@link SchematronLet} initialized from the parsed element.
     * @throws SchematronParsingException if the given element is not a valid {@code let} tag.
     */
    static SchematronLet fromLetElement(@NonNull final Element letElement) throws SchematronParsingException {
        if (!SchematronConstants.LET_TAG_NAME.equalsIgnoreCase(letElement.getTagName())) {
            throw new SchematronParsingException("The given node is not a 'let' element");
        }
        if (!letElement.hasAttribute("name") || !letElement.hasAttribute("value")) {
            throw new SchematronParsingException("A 'let' element is missing its 'name' and/or 'value' attributes");
        }

        return new SchematronLet(
            letElement.getAttribute("name"),
            letElement.getAttribute("value")
        );
    }
}
