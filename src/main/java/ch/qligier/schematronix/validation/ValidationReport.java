package ch.qligier.schematronix.validation;

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
    private final List<TriggeredAssertion> failedAsserts = new ArrayList<>();

    /**
     * The list of successful report messages.
     */
    private final List<TriggeredAssertion> successfulReports = new ArrayList<>();

    /**
     * No args constructor.
     */
    public ValidationReport() {
    }

    /**
     * Adds a failed assert to the validation report.
     *
     * @param failedAssert The failed assert to add.
     */
    public void addFailedAssert(@NonNull final TriggeredAssertion failedAssert) {
        this.failedAsserts.add(failedAssert);
    }

    /**
     * Adds a successful report to the validation report.
     *
     * @param successfulReport The successful report to add.
     */
    public void addSuccessfulReport(@NonNull final TriggeredAssertion successfulReport) {
        this.successfulReports.add(successfulReport);
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
