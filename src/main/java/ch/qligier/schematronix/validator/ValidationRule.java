package ch.qligier.schematronix.validator;

import ch.qligier.schematronix.exceptions.SchematronixValidationException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.sf.saxon.s9api.*;

import java.net.URI;
import java.util.*;

/**
 * A rule of the Schematronix validator. A rule is made of variable definitions and assertions that apply to a list of context items.
 *
 * @author Quentin Ligier
 */
@Data
class ValidationRule {

    /**
     * The ordered list of children (asserts and variables) that make this validation rule.
     */
    private final List<Child> children = new ArrayList<>();

    /**
     * The rule ID.
     */
    private String id;

    /**
     * The rule pattern or {@code null} if it does not belong to a pattern.
     */
    private String pattern;

    /**
     * The context XPath expression.
     */
    private String contextXpathExpression;

    /**
     * The rule context as a list of evaluated items (nodes, values).
     */
    private final XdmValue contextItems;

    /**
     * The XPath compiler.
     */
    private XPathCompiler xpathCompiler;

    /**
     * The list of defined variable names in the rule.
     */
    private final List<String> definedVariableNames = new ArrayList<>();

    /**
     * The list of hashes of nodes that have already been asserted in this pattern. A node may not be asserted by more than one rule in a
     * given pattern.
     */
    private final List<Integer> patternAssertedNodes;

    /**
     * Constructs a new validation rule.
     *
     * @param processor                 The XPath processor.
     * @param namespaces                The list of namespaces defined. The key is the namespace prefix, the value is the namespace URI.
     * @param contextItems              The rule context as a list of evaluated items (nodes, values).
     * @param schematronixDirectoryPath The path of the Schematronix file parent directory or {@code null} if it's not needed.
     * @param patternAssertedNodes      The list of hashes of nodes that have already been asserted in this pattern.
     */
    ValidationRule(@NonNull final Processor processor,
                   @NonNull final Map<String, String> namespaces,
                   @NonNull final XdmValue contextItems,
                   final URI schematronixDirectoryPath,
                   @NonNull final List<Integer> patternAssertedNodes) {
        this.contextItems = contextItems;
        this.patternAssertedNodes = patternAssertedNodes;
        this.xpathCompiler = processor.newXPathCompiler();
        for (final Map.Entry<String, String> namespace : namespaces.entrySet()) {
            this.xpathCompiler.declareNamespace(namespace.getKey(), namespace.getValue());
        }
        if (schematronixDirectoryPath != null) {
            this.xpathCompiler.setBaseURI(schematronixDirectoryPath);
        }
    }

    /**
     * Defines a new variable in the rule.
     *
     * @param variableName            The variable name.
     * @param variableXpathExpression The variable value, as an XPath expression.
     * @throws SaxonApiException if an error is encountered when compiling the XPath expression.
     */
    void addVariable(@NonNull final String variableName,
                     @NonNull final String variableXpathExpression) throws SaxonApiException {
        final Variable variable = new Variable();
        variable.setNbVariables(this.definedVariableNames.size());
        variable.setName(variableName);
        variable.setExecutable(this.xpathCompiler.compile(variableXpathExpression));
        this.children.add(variable);
        this.definedVariableNames.add(variableName);
        this.xpathCompiler.declareVariable(toQName(variableName));
    }

    /**
     * Defines a new assert in the rule.
     *
     * @param assertXpathExpression The assert XPath expression.
     * @param assertRole            The assert role.
     * @throws SaxonApiException if an error is encountered when compiling the XPath expression.
     */
    void addAssert(@NonNull final String assertXpathExpression,
                   @NonNull final String assertRole) throws SaxonApiException {
        final Assert anAssert = new Assert();
        anAssert.setNbVariables(this.definedVariableNames.size());
        anAssert.setXpath(assertXpathExpression);
        anAssert.setRole(assertRole);
        anAssert.setExecutable(this.xpathCompiler.compile(assertXpathExpression));
        this.children.add(anAssert);
    }

