package ch.qligier.schematronix.definition;

import ch.qligier.schematronix.exceptions.SchematronParsingException;
import ch.qligier.schematronix.utils.SimpleXmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link SchematronRule}.
 *
 * @author Quentin Ligier
 */
class SchematronRuleTest {

    /**
     * Ensures that it is an object.
     */
    @Test
    @DisplayName("Is a data object")
    void testIsADataObject() {
        final SchematronRule schematronRule = new SchematronRule("pattern", "id", "context", new ArrayList<>(), false);
        final SchematronRule schematronRule2 = new SchematronRule("pattern2", "id2", "context2", List.of(new SchematronLet("", "")), true);
        assertTrue(schematronRule.canEqual(schematronRule2));

        assertNotNull(schematronRule);
        assertNotNull(schematronRule2);
        assertEquals("pattern", schematronRule.getPattern());
        assertEquals("id", schematronRule.getId());
        assertEquals("context", schematronRule.getContext());
        assertEquals(0, schematronRule.getChildren().size());
        assertFalse(schematronRule.isAbstract());
        assertNotEquals(schematronRule, schematronRule2);

        schematronRule.setPattern("pattern2");
        assertEquals("pattern2", schematronRule.getPattern());
        schematronRule.setId("id2");
        assertEquals("id2", schematronRule.getId());
        schematronRule.setContext("context2");
        assertEquals("context2", schematronRule.getContext());
        schematronRule.setChildren(List.of(new SchematronLet("", "")));
        assertEquals(1, schematronRule.getChildren().size());
        schematronRule.setAbstract(true);
        assertTrue(schematronRule.isAbstract());
        assertEquals(schematronRule, schematronRule2);

        assertTrue(schematronRule.toString().length() > 0);

        final SchematronRule clonedRule = schematronRule.clone();
        assertEquals(schematronRule, clonedRule);
        assertNotSame(schematronRule, clonedRule);
        assertEquals(schematronRule.getChildren().get(0), clonedRule.getChildren().get(0));
        assertNotSame(schematronRule.getChildren().get(0), clonedRule.getChildren().get(0));
    }

    /**
     * Ensures that it is initializable from a 'rule' element without pattern.
     */
    @Test
    @DisplayName("Creation from 'rule' element without pattern")
    void testCreationFromRuleElementWithoutPattern() throws Exception {
        final Element ruleElement = SimpleXmlParser
            .parseString("<rule context=\"*[hl7:section[hl7:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.13']]]\">" +
                "<extends rule=\"SD.TEXT\"/>" +
                "<assert role=\"error\" see=\"http\" test=\"not(@xsi:type)\">message</assert>" +
                "</rule>")
            .getDocumentElement();
        final SchematronRule schematronRule = SchematronRule.fromRuleElement(ruleElement);
        assertNotNull(schematronRule);
        assertEquals(36, schematronRule.getId().length());
        assertEquals("*[hl7:section[hl7:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.13']]]", schematronRule.getContext());
        assertNull(schematronRule.getPattern());
        assertFalse(schematronRule.isAbstract());
        assertEquals(2, schematronRule.getChildren().size());
        assertTrue(schematronRule.hasExtends());
        assertTrue(schematronRule.getExtends().isPresent());
    }

    /**
     * Ensures that it is initializable from a 'rule' element with pattern.
     */
    @Test
    @DisplayName("Creation from 'rule' element with pattern")
    void testCreationFromRuleElementWithPattern() throws Exception {
        final Element ruleElement = SimpleXmlParser
            .parseString("<rule " +
                "id=\"d19e121-false-d986e0\" abstract=\"true\">" +
                "<assert role=\"error\" see=\"http\" test=\"not(@xsi:type)\">message</assert>" +
                "<assert role=\"error\" see=\"http\" test=\"not(@xsi:type)\">message</assert>" +
                "<assert role=\"error\" see=\"http\" test=\"not(@xsi:type)\">message</assert>" +
                "</rule>")
            .getDocumentElement();
        final SchematronRule schematronRule = SchematronRule.fromRuleElement(ruleElement, "pattern");
        assertNotNull(schematronRule);
        assertEquals("d19e121-false-d986e0", schematronRule.getId());
        assertNull(schematronRule.getContext());
        assertEquals("pattern", schematronRule.getPattern());
        assertTrue(schematronRule.isAbstract());
        assertEquals(3, schematronRule.getChildren().size());
        assertFalse(schematronRule.hasExtends());
        assertFalse(schematronRule.getExtends().isPresent());
    }

    /**
     * Ensures that it is not initializable from invalid elements.
     */
    @Test
    @DisplayName("Creation from invalid elements")
    void testCreationFromInvalidElements() {
        Exception exception = assertThrows(SchematronParsingException.class,
            () -> SchematronRule.fromRuleElement(SimpleXmlParser.parseString("<rule abstract=\"true\" context=\"\" id=\"\"></rule>").getDocumentElement()));
        assertEquals("An abstract 'rule' element shall not have a context", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronRule.fromRuleElement(SimpleXmlParser.parseString("<rule abstract=\"true\"></rule>").getDocumentElement()));
        assertEquals("An abstract 'rule' element shall have an ID", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronRule.fromRuleElement(SimpleXmlParser.parseString("<rule abstract=\"FALSE\" id=\"\"></rule>").getDocumentElement()));
        assertEquals("A non-abstract 'rule' element shall have a context", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronRule.fromRuleElement(SimpleXmlParser.parseString("<alt context=\"\" id=\"\"></alt>").getDocumentElement()));
        assertEquals("The given node is not a 'rule' element", exception.getMessage());

        exception = assertThrows(SchematronParsingException.class,
            () -> SchematronRule.fromRuleElement(SimpleXmlParser.parseString("<rule abstract=\"something\"></rule>").getDocumentElement()));
        assertEquals("The 'abstract' attribute of the 'rule' element shall be either true or false", exception.getMessage());
    }
}
