package ch.qligier.schematronix.validation;

import lombok.Data;

/**
 * The configuration model for the Schematronix validator.
 *
 * @author Quentin Ligier
 */
@Data
public class ValidatorConfiguration {

    /**
     * If {@code true}, the validation is stopped at the first encountered error; if {@code false}, the validation is fully performed. The
     * validation is faster if this option is enabled but the report may be incomplete if multiple errors lie in the checked file.
     */
    final boolean failFast;

    /**
     * Whether to evaluate the {@code <value-of>} nodes in assertion messages ({@code true}) or not ({@code false}). The validation is
     * faster if this option is disabled but the report is less detailed.
     */
    final boolean evaluateMessageNodes;

    /**
     * A pre-defined, fast validation configuration.
     */
    public static ValidatorConfiguration fastValidation() {
        return new ValidatorConfiguration(true, false);
    }

    /**
     * A pre-defined, full validation configuration.
     */
    public static ValidatorConfiguration fullValidation() {
        return new ValidatorConfiguration(false, true);
    }
}
