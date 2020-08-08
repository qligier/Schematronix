package ch.qligier.schematronix.models;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.schematron.SchematronConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * An assertion made about the context nodes.
 *
 * @author Quentin Ligier
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class SchematronAssert extends SchematronRuleChild implements Cloneable {

    /**
     * The assert's role.
     */
    private SchematronAssertRole role;

    /**
     * The assert's test as an XPath expression.
     */
    private String test;

    /**
     *
     */
    private String see;

    /**
     * The list of {@link Node}s (text or tag) that makes the assert's detail message.
     */
    private List<Node> messageNodes;

    /**
     * Clones the current object.
     *
     * @return the cloned object.
     */
    public SchematronAssert clone() {
        return new SchematronAssert(role, test, see, messageNodes);
    }

    /**
     * Constructs a new instance from the parsed Schematron element.
     *
     * @param assertElement The element parsed from a {@code assert} tag.
     * @return a new instance of {@link SchematronAssert} initialized from the parsed element.
     * @throws SchematronParsingException if the given element is not a valid {@code assert} tag.
     */
    static SchematronAssert fromAssertElement(@NonNull final Element assertElement) throws SchematronParsingException {
        if (!SchematronConstants.ASSERT_TAG_NAME.equalsIgnoreCase(assertElement.getTagName())) {
            throw new SchematronParsingException("The given node is not an 'assert' element");
        }
        if (!assertElement.hasAttribute("test")) {
            throw new SchematronParsingException("Missing attribute 'test' in 'assert' element");
        }

        final SchematronAssertRole role;
        if (assertElement.hasAttribute("role")) {
            role = SchematronAssertRole.getByValue(assertElement.getAttribute("role"));
        } else {
            role = SchematronAssertRole.ERROR;
        }

        final List<Node> messageNodes = new ArrayList<>();
        final NodeList nodes = assertElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            messageNodes.add(nodes.item(i));
        }

        return new SchematronAssert(
            role,
            assertElement.getAttribute("test"),
            assertElement.getAttribute("see"),
            messageNodes
        );
    }
}
