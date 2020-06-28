package ch.qligier.schematronix.models;

import lombok.Getter;
import lombok.NonNull;

/**
 * The list of Schematron assert roles.
 *
 * @author Quentin Ligier
 */
@Getter
public enum SchematronAssertRole {

    FATAL("fatal"),
    ERROR("error"),
    WARN("warning"),
    INFORMATION("info");

    /**
     * The role's name.
     */
    final String name;

    /**
     * Constructor.
     *
     * @param name The role's name.
     */
    SchematronAssertRole(final String name) {
        this.name = name;
    }

    /**
     * Finds an enum element by its name, ignoring the case.
     *
     * @param name The enum element's name.
     * @return the found enum element.
     * @throws IllegalArgumentException if the name is not found in the enum.
     */
    public static SchematronAssertRole getByValue(@NonNull final String name) {
        for (final SchematronAssertRole role : values()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("The given value is not an SchematronAssertRole value");
    }
}
