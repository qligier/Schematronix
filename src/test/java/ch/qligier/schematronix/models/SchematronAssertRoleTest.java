package ch.qligier.schematronix.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The test bed for {@link SchematronAssertRole}.
 *
 * @author Quentin Ligier
 * @version 0.1.0
 */
class SchematronAssertRoleTest {

    /**
     * Ensures that it has a getter.
     */
    @Test
    @DisplayName("Has a getter")
    void testHasGetter() {
        assertEquals("error", SchematronAssertRole.ERROR.getName());
    }

    /**
     * Ensures that a value can be found by its name.
     */
    @Test
    @DisplayName("Can find value by name")
    void testCanFind() {
        assertNotNull(SchematronAssertRole.getByValue("warning"));
    }

    /**
     * Ensures that it throws an exception when requesting an undefined name.
     */
    @Test
    @DisplayName("Throw on undefined name")
    void testThrowOnUndefinedName() {
        assertThrows(IllegalArgumentException.class, () -> SchematronAssertRole.getByValue("debug"));
    }
}
