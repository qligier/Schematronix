package ch.qligier.schematronix.models;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.schematron.SchematronConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.w3c.dom.Element;

/**
 * A pattern is a set of rules giving constraints that are in some way related.
 *
 * @author Quentin Ligier
 * @since 0.2.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Log
public class SchematronPattern {

    /**
     * The pattern ID. If it isn't set in the definition, the parser will generate one.
     */
    @NonNull
    private String id;

    /**
     * Whether the pattern is abstract.
     */
    private boolean isAbstract = false;

    /**
     * The pattern title or {@code null} if it isn't set.
     */
    private String title;

    /**
     * Constructs a new instance from the parsed Schematron element.
     *
     * @param patternElement The element parsed from a {@code pattern} tag.
     * @return a new instance of {@link SchematronPattern} initialized from the parsed element.
     * @throws SchematronParsingException if the given element is not a valid {@code pattern} tag.
     */
    public static SchematronPattern fromPatternElement(@NonNull final Element patternElement) throws SchematronParsingException {
        if (!SchematronConstants.PATTERN_TAG_NAME.equalsIgnoreCase(patternElement.getTagName())) {
            throw new SchematronParsingException("The given node is not an 'pattern' element");
        }

        boolean isAbstract;
        if (patternElement.hasAttribute("abstract") && "true".equalsIgnoreCase(patternElement.getAttribute("abstract"))) {
            isAbstract = true;
        } else if (patternElement.hasAttribute("abstract") && "false".equalsIgnoreCase(patternElement.getAttribute("abstract"))) {
            isAbstract = false;
        } else if (patternElement.hasAttribute("abstract")) {
            throw new SchematronParsingException("The 'abstract' attribute of the 'pattern' element shall be either true or false");
        } else {
            // Default value
            isAbstract = false;
        }

        return new SchematronPattern(
            patternElement.getAttribute("id"),
            isAbstract,
            patternElement.getAttribute("title")
        );
    }

    /**
     * Clones the current object.
     *
     * @return the deep-cloned object.
     */
    public SchematronPattern clone() {
        final SchematronPattern clonedPattern;
        try {
            clonedPattern = (SchematronPattern) super.clone();
        } catch (final CloneNotSupportedException e) {
            // This should never occur
            log.severe("SchematronPattern does not implement Cloneable");
            return null;
        }
        clonedPattern.setId(this.getId());
        clonedPattern.setAbstract(this.isAbstract());
        clonedPattern.setTitle(this.getTitle());
        return clonedPattern;
    }
}
