package ch.qligier.schematronix.validation;

import ch.qligier.schematronix.SchematronixValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test bed for {@link SchematronixValidator}. It uses the resources of the 'sample1' folder.
 * <p>
 * The expected behavior is as follows: when an assertion is evaluated (true or false???) on a node, this node can still be evaluated by
 * latter assertions of the same rule but must be excluded from latter rules of the same pattern. A new pattern resets this exclude list.
 * <p>
 * The reference Schematron validation report 'target1.html' is generated with the script 'validate_schematron.bat'.
 *
 * @author Quentin Ligier
 */
class SchematronixValidatorSample1Test {

    private final static String RES_DIR = "schematronix/validator/sample1/";

    @Test
    @DisplayName("Nodes should be evaluated in one rule of a pattern")
    void testNodeExclusionPerPattern() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File definitionFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "schematronix1.sch")).getFile());
        final File xmlFile = new File(Objects.requireNonNull(classLoader.getResource(RES_DIR + "target1.xml")).getFile());

        final SchematronixValidator validator = new SchematronixValidator(new StreamSource(xmlFile), definitionFile);
        ValidationReport report = validator.validate(false);

        assertFalse(report.isValid());
        assertEquals(6, report.getFailedAsserts().size());

        final List<String> expectedMessages = List.of(
            "[role:error][id:rule1][pattern:pattern1] Failed assert '@attr1' for node <book/> in context '//book'",
            "[role:error][id:rule1][pattern:pattern1] Failed assert '@attr1' for node <book/> in context '//book'",
            "[role:error][id:rule1][pattern:pattern1] Failed assert '@attr2' for node <book/> in context '//book'",
            "[role:error][id:rule4][pattern:pattern2] Failed assert '@attr1' for node <book/> in context '//*[@id]'",
            "[role:error][id:rule4][pattern:pattern2] Failed assert '@attr1' for node <book/> in context '//*[@id]'",
            "[role:error][id:rule4][pattern:pattern2] Failed assert '@attr2' for node <book/> in context '//*[@id]'"
        );
        assertEquals(expectedMessages, report.getFailedAsserts());

        report = validator.validate(true);
        assertFalse(report.isValid());
        assertEquals(1, report.getFailedAsserts().size());
        assertEquals("[role:error][id:rule1][pattern:pattern1] Failed assert '@attr1' for node <book/> in context '//book'",
            report.getFailedAsserts().get(0));
    }
}
