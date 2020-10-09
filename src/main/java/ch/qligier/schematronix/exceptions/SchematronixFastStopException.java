package ch.qligier.schematronix.exceptions;

/**
 * An exception thrown when the Schematron validation of a file fails the first assert. It informs the validator that it shall stop
 * processing.
 *
 * @author Quentin Ligier
 */
public class SchematronixFastStopException extends Exception {

    /**
     * Constructs a new Schematronix fast stop exception.
     */
    public SchematronixFastStopException() {
        super();
    }
}
