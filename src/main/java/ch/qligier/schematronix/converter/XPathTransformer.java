package ch.qligier.schematronix.converter;

import lombok.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A transformer of XPath expressions that are either fixes or optimizations for the Schematronix use-case.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
class XPathTransformer {

    /**
     * A pattern that detects nesting predicates duplicating the following descendent selector.
     */
    private static final Pattern DUPLICATED_NESTING_PREDICATE_PATTERN = Pattern.compile("\\[(.+)]/\\1");

    /**
     * A pattern that detects whitespaces around the equal sign of an attribute selector.
     */
    private static final Pattern WHITESPACES_IN_ATTR_SELECTOR_PATTERN = Pattern.compile("(\\[@[a-zA-Z0-9]+)\\s*=\\s*");

    /**
     * Applies all transformations defined in this class to an XPath expression.
     *
     * @param xpathExpression The XPath expression to transform.
     * @return the fully transformed XPath expression.
     */
    String transform(@NonNull final String xpathExpression) {
        String xpathFixed = this.fixWildcardAtStart(xpathExpression);
        xpathFixed = this.normalizeAttributeSelector(xpathFixed);
        return this.optimizeDuplicateFilter(xpathFixed);
    }

    /**
     * Fixes XPath expressions that start with a wildcard ('*') by adding the prefix 'anywhere' ('//'). Expressions should start with an
     * axis, not the wildcard step? Saxon at least doesn't match any element with this kind of expression.
     *
     * @param xpathExpression The XPath expresion to fix.
     * @return the fixed XPath expression.
     */
    private String fixWildcardAtStart(@NonNull final String xpathExpression) {
        if (xpathExpression.startsWith("*")) {
            return "//" + xpathExpression;
        } else {
            return xpathExpression;
        }
    }

    /**
     * Optimizes XPath expressions that contain a nesting predicate duplicating the following descendent selector; by example:
     * '//html[body]/body'. In this case the nesting predicate can be dropped, resulting in a clearer, shorter XPath expression. If the
     * XPath engine does not  optimize this too, it should also be a little bit faster.
     *
     * @param xpathExpression The XPath expression to optimize.
     * @return the optimized XPath expression.
     */
    private String optimizeDuplicateFilter(@NonNull final String xpathExpression) {
        final Matcher matcher = DUPLICATED_NESTING_PREDICATE_PATTERN.matcher(xpathExpression);
        if (matcher.find()) {
            // If a duplicated nesting predicate is found, keep only the descendent selector
            return matcher.replaceAll("/$1");
        } else {
            return xpathExpression;
        }
    }

    /**
     * Normalizes the attribute selectors in XPath expressions. It removes whitespaces around the equal sign of an attribute selector; by
     * example in the expression: '*[@root = '1.3.6']'. It is useful to boost the efficiency of {@link #optimizeDuplicateFilter(String)} and
     * should be applied before.
     *
     * @param xpathExpression The XPath expression to normalize.
     * @return the normalized XPath expression.
     */
    private String normalizeAttributeSelector(@NonNull final String xpathExpression) {
        final Matcher matcher = WHITESPACES_IN_ATTR_SELECTOR_PATTERN.matcher(xpathExpression);
        if (matcher.find()) {
            return matcher.replaceAll("$1=");
        } else {
            return xpathExpression;
        }
    }
}
