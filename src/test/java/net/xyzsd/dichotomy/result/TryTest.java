package net.xyzsd.dichotomy.result;

import net.xyzsd.dichotomy.function.ExFunction;
import net.xyzsd.dichotomy.function.ExSupplier;
import net.xyzsd.dichotomy.function.SpecExSupplier;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TryTest {

    private static final String OK_STRING = "ok!";
    private static final RuntimeException CANNOT_SUPPLY_RT = new RuntimeException( "Failure!" );
    private static final Exception CANNOT_SUPPLY_CHECKED = new Exception( "Failure!" );

    private static final ArithmeticException ARITHMETIC_EXCEPTION = new ArithmeticException("Failure!");

    private static final ExSupplier<String> STRING_NEVERSUPPLIER = () -> {
        throw new IllegalStateException( "!!should not execute!!" );
    };
    private static final ExSupplier<String> STRING_SUPPLIER_OK = () -> OK_STRING;
    private static final ExSupplier<String> STRING_SUPPLIER_RUNTIME = () -> {throw CANNOT_SUPPLY_RT;};
    private static final ExSupplier<String> STRING_SUPPLIER_CHECKED = () -> {throw CANNOT_SUPPLY_CHECKED;};

    private static final SpecExSupplier<String, RuntimeException> X_SPEC_STRING_SUPPLIER_OK = () -> OK_STRING;
    private static final SpecExSupplier<String, RuntimeException> X_SPEC_STRING_SUPPLIER_RUNTIME = () -> {
        throw CANNOT_SUPPLY_RT;
    };
    private static final SpecExSupplier<String, RuntimeException> X_SPEC_STRING_SUPPLIER_RUNTIME_AE = () -> {
        throw ARITHMETIC_EXCEPTION;
    };
    private static final SpecExSupplier<String, ArithmeticException> X_SPEC_STRING_SUPPLIER_AE_AE = () -> {
        throw ARITHMETIC_EXCEPTION;
    };
    private static final SpecExSupplier<String, ArithmeticException> X_SPEC_STRING_SUPPLIER_AE_RT = () -> {
        throw CANNOT_SUPPLY_RT;
    };
    private static final SpecExSupplier<String, Exception> X_SPEC_STRING_SUPPLIER_CHECKED = () -> {
        throw CANNOT_SUPPLY_CHECKED;
    };

    private static final IllegalArgumentException IAE_NEGATIVE = new IllegalArgumentException("negative argument!");
    private static final ExFunction<Integer, String> XFN = i -> {
        if(i >= 0) {
            return String.valueOf(100*i );
        } else {
            throw IAE_NEGATIVE;
        }
    };





    @Test
    void ofSupplier() {
        assertEquals( Result.ofOK( OK_STRING ), Try.of( STRING_SUPPLIER_OK ) );
        assertEquals( Result.ofErr( CANNOT_SUPPLY_RT ), Try.of( STRING_SUPPLIER_RUNTIME ) );
        assertEquals( Result.ofErr( CANNOT_SUPPLY_CHECKED ), Try.of( STRING_SUPPLIER_CHECKED ) );
    }

    @Test
    void fromSupplier() {
        assertDoesNotThrow( () -> Try.from( STRING_NEVERSUPPLIER ) );

        Supplier<Result<String, Exception>> supplier;

        supplier = Try.from( STRING_SUPPLIER_OK );
        assertNotNull( supplier );
        assertEquals( Result.ofOK( OK_STRING ), supplier.get() );

        supplier = Try.from( STRING_SUPPLIER_RUNTIME );
        assertNotNull( supplier );
        assertEquals( Result.ofErr( CANNOT_SUPPLY_RT ), supplier.get() );

        supplier = Try.from( STRING_SUPPLIER_CHECKED );
        assertNotNull( supplier );
        assertEquals( Result.ofErr( CANNOT_SUPPLY_CHECKED ), supplier.get() );
    }


    @Test
    void ofSpecExSupplier() {
        assertEquals( Result.ofOK( OK_STRING ), Try.of( X_SPEC_STRING_SUPPLIER_OK, RuntimeException.class ) );

        assertEquals( Result.ofErr( CANNOT_SUPPLY_RT ), Try.of( X_SPEC_STRING_SUPPLIER_RUNTIME, RuntimeException.class ) );
        assertEquals( Result.ofErr( ARITHMETIC_EXCEPTION ), Try.of( X_SPEC_STRING_SUPPLIER_AE_AE, ArithmeticException.class ) );

        // X_SPEC_STRING_SUPPLIER_RUNTIME_AE: We will catch (into a Result) any RuntimeException, which includes ArithmeticExceptions
        assertEquals( Result.ofErr( ARITHMETIC_EXCEPTION ), Try.of( X_SPEC_STRING_SUPPLIER_RUNTIME_AE, RuntimeException.class ) );

        // X_SPEC_STRING_SUPPLIER_AE_RT: we will handle ArithmeticExceptions, but any other RuntimeExceptions are not allowed and will throw
        assertThrows( RuntimeException.class, () -> Try.of( X_SPEC_STRING_SUPPLIER_AE_RT, ArithmeticException.class ) );

        // the following line does not compile, and it should not compile!
        //assertEquals( Result.ofErr( CANNOT_SUPPLY_RT ), Try.of( X_SPEC_STRING_SUPPLIER_CHECKED, RuntimeException.class ) );
    }


    @Test
    void ofFn() {
        assertEquals(Result.ofOK("900"), Try.of(9, XFN) );
        assertEquals(Result.ofErr( IAE_NEGATIVE ), Try.of(-9, XFN) );
    }

    @Test
    void fromFn() {
        assertDoesNotThrow( () -> Try.from( in -> { throw new IllegalStateException(); } ) );

        final Function<Integer, Result<String, Exception>> fn = Try.from( XFN );
        assertEquals(Result.ofOK("900"), fn.apply(9) );
        assertEquals(Result.ofErr( IAE_NEGATIVE ), fn.apply(-9) );
    }
}