package ch.qligier.schematronix.definition;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.utils.SimpleXmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link SchematronLet}.
 *
 * @author Quentin Ligier
 */
class SchematronLetTest {

    /**
     * Ensures that it is an object.
     */
    @Test
    @DisplayName("Is a data object")
    void testIsADataObject() {
        final SchematronLet schematronLet = new SchematronLet("document", "/hl7:ClinicalDocument");
        final SchematronLet schematronLet2 = new SchematronLet("name", "value");
        assertTrue(schematronLet.canEqual(schematronLet2));
        assertFalse(schematronLet.canEqual(new Object()));

        assertNotNull(schematronLet);
        assertNotNull(schematronLet2);
        assertEquals("document", schematronLet.getName());
        assertEquals("/hl7:ClinicalDocument", schematronLet.getValue());
        assertNotEquals(schematronLet, schematronLet2);

        schematronLet.setName("name");
        schematronLet.setValue("value");
        assertEquals("name", schematronLet.getName());
        assertEquals("value", schematronLet.getValue());
        assertEquals(schematronLet, schematronLet2);

        assertTrue(schematronLet.toString().length() > 0);

        assertEquals(schematronLet, schematronLet.clone());
        assertNotSame(schematronLet, schematronLet.clone());
    }

    /**
     * Ensures that it is initializable from a 'let' element.
     */
    @Test
    @DisplayName("Creation from 'let' element")
    void testCreationFromLetElement() throws Exception {
        final Element letElement = SimpleXmlParser
            .parseString("<let name=\"document\" value=\"/hl7:ClinicalDocument\" />")
            .getDocumentElement();
        final SchematronLet schematronLet = SchematronLet.fromLetElement(letElement);
        assertNotNull(schematronLet);
        assertEquals("document", schematronLet.getName());
        assertEquals("/hl7:ClinicalDocument", schematronLet.getValue());
    }

    /**
     * Ensures that it is not initializable from invalid elements.
     */
    @Test
    @DisplayName("Creation from invalid elements")
    void testCreationFromInvalidElements() {
        assertThrows(SchematronParsingException.class,
            () -> SchematronLet.fromLetElement(SimpleXmlParser.parseString("<let name=\"document\" />").getDocumentElement()));
        assertThrows(SchematronParsingException.class,
            () -> SchematronLet.fromLetElement(SimpleXmlParser.parseString("<let value=\"/hl7:ClinicalDocument\" />").getDocumentElement()));
        assertThrows(SchematronParsingException.class,
            () -> SchematronLet.fromLetElement(SimpleXmlParser.parseString("<let attr=\"document\" />").getDocumentElement()));
        assertThrows(SchematronParsingException.class,
            () -> SchematronLet.fromLetElement(SimpleXmlParser.parseString("<alt name=\"document\" value=\"/hl7:ClinicalDocument\" />").getDocumentElement()));
    }
}
