package ch.qligier.schematronix.models;

/**
 * An abstract class that represents a child of a Schematron rule.
 *
 * @author Quentin Ligier
 * @see SchematronAssert
 * @see SchematronExtends
 * @see SchematronLet
 */
public abstract class SchematronRuleChild implements Cloneable {

    /**
     * Clones the current object.
     *
     * @return the cloned object.
     */
    public abstract SchematronRuleChild clone();
}
