package ch.qligier.schematronix.cdachemed;

import ch.qligier.schematronix.converter.SchematronixWriter;
import ch.qligier.schematronix.models.SchematronDefinition;
import ch.qligier.schematronix.schematron.DefinitionParser;
import ch.qligier.schematronix.validator.SchematronixValidator;
import ch.qligier.schematronix.validator.ValidationReport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for CDA-CH-EMED PRE documents.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreTest {

    private final ClassLoader classLoader = getClass().getClassLoader();
    private final File schematronixFile;

    PreTest() throws Exception {
        final DefinitionParser definitionParser = new DefinitionParser();
        final SchematronixWriter schematronixWriter = new SchematronixWriter();

        final File schematronFile =
            new File(this.classLoader.getResource("cdachemed/v2.0/cdachemed-PRE.sch").getFile());
        final SchematronDefinition definition = definitionParser.parse(schematronFile);

        this.schematronixFile = new File(this.classLoader.getResource("cdachemed/v2.0/").getPath() + "schematronix_pre.sch");
        System.out.println(this.schematronixFile.getAbsolutePath());
        schematronixWriter.writeSchematronix(definition, this.schematronixFile);
    }

    @Test
    void testSchematronix() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        final Document document = documentBuilder.parse(this.schematronixFile);

        assertEquals(92, document.getElementsByTagName("pattern").getLength());
        assertEquals("template-2.16.756.5.30.1.1.10.1.4-2016-05-21T000000",
            document.getElementsByTagName("pattern").item(0).getAttributes().getNamedItem("id").getNodeValue());
        assertEquals("xslt2", document.getDocumentElement().getAttribute("queryBinding"));
        assertEquals("Schematron file for  - Medication Prescription document", document.getElementsByTagName("title").item(0).getTextContent());
    }

    /**
     * Test that the Schematronix validation matches the reference validation report, 'pre1_report.html'.
     */
    @Test
    void testPre1() throws Exception {
        final File preFile =
            new File(Objects.requireNonNull(this.classLoader.getResource("cdachemed/pre/pre1.xml")).getFile());
        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(preFile), this.schematronixFile);
        final ValidationReport report = validator.validate(false);

        assertFalse(report.isValid());
        assertEquals(9, report.getMessages().size());

        assertTrue(report.getMessages().get(0).contains("count(hl7:entryRelationship[@typeCode='COMP'])<=1"));
        assertTrue(report.getMessages().get(1).contains("count(hl7:entryRelationship[@typeCode='COMP'])<=1"));
        assertTrue(report.getMessages().get(2).contains("count(hl7:entryRelationship[@typeCode='COMP'])<=1"));
        assertTrue(report.getMessages().get(3).contains("count(hl7:entryRelationship[@typeCode='COMP'])<=1"));
        assertTrue(report.getMessages().get(4).contains("not(.)"));
        assertTrue(report.getMessages().get(5).contains("not(.)"));
        assertTrue(report.getMessages().get(6).contains("not(.)"));
        assertTrue(report.getMessages().get(7).contains("not(.)"));
        assertTrue(report.getMessages().get(8).contains("count(hl7:functionCode[concat(@code, @codeSystem) = doc('include/voc-2.16.756.5" +
            ".30.1.127.3.10.1.1.3-DYNAMIC.xml')//valueSet[1]/conceptList/concept/concat(@code, @codeSystem) or @nullFlavor]) >= 1"));
    }

    @AfterAll
    void clean() {
        this.schematronixFile.delete();
    }
}
