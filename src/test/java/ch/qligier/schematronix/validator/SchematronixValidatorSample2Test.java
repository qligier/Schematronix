package ch.qligier.schematronix.validator;

import ch.qligier.schematronix.exceptions.SchematronixParsingException;
import net.sf.saxon.s9api.SaxonApiException;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bed for {@link SchematronixValidator}. It uses the resources of the 'sample2' folder.
 * <p>
 * It is used to verify that both XML parsers used by the validator are safe against XXE injections.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
class SchematronixValidatorSample2Test {

    private static final String URL_TO_FETCH = "https://api.ipify.org/";
    private static final String FILE_TO_FETCH;

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            FILE_TO_FETCH = "C:/Windows/System32/drivers/etc/hosts";
        } else {
            FILE_TO_FETCH = "/etc/hosts";
        }
    }

    /**
     * Tests the parser of the XML document for entity support. It should be deactivated.
     */
    @Test
    void testXmlXxe1() {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"rule1\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE replace [<!ENTITY entity \"VULNERABLE\"> ]>" +
            "<ClinicalDocument><title>&entity;</title></ClinicalDocument>";

        assertThrows(SaxonApiException.class, () -> {
            new SchematronixValidator(
                new StreamSource(new StringReader(xml)),
                new StreamSource(new StringReader(schematron))
            );
        });
    }

    /**
     * Tests the parser of the XML document for entity support. It should be deactivated.
     */
    @Test
    void testXmlXxe2() {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"rule1\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"file:///" + FILE_TO_FETCH + "\"> ]>" +
            "<ClinicalDocument><title>&xxe;</title></ClinicalDocument>";

        assertThrows(SaxonApiException.class, () -> {
            new SchematronixValidator(
                new StreamSource(new StringReader(xml)),
                new StreamSource(new StringReader(schematron))
            );
        });
    }

    /**
     * Tests the parser of the XML document for entity support. It should be deactivated.
     */
    @Test
    void testXmlXxe3() {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"rule1\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"" + URL_TO_FETCH + "\"> ]>" +
            "<ClinicalDocument><title>&xxe;</title></ClinicalDocument>";

        assertThrows(SaxonApiException.class, () -> {
            new SchematronixValidator(
                new StreamSource(new StringReader(xml)),
                new StreamSource(new StringReader(schematron))
            );
        });
    }

    /**
     * Tests the parser of the XML document for XInclude support. It should be deactivated.
     */
    @Test
    void testXmlXinclude1() throws Exception {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"rule1\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<ClinicalDocument>" +
            "<title xmlns:xi=\"http://www.w3.org/2001/XInclude\"><xi:include parse=\"text\" href=\"file:///" + FILE_TO_FETCH + "\"/></title>" +
            "</ClinicalDocument>";

        final SchematronixValidator validator = new SchematronixValidator(
            new StreamSource(new StringReader(xml)),
            new StreamSource(new StringReader(schematron))
        );
        final ValidationReport report = validator.validate(false);
        assertTrue(report.isValid());
    }

    /**
     * Tests the parser of the XML document for XInclude support. It should be deactivated.
     */
    @Test
    void testXmlXinclude2() throws Exception {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"rule1\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<ClinicalDocument>" +
            "<title xmlns:xi=\"http://www.w3.org/2001/XInclude\"><xi:include parse=\"text\" href=\"" + URL_TO_FETCH + "\"/></title>" +
            "</ClinicalDocument>";

        final SchematronixValidator validator = new SchematronixValidator(
            new StreamSource(new StringReader(xml)),
            new StreamSource(new StringReader(schematron))
        );
        final ValidationReport report = validator.validate(false);
        assertTrue(report.isValid());
    }

    /**
     * Tests the parser of the Schematronix document for entity support. It should be deactivated.
     */
    @Test
    void testSchematronixXxe1() throws Exception {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE replace [<!ENTITY entity \"VULNERABLE\"> ]>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"&entity;\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<ClinicalDocument><title>asd</title></ClinicalDocument>";

        final SchematronixValidator validator = new SchematronixValidator(
            new StreamSource(new StringReader(xml)),
            new StreamSource(new StringReader(schematron))
        );
        assertThrows(SchematronixParsingException.class, () -> validator.validate(false));
    }

    /**
     * Tests the parser of the Schematronix document for entity support. It should be deactivated.
     */
    @Test
    void testSchematronixXxe2() throws Exception {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"file:///" + FILE_TO_FETCH + "\"> ]>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"&xxe;\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<ClinicalDocument><title>asd</title></ClinicalDocument>";

        final SchematronixValidator validator = new SchematronixValidator(
            new StreamSource(new StringReader(xml)),
            new StreamSource(new StringReader(schematron))
        );
        assertThrows(SchematronixParsingException.class, () -> validator.validate(false));
    }

    /**
     * Tests the parser of the Schematronix document for entity support. It should be deactivated.
     */
    @Test
    void testSchematronixXxe3() throws Exception {
        final String schematron = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"" + URL_TO_FETCH + "\"> ]>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//ClinicalDocument\" id=\"&xxe;\">" +
            "<assert role=\"error\" test=\"string(title) = ''\">title not empty</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<ClinicalDocument><title>asd</title></ClinicalDocument>";

        final SchematronixValidator validator = new SchematronixValidator(
            new StreamSource(new StringReader(xml)),
            new StreamSource(new StringReader(schematron))
        );
        assertThrows(SchematronixParsingException.class, () -> validator.validate(false));
    }
}
