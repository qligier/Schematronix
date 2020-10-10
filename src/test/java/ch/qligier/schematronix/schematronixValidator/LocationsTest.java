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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bed for locations support.
 *
 * @author Quentin Ligier
 */
public class LocationsTest {

    private final static String RES_DIR = "schematronix/validator/message/";

    @Test
    @DisplayName("Ensure that locations are correctly computed")
    void testMessages() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "schematronix.sch")).getFile());
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target.xml")).getFile());

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), definitionFile);
        ValidationReport report = validator.validate(new ValidatorConfiguration(false, false, true));

        assertFalse(report.isValid());
        assertEquals(
            List.of("/library[1]/book[1]", "/library[1]/book[2]"),
            report.getFailedAsserts().stream().map(TriggeredAssertion::getLocation).collect(Collectors.toList())
        );

        report = validator.validate(new ValidatorConfiguration(false, false, false));
        assertFalse(report.isValid());
        assertNull(report.getFailedAsserts().get(0).getLocation());
        assertNull(report.getFailedAsserts().get(1).getLocation());
    }
}
