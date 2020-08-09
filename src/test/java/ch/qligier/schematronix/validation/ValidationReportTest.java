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
    }

    /**
     * Ensures the quick constructor is working.
     */
    @Test
    @DisplayName("Quick constructor")
    void testQuickConstructor() {
        final ValidationReport report = new ValidationReport("Something was wrong");
        assertFalse(report.isValid());
        assertEquals(1, report.getFailedAsserts().size());
    }
}
