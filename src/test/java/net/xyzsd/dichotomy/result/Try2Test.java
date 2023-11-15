package net.xyzsd.dichotomy.result;

import net.xyzsd.dichotomy.trying.Try2;
import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import net.xyzsd.dichotomy.trying.function.SpecExSupplier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class Try2Test {

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
    void basic() {
        Try2<String, IOException> try2s = new Try2.OK<>("An OK String!");
        Try2<String, IOException> try2ec = new Try2.Err<>( new IOException("an IO exception") );
        Try2<String, RuntimeException> try2er = new Try2.Err<>( new RuntimeException("a runtime exception") );

        assertThrows(IOException.class, try2ec::orThrow);
        assertThrows(RuntimeException.class, try2er::orThrow);

    }



    // TODO: everything below here needs to be redone for Try2

    @Test
    void ofSupplier() {
        assertEquals( new Try2.OK<>( OK_STRING ), Try2.of( STRING_SUPPLIER_OK ) );
        assertEquals( new Try2.Err<>( CANNOT_SUPPLY_RT ), Try2.of( STRING_SUPPLIER_RUNTIME ) );
        assertEquals( new Try2.Err<>( CANNOT_SUPPLY_CHECKED ), Try2.of( STRING_SUPPLIER_CHECKED ) );
    }

    @Test
    void fromSupplier() {
        assertDoesNotThrow( () -> Try2.from( STRING_NEVERSUPPLIER ) );

        Supplier<Try2<String, Exception>> supplier;

        supplier = Try2.from( STRING_SUPPLIER_OK );
        assertNotNull( supplier );
        assertEquals( Try2.ofOK( OK_STRING ), supplier.get() );

        supplier = Try2.from( STRING_SUPPLIER_RUNTIME );
        assertNotNull( supplier );
        assertEquals( Try2.ofErr( CANNOT_SUPPLY_RT ), supplier.get() );

        supplier = Try2.from( STRING_SUPPLIER_CHECKED );
        assertNotNull( supplier );
        assertEquals( Try2.ofErr( CANNOT_SUPPLY_CHECKED ), supplier.get() );
    }


    @Test
    void ofSpecExSupplier() {
        assertEquals( Try2.ofOK( OK_STRING ), Try2.of( X_SPEC_STRING_SUPPLIER_OK, RuntimeException.class ) );

        assertEquals( Try2.ofErr( CANNOT_SUPPLY_RT ), Try2.of( X_SPEC_STRING_SUPPLIER_RUNTIME, RuntimeException.class ) );
        assertEquals( Try2.ofErr( ARITHMETIC_EXCEPTION ), Try2.of( X_SPEC_STRING_SUPPLIER_AE_AE, ArithmeticException.class ) );

        // X_SPEC_STRING_SUPPLIER_RUNTIME_AE: We will catch (into a Result) any RuntimeException, which includes ArithmeticExceptions
        assertEquals( Try2.ofErr( ARITHMETIC_EXCEPTION ), Try2.of( X_SPEC_STRING_SUPPLIER_RUNTIME_AE, RuntimeException.class ) );

        // X_SPEC_STRING_SUPPLIER_AE_RT: we will handle ArithmeticExceptions, but any other RuntimeExceptions are not allowed and will throw
        assertThrows( RuntimeException.class, () -> Try2.of( X_SPEC_STRING_SUPPLIER_AE_RT, ArithmeticException.class ) );

        // the following line does not compile, and it should not compile!
        //assertEquals( Result.ofErr( CANNOT_SUPPLY_RT ), Try2.of( X_SPEC_STRING_SUPPLIER_CHECKED, RuntimeException.class ) );
    }


    @Test
    void ofFn() {
        assertEquals(Try2.ofOK("900"), Try2.of(9, XFN) );
        assertEquals(Try2.ofErr( IAE_NEGATIVE ), Try2.of(-9, XFN) );
    }

    @Test
    void fromFn() {
        assertDoesNotThrow( () -> Try2.from( in -> { throw new IllegalStateException(); } ) );

        final Function<Integer, Try2<String, Exception>> fn = Try2.from( XFN );
        assertEquals(Try2.ofOK("900"), fn.apply(9) );
        assertEquals(Try2.ofErr( IAE_NEGATIVE ), fn.apply(-9) );
    }
}