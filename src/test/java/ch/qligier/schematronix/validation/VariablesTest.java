package ch.qligier.schematronix.validation;

import ch.qligier.schematronix.SchematronixValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A test bed for {@link SchematronixValidator}. It uses the resources of the 'variables' folder.
 * <p>
 * The expected behavior is as follows: when an assertion is evaluated (true or false???) on a node, this node can still be evaluated by
 * latter assertions of the same rule but must be excluded from latter rules of the same pattern. A new pattern resets this exclude list.
 * <p>
 * The reference Schematron validation report 'target.html' is generated with the script 'validate_schematron.bat'.
 *
 * @author Quentin Ligier
 */
public class VariablesTest {

    private final static String RES_DIR = "schematronix/validator/variables/";

    @Test
    @DisplayName("Ensures that variables are correctly processed")
    void testVariables() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "schematronix.sch")).getFile());
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target.xml")).getFile());

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), definitionFile);
        ValidationReport report = validator.validate(ValidatorConfiguration.fullValidation());

        assertFalse(report.isValid());
        assertEquals(List.of(
            new TriggeredAssertion("rule1", "pattern1", "", "Seconds shall be between 0 and 59, found '63'", "//times/time", "$seconds >= 0 and $seconds <= 59"),
            new TriggeredAssertion("rule1", "pattern1", "", "Hours shall be between 0 and 23, found '24'", "//times/time", "$hours >= 0 and $hours <= 23")
        ), report.getFailedAsserts());
    }
}
