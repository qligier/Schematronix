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
public class ValidationReport {

    /**
     * The list of failed assert messages.
     */
    private final List<String> failedAsserts = new ArrayList<>();

    /**
     * The list of successful report messages.
     */
    private final List<String> successfulReports = new ArrayList<>();

    /**
     * No args constructor.
     */
    public ValidationReport() {
    }

    /**
     * Quick constructor for creating a report with a single failed assert message (e.g. when validating a document in fail fast mode).
     *
     * @param failedAssert The failed assert message.
     */
    public ValidationReport(@NonNull final String failedAssert) {
        this.failedAsserts.add(failedAssert);
    }

    /**
     * Returns whether the document has passed validation. A document passes the validation if there is no failed assert, whatever its
     * role is.
     *
     * @return {@code true} if the document passed validation, {@code false} otherwise.
     */
    public boolean isValid() {
        return this.failedAsserts.isEmpty();
    }
}
