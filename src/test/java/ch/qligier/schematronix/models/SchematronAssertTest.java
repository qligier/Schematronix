package ch.qligier.schematronix.models;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.utils.SimpleXmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link SchematronAssert}.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
class SchematronAssertTest {

    /**
     * Ensures that it is an object.
     */
    @Test
    @DisplayName("Is a data object")
    void testIsADataObject() {
        final SchematronAssert schematronAssert = new SchematronAssert("warn", "II", "see", new ArrayList<>());
        final SchematronAssert schematronAssert2 = new SchematronAssert("fatal", "AA", null, null);
        assertTrue(schematronAssert.canEqual(schematronAssert2));

        assertNotNull(schematronAssert);
        assertNotNull(schematronAssert2);
        assertEquals("warn", schematronAssert.getRole());
        assertEquals("II", schematronAssert.getTest());
        assertEquals("see", schematronAssert.getSee());
        assertEquals(0, schematronAssert.getMessageNodes().size());
        assertNotEquals(schematronAssert, schematronAssert2);

        schematronAssert.setRole("fatal");
        assertEquals("fatal", schematronAssert.getRole());
        schematronAssert.setTest("AA");
        assertEquals("AA", schematronAssert.getTest());
        schematronAssert.setSee(null);
        assertNull(schematronAssert.getSee());
        schematronAssert.setMessageNodes(null);
        assertNull(schematronAssert.getMessageNodes());
        assertEquals(schematronAssert, schematronAssert2);

        assertTrue(schematronAssert.toString().length() > 0);

        assertEquals(schematronAssert, schematronAssert.clone());
        assertNotSame(schematronAssert, schematronAssert.clone());
    }

    /**
     * Ensures that it is initializable from an 'assert' element.
     */
    @Test
    @DisplayName("Creation from 'assert' element")
    void testCreationFromAssertElement() throws Exception {
        final Element assertElement = SimpleXmlParser
            .parseString("<assert role=\"warning\" test=\"II\" see=\"http\">Found: <value-of select=\"@root\"/></assert>")
            .getDocumentElement();
        final SchematronAssert schematronAssert = SchematronAssert.fromAssertElement(assertElement);
        assertNotNull(schematronAssert);
        assertEquals("warning", schematronAssert.getRole());
        assertEquals("II", schematronAssert.getTest());
        assertEquals("http", schematronAssert.getSee());
        assertEquals(2, schematronAssert.getMessageNodes().size());
    }

    /**
     * Ensures that it is not initializable from invalid elements.
     */
    @Test
    @DisplayName("Creation from invalid elements")
    void testCreationFromInvalidElements() {
        Exception exception = assertThrows(SchematronParsingException.class,
            () -> SchematronAssert.fromAssertElement(SimpleXmlParser.parseString("<assert role=\"warning\"> </assert>").getDocumentElement()));
        assertEquals("Missing attribute 'test' in 'assert' element", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronAssert.fromAssertElement(SimpleXmlParser.parseString("<alt role=\"warning\" test=\"II\">Found: <value-of " +
                "select=\"@root\"/></alt>").getDocumentElement()));
        assertEquals("The given node is not an 'assert' element", exception.getMessage());
    }
}
