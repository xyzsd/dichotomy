package net.xyzsd.dichotomy.trying.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExSupplierTest {

    @Test
    void from() {
        assertThrows( NullPointerException.class, () -> ExSupplier.from( null ) );
        assertDoesNotThrow(  () -> ExSupplier.from( () ->  (String) null  ) );
        try {
            final ExSupplier<String> supplier = ExSupplier.from( () -> "Out"  );
            assertEquals( "Out", supplier.get() );
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }
}