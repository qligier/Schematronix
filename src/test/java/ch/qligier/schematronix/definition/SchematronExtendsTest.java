package ch.qligier.schematronix.definition;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.utils.SimpleXmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link SchematronExtends}.
 *
 * @author Quentin Ligier
 */
class SchematronExtendsTest {

    /**
     * Ensures that it is an object.
     */
    @Test
    @DisplayName("Is a data object")
    void testIsADataObject() {
        final SchematronExtends schematronExtends = new SchematronExtends("II");
        final SchematronExtends schematronExtends2 = new SchematronExtends("AA");
        assertTrue(schematronExtends.canEqual(schematronExtends2));

        assertNotNull(schematronExtends);
        assertNotNull(schematronExtends2);
        assertEquals("II", schematronExtends.getExtendsRuleId());
        assertNotEquals(schematronExtends, schematronExtends2);

        schematronExtends.setExtendsRuleId("AA");
        assertEquals("AA", schematronExtends.getExtendsRuleId());
        assertEquals(schematronExtends, schematronExtends2);

        assertTrue(schematronExtends.toString().length() > 0);

        assertEquals(schematronExtends, schematronExtends.clone());
        assertNotSame(schematronExtends, schematronExtends.clone());
    }

    /**
     * Ensures that it is initializable from an 'extends' element.
     */
    @Test
    @DisplayName("Creation from 'extends' element")
    void testCreationFromExtendsElement() throws Exception {
        final Element extendsElement = SimpleXmlParser
            .parseString("<extends rule=\"II\" />")
            .getDocumentElement();
        final SchematronExtends schematronExtends = SchematronExtends.fromExtendsElement(extendsElement);
        assertNotNull(schematronExtends);
        assertEquals("II", schematronExtends.getExtendsRuleId());
    }

    /**
     * Ensures that it is not initializable from invalid elements.
     */
    @Test
    @DisplayName("Creation from invalid elements")
    void testCreationFromInvalidElements() {
        Exception exception = assertThrows(SchematronParsingException.class,
            () -> SchematronExtends.fromExtendsElement(SimpleXmlParser.parseString("<extends id=\"document\" />").getDocumentElement()));
        assertEquals("Missing attribute 'rule' in 'extends' element", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronExtends.fromExtendsElement(SimpleXmlParser.parseString("<extends />").getDocumentElement()));
        assertEquals("Missing attribute 'rule' in 'extends' element", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronExtends.fromExtendsElement(SimpleXmlParser.parseString("<alt rule=\"II\" />").getDocumentElement()));
        assertEquals("This is not an 'extends' element", exception.getMessage());
    }
}
