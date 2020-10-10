package ch.qligier.schematronix.validation;

import ch.qligier.schematronix.exceptions.SchematronixFastStopException;
import ch.qligier.schematronix.exceptions.SchematronixValidationException;
import lombok.*;
import net.sf.saxon.s9api.*;
import net.sf.saxon.tree.tiny.TinyElementImpl;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A rule of the Schematronix validator. A rule is made of variable definitions and assertions that apply to a list of context items.
 *
 * @author Quentin Ligier
 */
public class ValidationRule {

    /**
     * The ordered list of children (asserts, reports and variables) that make this validation rule.
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
     * The validator configuration to use.
     */
    private final ValidatorConfiguration configuration;

    /**
     * Constructs a new validation rule.
     *
     * @param processor                 The XPath processor.
     * @param namespaces                The list of namespaces defined. The key is the namespace prefix, the value is the namespace URI.
     * @param contextItems              The rule context as a list of evaluated items (nodes, values).
     * @param schematronixDirectoryPath The path of the Schematronix file parent directory or {@code null} if it's not needed.
     * @param patternAssertedNodes      The list of hashes of nodes that have already been asserted in this pattern.
     * @param configuration             The validator configuration to use.
     * @param ruleId                    The rule Id.
     * @param patternId                 The pattern Id.
     * @param contextXpathExpression    The rule context, as an XPath expression.
     */
    public ValidationRule(@NonNull final Processor processor,
                          @NonNull final Map<String, String> namespaces,
                          @NonNull final XdmValue contextItems,
                          final URI schematronixDirectoryPath,
                          @NonNull final List<Integer> patternAssertedNodes,
                          @NonNull final ValidatorConfiguration configuration,
                          final String ruleId,
                          @NonNull final String patternId,
                          @NonNull final String contextXpathExpression) {
        this.contextItems = contextItems;
        this.patternAssertedNodes = patternAssertedNodes;
        this.xpathCompiler = processor.newXPathCompiler();
        for (final Map.Entry<String, String> namespace : namespaces.entrySet()) {
            this.xpathCompiler.declareNamespace(namespace.getKey(), namespace.getValue());
        }
        if (schematronixDirectoryPath != null) {
            this.xpathCompiler.setBaseURI(schematronixDirectoryPath);
        }
        this.configuration = configuration;
        this.id = ruleId;
        this.pattern = patternId;
        this.contextXpathExpression = contextXpathExpression;
    }

    /**
     * Defines a new variable in the rule.
     *
     * @param variableName            The variable name.
     * @param variableXpathExpression The variable value, as an XPath expression.
     * @throws SaxonApiException if an error is encountered when compiling the XPath expression.
     */
    public void addVariable(@NonNull final String variableName,
                            @NonNull final String variableXpathExpression) throws SaxonApiException {
        final Variable variable = new Variable();
        variable.setNbVariables(this.definedVariableNames.size()); // Store the number of variables that were defined before this variable
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
     * @param messageNodes          The report message as a list of nodes.
     * @throws SaxonApiException if an error is encountered when compiling the XPath expression.
     */
    public void addAssert(@NonNull final String assertXpathExpression,
                          @NonNull final String assertRole,
                          @NonNull final List<ValidationRule.MessageNode> messageNodes) throws SaxonApiException {
        final Assert anAssert = new Assert();
        anAssert.setNbVariables(this.definedVariableNames.size()); // Store the number of variables that were defined before this assert
        anAssert.setXpath(assertXpathExpression);
        anAssert.setRole(assertRole);
        anAssert.setExecutable(this.xpathCompiler.compile(assertXpathExpression));
        anAssert.getMessageNodes().addAll(messageNodes);
        this.children.add(anAssert);
    }

    /**
     * Defines a new report in the rule.
     *
     * @param reportXpathExpression The report's XPath expression.
     * @param reportRole            The report's role.
     * @param messageNodes          The report message as a list of nodes.
     * @throws SaxonApiException if an error is encountered when compiling the XPath expression.
     */
    public void addReport(@NonNull final String reportXpathExpression,
                          @NonNull final String reportRole,
                          @NonNull final List<ValidationRule.MessageNode> messageNodes) throws SaxonApiException {
        final Report report = new Report();
        report.setNbVariables(this.definedVariableNames.size()); // Store the number of variables that were defined before this report
        report.setXpath(reportXpathExpression);
        report.setRole(reportRole);
        report.setExecutable(this.xpathCompiler.compile(reportXpathExpression));
        report.getMessageNodes().addAll(messageNodes);
        this.children.add(report);
    }

    /**
     * Executes the validation rule and stores the results in the report. If failFast is set, the validation stops at the first error.
     *
     * @param report        The report that will be updated with the rule validation results.
     * @param configuration The validator configuration to use.
     * @throws SaxonApiException               if the execution of an XPath expression fails.
     * @throws SchematronixValidationException if the validation has encountered an exception.
     * @throws SchematronixFastStopException   if the validation has failed while on fast validation mode.
     */
    public void execute(@NonNull final ValidationReport report,
                        @NonNull final ValidatorConfiguration configuration) throws SaxonApiException, SchematronixValidationException, SchematronixFastStopException {
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
            this.executeSingle(contextNode, report, configuration);
        }
    }

