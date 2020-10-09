package ch.qligier.schematronix.schematronixValidator;

import ch.qligier.schematronix.SchematronixValidator;
import ch.qligier.schematronix.exceptions.SchematronixParsingException;
import ch.qligier.schematronix.validation.ValidatorConfiguration;
import lombok.NonNull;
import net.sf.saxon.s9api.SaxonApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bed to ensure Schematronix files parsing is correct.
 *
 * @author Quentin Ligier
 */
public class ParsingTest {

    @Test
    @DisplayName("Ensure that unknown tags are rejected")
    void testUnknownTag() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<tag />" +
            "</schema>";
        testSchematronixParsing(schematronix, "Unknown 'tag' element");
    }

    @Test
    @DisplayName("Ensure that 'value-of' elements have a 'select' attribute")
    void testValueOfHasSelect() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//book\" id=\"rule1\">" +
            "<assert test=\"not(@isbn)\">Found ISBN with value '<value-of/>'</assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'value-of' element is missing its 'select' attribute");
    }

    @Test
    @DisplayName("Ensure that a namespace has a prefix")
    void testNamespaceHasPrefix() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<ns uri=\"urn:example:schematronix:book\"/>" +
            "</schema>";
        testSchematronixParsing(schematronix, "The namespace prefix is mandatory");
    }

    @Test
    @DisplayName("Ensure that a namespace has an URI")
    void testNamespaceHasUri() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<ns prefix=\"book\"/>" +
            "</schema>";
        testSchematronixParsing(schematronix, "The namespace URI is mandatory");
    }

    @Test
    @DisplayName("Ensure that a pattern has an id")
    void testPatternHasId() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'pattern' element is missing its 'id' attribute");
    }

    @Test
    @DisplayName("Ensure that a pattern does not appear in a pattern")
    void testPatternNotInPattern() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<pattern id=\"pattern2\">" +
            "</pattern>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'pattern' element appears inside another pattern");
    }

    @Test
    @DisplayName("Ensure that a rule has a context")
    void testRuleHasContext() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'rule' element is missing its 'context' attribute");
    }

    @Test
    @DisplayName("Ensure that a rule is defined in a pattern")
    void testRuleNotOutsidePattern() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<rule context=\"//book\" id=\"rule1\">" +
            "</rule>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'rule' element appears outside a pattern");
    }

    @Test
    @DisplayName("Ensure that an assert is defined in a rule")
    void testAssertNotOutsideRule() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<assert test=\"not(@isbn)\">Found ISBN with value '<value-of select=\"@isbn:isbn\"/>'</assert>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "An 'assert' element appears outside a 'rule' element");
    }

    @Test
    @DisplayName("Ensure that an assert has a test")
    void testAssertHasTest() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//book\" id=\"rule1\">" +
            "<assert></assert>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "An 'assert' element is missing its 'test' attribute");
    }

    @Test
    @DisplayName("Ensure that a report is defined in a rule")
    void testReportNotOutsideRule() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<report test=\"not(@isbn)\">Found ISBN with value '<value-of select=\"@isbn:isbn\"/>'</report>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'report' element appears outside a 'rule' element");
    }

    @Test
    @DisplayName("Ensure that a report has a test")
    void testReportHasTest() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//book\" id=\"rule1\">" +
            "<report></report>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'report' element is missing its 'test' attribute");
    }

    @Test
    @DisplayName("Ensure that a let is defined in a rule")
    void testLetNotOutsideRule() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<let value=\"number(substring(.,1,2))\"/>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'let' element appears outside a 'rule' element");
    }

    @Test
    @DisplayName("Ensure that a let has a name")
    void testLetHasName() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//book\" id=\"rule1\">" +
            "<let value=\"number(substring(.,1,2))\"/>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'let' element is missing its 'name' attribute");
    }

    @Test
    @DisplayName("Ensure that a let has a value")
    void testLetHasValue() throws Exception {
        final String schematronix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" queryBinding=\"xslt2\">" +
            "<pattern id=\"pattern1\">" +
            "<rule context=\"//book\" id=\"rule1\">" +
            "<let name=\"hours\"/>" +
            "</rule>" +
            "</pattern>" +
            "</schema>";
        testSchematronixParsing(schematronix, "A 'let' element is missing its 'value' attribute");
    }

    private void testSchematronixParsing(@NonNull final String schematronix,
                                         @NonNull final String expectedExceptionMessage) throws SaxonApiException {
        final SchematronixValidator validator = new SchematronixValidator(
            new StreamSource(new ByteArrayInputStream("<?xml version=\"1.0\"?><document></document>".getBytes())),
            new StreamSource(new ByteArrayInputStream(schematronix.getBytes()))
        );
        Exception exception = null;
        try {
            validator.validate(ValidatorConfiguration.fullValidation());
        } catch (final Exception e) {
            exception = e;
        }
        assertTrue(exception instanceof SchematronixParsingException);
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }
}
