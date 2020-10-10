package ch.qligier.schematronix.schematronixValidator;

import ch.qligier.schematronix.SchematronixValidator;
import ch.qligier.schematronix.validation.TriggeredAssertion;
import ch.qligier.schematronix.validation.ValidationReport;
import ch.qligier.schematronix.validation.ValidatorConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A test bed for namespace support. It uses the resources of the 'namespaces' folder.
 * <p>
 * The reference Schematron validation report 'target.html' is generated with the script 'validate_schematron.bat'.
 *
 * @author Quentin Ligier
 */
public class NamespacesTest {

    private final static String RES_DIR = "schematronix/validator/namespaces/";

    @Test
    @DisplayName("Ensures that namespaces are correctly processed")
    void testNamespaces() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "schematronix.sch")).getFile());
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target.xml")).getFile());

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), definitionFile);
        final ValidationReport report = validator.validate(ValidatorConfiguration.fullValidation());

        assertFalse(report.isValid());
        assertEquals(List.of(
            new TriggeredAssertion("rule1", "pattern1", "", "Found ISBN with value '9780575075344'", "/library[1]/book[1]", "//book:book", "not(@isbn:isbn)"),
            new TriggeredAssertion("rule1", "pattern1", "", "Found ISBN with value '9780553573398'", "/library[1]/book[2]", "//book:book", "not(@isbn:isbn)")
        ), report.getFailedAsserts());
    }
}