    /**
     * Executes the validation rule on a single item of the context. The validation stops at the first encountered error.
     *
     * @param contextItem   The context item on which to execute the validation rule.
     * @param report        The report that will be updated with the rule validation results.
     * @param configuration The validator configuration to use.
     * @throws SaxonApiException               if an error arises during the XPath expression execution.
     * @throws SchematronixValidationException if the assert fails on the given value.
     * @throws SchematronixFastStopException   if the validation has failed while on fast validation mode.
     */
    private void executeSingle(@NonNull final XdmItem contextItem,
                               @NonNull final ValidationReport report,
                               @NonNull final ValidatorConfiguration configuration) throws SaxonApiException, SchematronixValidationException, SchematronixFastStopException {
        // The list of variables and their evaluated value
        final Map<String, XdmValue> variables = new HashMap<>();

        for (final Child child : this.children) {
            // Create and prepare a new XPath selector tuned for this child
            final XPathSelector selector = child.getExecutable().load();
            selector.setContextItem(contextItem);

            // Declare to the new XPath selector the variables that were defined before the child being processed.
            for (int i = 0; i < child.getNbVariables(); ++i) {
                final String previousVariableName = this.definedVariableNames.get(i);
                selector.setVariable(
                    toQName(previousVariableName),
                    variables.get(previousVariableName)
                );
            }

            if (child instanceof Variable) {
                // The child is a variable declaration, we evaluate and store it
                variables.put(((Variable) child).getName(), selector.evaluate());
            } else if (child instanceof Assert) {
                // If the assert selector doesn't evaluate to 'true', it has failed
                if (!selector.effectiveBooleanValue()) {
                    final Assert failedAssert = (Assert) child;

                    report.getFailedAsserts().add(this.toTriggeredAssertion(
                        contextItem,
                        variables,
                        failedAssert.getRole(),
                        failedAssert.getXpath(),
                        failedAssert.getMessageNodes(),
                        configuration
                    ));
                    if (configuration.isFailFast()) {
                        throw new SchematronixFastStopException();
                    }
                }
            } else {
                // If the report selector evaluates to 'true', it has succeeded
                if (!selector.effectiveBooleanValue()) {
                    final Report successfulReport = (Report) child;
                    report.getSuccessfulReports().add(this.toTriggeredAssertion(
                        contextItem,
                        variables,
                        successfulReport.getRole(),
                        successfulReport.getXpath(),
                        successfulReport.getMessageNodes(),
                        configuration
                    ));
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
     * Constructs a {@link TriggeredAssertion} from the current.
     *
     * @param contextItem   The context item on which the assertion was executed.
     * @param variables     The list of variables and their evaluated value.
     * @param role          The assertion role.
     * @param test          The test that failed, as an XPath expression.
     * @param messageNodes  The assertion message nodes.
     * @param configuration The validator configuration to use.
     * @return the constructed {@link TriggeredAssertion}.
     * @throws SaxonApiException               if the processing of {@code <value-of>} nodes fails.
     * @throws SchematronixValidationException if the processing of {@code <value-of>} nodes fails.
     */
    private TriggeredAssertion toTriggeredAssertion(@NonNull final XdmItem contextItem,
                                                    final Map<String, XdmValue> variables,
                                                    final String role,
                                                    @NonNull final String test,
                                                    @NonNull final List<MessageNode> messageNodes,
                                                    @NonNull final ValidatorConfiguration configuration) throws SaxonApiException, SchematronixValidationException {
        final String message;
        if (configuration.isEvaluateMessageNodes()) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (final MessageNode messageNode : messageNodes) {
                if (messageNode instanceof MessageTextNode) {
                    stringBuilder.append(((MessageTextNode) messageNode).getContent());
                } else if (messageNode instanceof MessageValueOfNode) {
                    stringBuilder.append(
                        this.getSingleValueFromXpathEvaluation(((MessageValueOfNode) messageNode).getSelector(), contextItem, variables)
                    );
                } else if (messageNode instanceof MessageNameNode) {
                    stringBuilder.append(((TinyElementImpl)contextItem.itemAt(0).getUnderlyingValue()).getLocalPart());
                }
            }
            message = stringBuilder.toString();
        } else {
            message = messageNodes.stream().map(MessageNode::toString).collect(Collectors.joining());
        }

        String location = null;
        if (this.configuration.isComputeNodeLocations()) {
            location = this.getSingleValueFromXpathEvaluation("replace(path(), \"Q[{][^}]*[}]\", \"\")", contextItem, variables);
        }

        return new TriggeredAssertion(
            this.id,
            this.pattern,
            role,
            message,
            location,
            this.contextXpathExpression,
            test
        );
    }

    /**
     * Evaluates an XPath expression on a single context item and returns a single result as a string. If zero or multiple results are
     * yielded by the XPath evaluation, an exception is thrown.
     *
     * @param xpathExpression The XPath expression to execute.
     * @param contextItem     The context item on which to execute the XPath expression.
     * @param variables       The list of variables and their evaluated value.
     * @return the result of the XPath evaluation.
     * @throws SaxonApiException               if the XPath cannot be evaluated.
     * @throws SchematronixValidationException if the XPath evaluation does not yield a single value.
     */
    String getSingleValueFromXpathEvaluation(@NonNull final String xpathExpression,
                                             @NonNull final XdmItem contextItem,
                                             final Map<String, XdmValue> variables) throws SaxonApiException, SchematronixValidationException {
        final XPathSelector selector = this.xpathCompiler.compile(xpathExpression).load();
        selector.setContextItem(contextItem);
        for (int i = 0; i < variables.size(); ++i) {
            final String previousVariableName = this.definedVariableNames.get(i);
            selector.setVariable(
                toQName(previousVariableName),
                variables.get(previousVariableName)
            );
        }
        final XdmValue xdmValue = selector.evaluate();
        if (xdmValue.size() != 1) {
            throw new SchematronixValidationException("The XPath selector yielded no value or multiple values");
        }
        return xdmValue.itemAt(0).getStringValue();
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
         * The assert's role.
         */
        @NonNull
        private String role;

        /**
         * The list of message nodes.
         */
        private final List<MessageNode> messageNodes = new ArrayList<>();
    }

    /**
     * A report.
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    private static final class Report extends Child {

        /**
         * The XPath expression as a string, stored for the failure message generation.
         */
        @NonNull
        private String xpath;

        /**
         * The report's role.
         */
        @NonNull
        private String role;

        /**
         * The list of message nodes.
         */
        private final List<MessageNode> messageNodes = new ArrayList<>();
    }

    /**
     * A node in an assertion message.
     */
    @Data
    public abstract static class MessageNode {

        public abstract String toString();
    }

    /**
     * A text node in an assertion message.
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class MessageTextNode extends MessageNode {

        /**
         * The content of the text node.
         */
        private String content;

        public String toString() {
            return this.content;
        }
    }

    /**
     * A {@code value-of} node in an assertion message.
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class MessageValueOfNode extends MessageNode {

        /**
         * The value of the {@code select} attribute.
         */
        private String selector;

        public String toString() {
            return String.format("<value-of select=\"%s\" />", this.selector);
        }
    }

    /**
     * A {@code name} node in an assertion message.
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class MessageNameNode extends MessageNode {

        public String toString() {
            return "<name />";
        }
    }
}
