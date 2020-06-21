package ch.qligier.schematronix.models;

/**
 * An abstract class that represents a child of a Schematron rule.
 *
 * @author Quentin Ligier
 * @version 0.1.0
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
