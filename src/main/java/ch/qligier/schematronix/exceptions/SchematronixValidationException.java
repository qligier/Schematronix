package ch.qligier.schematronix.exceptions;

import lombok.NonNull;

/**
 * An exception thrown when the Schematron validation of a file fails.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
public class SchematronixValidationException extends Exception {

    /**
     * Constructs a new Schematron validation exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public SchematronixValidationException(@NonNull final String message) {
        super(message);
    }
}

