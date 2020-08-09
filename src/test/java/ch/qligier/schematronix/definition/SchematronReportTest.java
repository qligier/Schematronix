package ch.qligier.schematronix.definition;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.utils.SimpleXmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link SchematronReport}.
 *
 * @author Quentin Ligier
 */
class SchematronReportTest {

    /**
     * Ensures that it is an object.
     */
    @Test
    @DisplayName("Is a data object")
    void testIsADataObject() {
        final SchematronReport schematronReport = new SchematronReport("warn", "II", "see", new ArrayList<>());
        final SchematronReport schematronReport2 = new SchematronReport("fatal", "AA", null, null);
        assertTrue(schematronReport.canEqual(schematronReport2));

        assertNotNull(schematronReport);
        assertNotNull(schematronReport2);
        assertEquals("warn", schematronReport.getRole());
        assertEquals("II", schematronReport.getTest());
        assertEquals("see", schematronReport.getSee());
        assertEquals(0, schematronReport.getMessageNodes().size());
        assertNotEquals(schematronReport, schematronReport2);

        schematronReport.setRole("fatal");
        assertEquals("fatal", schematronReport.getRole());
        schematronReport.setTest("AA");
        assertEquals("AA", schematronReport.getTest());
        schematronReport.setSee(null);
        assertNull(schematronReport.getSee());
        schematronReport.setMessageNodes(null);
        assertNull(schematronReport.getMessageNodes());
        assertEquals(schematronReport, schematronReport2);

        assertTrue(schematronReport.toString().length() > 0);

        assertEquals(schematronReport, schematronReport.clone());
        assertNotSame(schematronReport, schematronReport.clone());
    }

    /**
     * Ensures that it is initializable from a 'report' element.
     */
    @Test
    @DisplayName("Creation from 'report' element")
    void testCreationFromReportElement() throws Exception {
        final Element reportElement = SimpleXmlParser
            .parseString("<report role=\"warning\" test=\"II\" see=\"http\">Found: <value-of select=\"@root\"/></report>")
            .getDocumentElement();
        final SchematronReport schematronReport = SchematronReport.fromReportElement(reportElement);
        assertNotNull(schematronReport);
        assertEquals("warning", schematronReport.getRole());
        assertEquals("II", schematronReport.getTest());
        assertEquals("http", schematronReport.getSee());
        assertEquals(2, schematronReport.getMessageNodes().size());
    }

    /**
     * Ensures that it is not initializable from invalid elements.
     */
    @Test
    @DisplayName("Creation from invalid elements")
    void testCreationFromInvalidElements() {
        Exception exception = assertThrows(SchematronParsingException.class,
            () -> SchematronReport.fromReportElement(SimpleXmlParser.parseString("<report role=\"warning\"> </report>").getDocumentElement()));
        assertEquals("Missing attribute 'test' in 'report' element", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronReport.fromReportElement(SimpleXmlParser.parseString("<alt role=\"warning\" test=\"II\">Found: <value-of " +
                "select=\"@root\"/></alt>").getDocumentElement()));
        assertEquals("The given node is not an 'report' element", exception.getMessage());
    }
}
