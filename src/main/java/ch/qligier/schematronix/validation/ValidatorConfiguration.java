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
     * If {@code true}, the validation is stopped at the first encountered error; if {@code false}, the validation is fully performed.
     */
    final boolean failFast;

    /**
     * A pre-defined, fast validation configuration.
     */
    public static ValidatorConfiguration fastValidation() {
        return new ValidatorConfiguration(true);
    }

    /**
     * A pre-defined, full validation configuration.
     */
    public static ValidatorConfiguration fullValidation() {
        return new ValidatorConfiguration(false);
    }
}
