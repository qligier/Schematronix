package ch.qligier.schematronix.definition;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
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
 * A report made about the context nodes.
 *
 * @author Quentin Ligier
 * @since 0.3.0
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class SchematronReport extends SchematronRuleChild implements Cloneable {

    /**
     * The report's role, or {@code null} if it's not specified.
     */
    private String role;

    /**
     * The report's test as an XPath expression.
     */
    private String test;

    /**
     * The URI [IRI] of external information of interest to maintainers and users of the schema.
     */
    private String see;

    /**
     * The list of {@link Node}s (text or tag) that makes the report's detail message.
     */
    private List<Node> messageNodes;

    /**
     * Clones the current object.
     *
     * @return the cloned object.
     */
    public SchematronReport clone() {
        return new SchematronReport(role, test, see, messageNodes);
    }

    /**
     * Constructs a new instance from the parsed Schematron element.
     *
     * @param reportElement The element parsed from a {@code report} tag.
     * @return a new instance of {@link SchematronReport} initialized from the parsed element.
     * @throws SchematronParsingException if the given element is not a valid {@code report} tag.
     */
    static SchematronReport fromReportElement(@NonNull final Element reportElement) throws SchematronParsingException {
        if (!SchematronConstants.REPORT_TAG_NAME.equalsIgnoreCase(reportElement.getTagName())) {
            throw new SchematronParsingException("The given node is not an 'report' element");
        }
        if (!reportElement.hasAttribute("test")) {
            throw new SchematronParsingException("Missing attribute 'test' in 'report' element");
        }

        final String role;
        if (reportElement.hasAttribute("role")) {
            role = reportElement.getAttribute("role");
        } else {
            role = null;
        }

        final List<Node> messageNodes = new ArrayList<>();
        final NodeList nodes = reportElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            messageNodes.add(nodes.item(i));
        }

        return new SchematronReport(
            role,
            reportElement.getAttribute("test"),
            reportElement.getAttribute("see"),
            messageNodes
        );
    }
}
