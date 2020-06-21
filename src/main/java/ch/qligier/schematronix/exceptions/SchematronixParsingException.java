package ch.qligier.schematronix.exceptions;

import lombok.NonNull;

/**
 * An exception thrown when the parsing of a Schematronix file fails.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
public class SchematronixParsingException extends Exception {

    /**
     * Constructs a new Schematronix parsing exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public SchematronixParsingException(@NonNull final String message) {
        super(message);
    }
}
