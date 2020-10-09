package ch.qligier.schematronix;

import ch.qligier.schematronix.validation.TriggeredAssertion;
import ch.qligier.schematronix.validation.ValidationReport;
import ch.qligier.schematronix.validation.ValidatorConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bed for {@link SchematronixValidator}. It uses the resources of the 'sample1' folder.
 * <p>
 * The expected behavior is as follows: when an assertion is evaluated (true or false???) on a node, this node can still be evaluated by
 * latter assertions of the same rule but must be excluded from latter rules of the same pattern. A new pattern resets this exclude list.
 * <p>
 * The reference Schematron validation report 'target1.html' is generated with the script 'validate_schematron.bat'.
 *
 * @author Quentin Ligier
 */
class SchematronixValidatorSample1Test {

    private final static String RES_DIR = "schematronix/validator/sample1/";

    @Test
    @DisplayName("Nodes should be evaluated in one rule of a pattern")
    void testNodeExclusionPerPattern() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "schematronix1.sch")).getFile());
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target1.xml")).getFile());

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), definitionFile);
        ValidationReport report = validator.validate(ValidatorConfiguration.fullValidation());

        assertFalse(report.isValid());
        assertEquals(6, report.getFailedAsserts().size());
        assertEquals(
            List.of(
                new TriggeredAssertion("rule1", "pattern1", "error", "pattern1 rule1 attr1", "//book", "@attr1"),
                new TriggeredAssertion("rule1", "pattern1", "error", "pattern1 rule1 attr1", "//book", "@attr1"),
                new TriggeredAssertion("rule1", "pattern1", "error", "pattern1 rule1 attr2", "//book", "@attr2"),
                new TriggeredAssertion("rule4", "pattern2", "error", "pattern2 rule4 attr1", "//*[@id]", "@attr1"),
                new TriggeredAssertion("rule4", "pattern2", "error", "pattern2 rule4 attr1", "//*[@id]", "@attr1"),
                new TriggeredAssertion("rule4", "pattern2", "error", "pattern2 rule4 attr2", "//*[@id]", "@attr2")

            ),
            report.getFailedAsserts()
        );

        report = validator.validate(ValidatorConfiguration.fastValidation());
        assertFalse(report.isValid());
        assertEquals(1, report.getFailedAsserts().size());
        assertEquals("rule1", report.getFailedAsserts().get(0).getRuleId());
        assertEquals("pattern1", report.getFailedAsserts().get(0).getPatternId());
        assertEquals("@attr1", report.getFailedAsserts().get(0).getTest());
        assertEquals("//book", report.getFailedAsserts().get(0).getRuleContext());
        assertEquals("error", report.getFailedAsserts().get(0).getRole());
    }
}
