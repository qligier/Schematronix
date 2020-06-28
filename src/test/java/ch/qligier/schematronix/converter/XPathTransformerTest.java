package ch.qligier.schematronix.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link XPathTransformer}.
 */
class XPathTransformerTest {

    /**
     * The XPath fixer to be tested.
     */
    private final XPathTransformer transformer = new XPathTransformer();

    /**
     * Ensures that non-transformable expressions are not modified.
     */
    @Test
    @DisplayName("Non-transformable XPath expressions")
    void testNonFixableExpressions() {
        final List<String> nonFixableExpressions = Arrays.asList(
            "not(.//hl7:translation[@codeSystemVersion][not(@codeSystem)])",
            "(local-name-from-QName(resolve-QName(@xsi:type,.))='CE' and namespace-uri-from-QName(resolve-QName(@xsi:type,.))='urn:hl7-org:v3') or not(@xsi:type)",
            "//*/hl7:id"
        );

        for (final String nonFixableExpression : nonFixableExpressions) {
            assertEquals(nonFixableExpression, this.transformer.transform(nonFixableExpression));
        }
    }

    /**
     * Ensures that transformable expressions are transformed.
     */
    @Test
    @DisplayName("Transformable XPath expressions")
    void testFixableExpressions() {
        // The initial wildcard should be fixed
        assertEquals(
            "//*/hl7:id",
            this.transformer.transform("*/hl7:id")
        );

        // The nesting predicate should be optimized
        assertEquals(
            "//*/hl7:ClinicalDocument/hl7:templateId[@root='1.3'][not(@nullFlavor)]",
            this.transformer.transform("//*/hl7:ClinicalDocument[hl7:templateId[@root='1.3']]/hl7:templateId[@root='1.3'][not(@nullFlavor)]")
        );

        // The whitespaces in attribute selectors should be normalized
        assertEquals("//*[@root='2.16'][@root='2.16']",
            this.transformer.transform("*[@root='2.16'][@root = '2.16']")
        );

        // All transformations should be applied
        assertEquals(
            "//*/hl7:observation[hl7:templateId[@root='2.16']]/hl7:effectiveTime",
            this.transformer.transform("*[hl7:observation[hl7:templateId[@root='2.16']]]/hl7:observation[hl7:templateId[@root = '2.16']]/hl7:effectiveTime")
        );

        // TODO: test multiple nesting predicates
    }
}
