package ch.qligier.schematronix.validator;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The model containing the Schematronix validation results of an XML file.
 *
 * @author Quentin Ligier
 */
@Data
@SuppressWarnings("WeakerAccess")
public class ValidationReport {

    /**
     * The list of issue messages.
     */
    private final List<String> messages;

    /**
     * Constructor.
     */
    public ValidationReport() {
        this.messages = new ArrayList<>();
    }

    /**
     * Quick constructor for creating a report with a single message (e.g. when validating a document in fail fast mode).
     *
     * @param message The issue message.
     */
    public ValidationReport(@NonNull final String message) {
        this.messages = new ArrayList<>();
        this.messages.add(message);
    }

    /**
     * Returns whether the document has passed validation or not.
     *
     * @return {@code true} if the document passed validation, {@code false} otherwise.
     */
    public boolean isValid() {
        return this.messages.isEmpty();
    }
}