    /**
     * Executes the validation rule and stores the results in the report. If failFast is set, the validation stops at the first error.
     *
     * @param report   The report that will be updated with the rule validation results.
     * @param failFast if {@code true}, the validation is stopped at the first encountered error; if {@code false}, the validation is fully
     *                 performed.
     * @throws SaxonApiException               if the execution of an XPath expression fails.
     * @throws SchematronixValidationException if the validation has been stopped at the first error.
     */
    void execute(@NonNull final ValidationReport report,
                 final boolean failFast) throws SaxonApiException, SchematronixValidationException {
        for (final XdmItem contextNode : this.contextItems) {
            final int nodeHash = contextNode.hashCode();

            if (this.patternAssertedNodes.contains(nodeHash)) {
                /*
                 * The node has already been asserted by another rule in this pattern, we skip it.
                 * In the XSL, it's the 'mode' and 'priority' attributes of the <template/> tag that control this behavior.
                 */
                continue;
            }
            this.patternAssertedNodes.add(nodeHash);
            this.executeSingle(contextNode, report, failFast);
        }
    }

    /**
     * Executes the validation rule on a single item of the context. The validation stops at the first encountered error.
     *
     * @param contextItem The context item on which to execute the validation rule.
     * @param report      The report that will be updated with the rule validation results.
     * @param failFast    if {@code true}, the validation is stopped at the first encountered error; if {@code false}, the validation is
     *                    fully performed.
     * @throws SaxonApiException               if an error arises during the XPath expression execution.
     * @throws SchematronixValidationException if the assert fails on the given value.
     */
    private void executeSingle(@NonNull final XdmItem contextItem,
                               @NonNull final ValidationReport report,
                               final boolean failFast) throws SaxonApiException, SchematronixValidationException {
        // The list of variables and their evaluated value
        final Map<String, XdmValue> variables = new HashMap<>();

        for (final Child child : this.getChildren()) {
            // Create and prepare a new XPath selector tuned for this child
            final XPathSelector selector = child.getExecutable().load();
            selector.setContextItem(contextItem);

            // Declare to the new XPath selector the variables that were defined before the child we are processing.
            for (int i = 0; i < child.getNbVariables(); ++i) {
                final String previousVariableName = this.definedVariableNames.get(i);
                selector.setVariable(
                    toQName(previousVariableName),
                    variables.get(previousVariableName)
                );
            }

            if (child instanceof Variable) {
                variables.put(((Variable) child).getName(), selector.evaluate());
            } else {
                // If the assert selector doesn't evaluate to 'true', it has failed
                if (!selector.effectiveBooleanValue()) {
                    final Assert failedAssert = (Assert) child;
                    report.getMessages().add(String.format(
                        "[role:%s][id:%s][pattern:%s] Failed assert '%s' for node %s in context '%s'",
                        failedAssert.getRole(),
                        this.id,
                        this.pattern,
                        failedAssert.getXpath(),
                        ((XdmNode) contextItem).getUnderlyingValue().toShortString(),
                        this.contextXpathExpression
                    ));
                    if (failFast) {
                        throw new SchematronixValidationException("Failed validation, stopping");
                    }
                }
            }
        }
    }

    /**
     * Converts a variable name to a {@link net.sf.saxon.s9api.QName}.
     *
     * @param variableName The variable name to convert.
     * @return the equivalent QName.
     */
    private static net.sf.saxon.s9api.QName toQName(@NonNull final String variableName) {
        return new net.sf.saxon.s9api.QName(variableName);
    }

    /**
     * A rule child that can either be a variable or an assert.
     *
     * @see Variable
     * @see Assert
     */
    @Data
    @NoArgsConstructor
    static class Child {

        /**
         * The number of defined variables when the child is created. It will be useful when executing the child to only declare variables
         * that were already defined (this is a requirement of the XPath processor).
         */
        private int nbVariables = 0;

        /**
         * The compiled, executable XPath expression.
         */
        @NonNull
        private XPathExecutable executable;
    }

    /**
     * A variable.
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    private static final class Variable extends Child {

        /**
         * The variable name.
         */
        @NonNull
        private String name;
    }

    /**
     * An assert.
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    private static final class Assert extends Child {

        /**
         * The XPath expression as a string, stored for the failure message generation.
         */
        @NonNull
        private String xpath;

        /**
         * The assert role.
         */
        @NonNull
        private String role;
    }
}
