package ch.qligier.schematronix.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link ValidationReport}.
 *
 * @author Quentin Ligier
 */
class ValidationReportTest {

    /**
     * Ensures the default constructor is working.
     */
    @Test
    @DisplayName("Empty constructor")
    void testEmptyConstructor() {
        final ValidationReport report = new ValidationReport();
        assertTrue(report.isValid());
        assertEquals(0, report.getFailedAsserts().size());
        assertEquals(0, report.getSuccessfulReports().size());
    }

    /**
     * Ensures the validity status is working.
     */
    @Test
    @DisplayName("Validity status")
    void testValidityStatus() {
        ValidationReport report = new ValidationReport();
        report.addFailedAssert(new TriggeredAssertion(
            "ruleId", "patternId", "role", "ruleContext", "test"
        ));
        assertFalse(report.isValid());
        assertEquals(1, report.getFailedAsserts().size());
        assertEquals(0, report.getSuccessfulReports().size());

        report = new ValidationReport();
        report.addSuccessfulReport(new TriggeredAssertion(
            "ruleId", "patternId", "role", "ruleContext", "test"
        ));
        assertTrue(report.isValid());
        assertEquals(0, report.getFailedAsserts().size());
        assertEquals(1, report.getSuccessfulReports().size());
    }
}
