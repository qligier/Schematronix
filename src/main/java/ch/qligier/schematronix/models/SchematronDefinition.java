package ch.qligier.schematronix.models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NonNull;

import java.util.*;

/**
 * Data structure of a parsed Schematron file.
 *
 * @author Quentin Ligier
 */
@Data
public class SchematronDefinition {

    /**
     * The list of defined rules.
     */
    private final Map<String, SchematronRule> definedRules = new HashMap<>();

    /**
     * The list of IDs of enabled pattern. The order in which the patterns are defined in the main phase is kept.
     */
    private final Set<String> enabledPatterns = new LinkedHashSet<>();

    /**
     * The list of IDs of enabled rule IDs.
     */
    private final Set<String> enabledRules = new LinkedHashSet<>();

    /**
     * The map that links patterns to their rules. The key is the pattern ID, the value is the list of child rule IDs.
     */
    private final Map<String, List<String>> rulesPerPattern = new HashMap<>();

    /**
     * The map of defined namespaces. The key is the namespace prefix, the value is the namespace URI.
     */
    private final Map<String, String> namespaces = new HashMap<>();

    /**
     * The Schematron instance title.
     */
    private String title = "";

    /**
     * The query binding.
     */
    private String queryBinding;

    /**
     * Gets a rule by its ID and resolve it (i.e. all extended rules are aggregated into the given rule).
     *
     * @param ruleId The ID of the rule to get.
     * @return an instance of rule that is self-contained (it does not extend other rules anymore).
     */
    public SchematronRule getResolvedRule(@NonNull final String ruleId) {
        final SchematronRule rule = this.getDefinedRules().get(ruleId).clone();
        rule.setChildren(this.resolveExtendedChildren(rule));
        return rule;
    }

    /**
     * Resolves the extended rules from a given rule. The function is recursive and will call itself as many times as necessary to fully
     * resolve all extended rules. Extended rules are added at the position the 'extend' tag was encountered to preserve the Schematron
     * order.
     *
     * @param extendedRule The instance of the rule to resolve.
     * @return an instance of the rule that is self-contained (it does not extend other rules anymore).
     */
    private List<SchematronRuleChild> resolveExtendedChildren(@NonNull final SchematronRule extendedRule) {
        final List<SchematronRuleChild> children = new ArrayList<>(extendedRule.getChildren());
        if (children.stream().anyMatch(child -> child instanceof SchematronExtends)) {
            final int index = Iterables.indexOf(children, child -> child instanceof SchematronExtends);
            final SchematronExtends extend = (SchematronExtends) children.get(index);
            children.remove(index);
            final List<SchematronRuleChild> newChildren = resolveExtendedChildren(this.getDefinedRules().get(extend.getExtendsRuleId()));
            Lists.reverse(newChildren).forEach(newChild -> children.add(index, newChild));
        }
        return children;
    }
}
