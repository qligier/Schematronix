package ch.qligier.schematronix.validation;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The model that details the trigger of an assertion (assert or report elements).
 *
 * @author Quentin Ligier
 * @since 0.4.0
 */
@Data
@AllArgsConstructor
public class TriggeredAssertion {

    /**
     * The rule id, if any.
     */
    private String ruleId;

    /**
     * The id of the pattern the rule belongs to, if any.
     */
    private String patternId;

    /**
     * The assertion role.
     */
    private String role;

    /**
     * The assertion message.
     */
    private String message;

    /**
     * The rule context node location.
     */
    private String location;

    /**
     * The rule context, as an XPath expression.
     */
    private String ruleContext;

    /**
     * The test that failed, as an XPath expression.
     */
    private String test;
}
