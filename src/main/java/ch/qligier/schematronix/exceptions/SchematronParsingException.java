package ch.qligier.schematronix.exceptions;

import lombok.NonNull;

/**
 * An exception thrown when the parsing of a Schematron file fails.
 *
 * @author Quentin Ligier
 */
public class SchematronParsingException extends Exception {

    /**
     * Constructs a new Schematron parsing exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public SchematronParsingException(@NonNull final String message) {
        super(message);
    }
}
