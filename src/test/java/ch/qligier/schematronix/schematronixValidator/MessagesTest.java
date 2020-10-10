package ch.qligier.schematronix.schematronixValidator;

import ch.qligier.schematronix.SchematronixValidator;
import ch.qligier.schematronix.exceptions.SchematronixValidationException;
import ch.qligier.schematronix.validation.TriggeredAssertion;
import ch.qligier.schematronix.validation.ValidationReport;
import ch.qligier.schematronix.validation.ValidatorConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bed for {@link SchematronixValidator}. It uses the resources of the 'message' folder.
 * <p>
 * The reference Schematron validation report 'target.html' is generated with the script 'validate_schematron.bat'.
 *
 * @author Quentin Ligier
 */
class MessagesTest {

    private final static String RES_DIR = "schematronix/validator/message/";

    @Test
    @DisplayName("Ensure that messages are correctly processed")
    void testMessages() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "schematronix.sch")).getFile());
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target.xml")).getFile());

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), definitionFile);
        ValidationReport report = validator.validate(new ValidatorConfiguration(false, true, false));

        assertFalse(report.isValid());
        assertEquals(List.of(
            new TriggeredAssertion("rule1", "pattern1", "", "Found ISBN with value '9780575075344'", null, "//book", "not(@isbn)"),
            new TriggeredAssertion("rule1", "pattern1", "", "Found ISBN with value '9780553573398'", null, "//book", "not(@isbn)")
        ), report.getFailedAsserts());

        report = validator.validate(new ValidatorConfiguration(false, false, false));
        assertFalse(report.isValid());
        assertEquals(List.of(
            new TriggeredAssertion("rule1", "pattern1", "", "Found ISBN with value '<value-of select=\"@isbn\" />'", null, "//book", "not(@isbn)"),
            new TriggeredAssertion("rule1", "pattern1", "", "Found ISBN with value '<value-of select=\"@isbn\" />'", null, "//book", "not(@isbn)")
        ), report.getFailedAsserts());
    }

    @Test
    @DisplayName("Ensure that 'value-of's selector yields at least one value")
    void testInvalidValueOfWithNoValue() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target.xml")).getFile());
        final String definition = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">\n" +
            "    <pattern id=\"pattern1\">\n" +
            "        <rule context=\"//book\" id=\"rule1\">\n" +
            "            <assert test=\"not(@isbn)\">Found ISBN with value '<value-of select=\"@nonexistent\"/>'</assert>\n" +
            "        </rule>\n" +
            "    </pattern>\n" +
            "</schema>\n";

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), new StreamSource(new ByteArrayInputStream(definition.getBytes())));
        assertThrows(SchematronixValidationException.class, () -> validator.validate(ValidatorConfiguration.fullValidation()));
    }

    @Test
    @DisplayName("Ensure that 'value-of's selector yields at max one value")
    void testInvalidValueOfWithMultipleValues() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target.xml")).getFile());
        final String definition = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">\n" +
            "    <pattern id=\"pattern1\">\n" +
            "        <rule context=\"//book\" id=\"rule1\">\n" +
            "            <assert test=\"not(@isbn)\">Found ISBN with value '<value-of select=\"(@isbn|@id)\"/>'</assert>\n" +
            "        </rule>\n" +
            "    </pattern>\n" +
            "</schema>\n";

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), new StreamSource(new ByteArrayInputStream(definition.getBytes())));

        Exception exception = null;
        try {
            validator.validate(ValidatorConfiguration.fullValidation());
        } catch (final Exception e) {
            exception = e;
        }
        assertTrue(exception instanceof SchematronixValidationException);
        assertEquals("The XPath selector yielded no value or multiple values", exception.getMessage());
    }
}
