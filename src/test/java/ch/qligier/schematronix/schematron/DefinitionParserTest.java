package ch.qligier.schematronix.schematron;

import ch.qligier.schematronix.models.SchematronAssert;
import ch.qligier.schematronix.models.SchematronDefinition;
import ch.qligier.schematronix.models.SchematronLet;
import ch.qligier.schematronix.models.SchematronRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link DefinitionParser}.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
class DefinitionParserTest {

    @Test
    @DisplayName("Simple parsing test")
    void testSimple() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile =
            new File(Objects.requireNonNull(classLoader.getResource("schematron/parsing_tests/simple/main.sch")).getFile());

        final DefinitionParser parser = new DefinitionParser();
        final SchematronDefinition definition = parser.parse(definitionFile);

        assertNotNull(definition);
        assertEquals("Simple Schematron definition", definition.getTitle());
        assertEquals("xslt2", definition.getQueryBinding());

        assertEquals(2, definition.getNamespaces().size());
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", definition.getNamespaces().get("ns1"));
        assertEquals("http://www.w3.org/2001/XMLSchema", definition.getNamespaces().get("ns2"));

        assertEquals(4, definition.getDefinedRules().size());
        assertTrue(definition.getDefinedRules().containsKey("rule1"));
        assertTrue(definition.getDefinedRules().containsKey("rule2"));
        assertTrue(definition.getDefinedRules().containsKey("rule3"));
        assertTrue(definition.getDefinedRules().containsKey("rule4"));

        assertEquals(1, definition.getPatterns().size());
        assertEquals("pattern1", definition.getPatterns().iterator().next().getId());

        assertEquals(1, definition.getEnabledRules().size());
        assertTrue(definition.getEnabledRules().contains("rule4"));

        assertEquals(1, definition.getRulesPerPattern().get("pattern1").size());
        assertTrue(definition.getRulesPerPattern().get("pattern1").contains("rule3"));

        final SchematronRule rule3 = definition.getResolvedRule("rule3");
        assertNotNull(rule3);
        assertEquals("pattern1", rule3.getPattern());
        assertEquals("rule3", rule3.getId());
        assertEquals("/", rule3.getContext());
        assertFalse(rule3.isAbstract());
        assertEquals(5, rule3.getChildren().size());
        assertEquals("test1.1", ((SchematronAssert)rule3.getChildren().get(0)).getTest());
        assertEquals("test2.1", ((SchematronAssert)rule3.getChildren().get(1)).getTest());
        assertEquals("var2.2", ((SchematronLet)rule3.getChildren().get(2)).getName());
        assertEquals("test2.3", ((SchematronAssert)rule3.getChildren().get(3)).getTest());
        // TODO: missing report here
        assertEquals("test3.1", ((SchematronAssert)rule3.getChildren().get(4)).getTest());
    }

}
