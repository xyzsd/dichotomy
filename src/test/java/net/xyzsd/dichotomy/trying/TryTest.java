package net.xyzsd.dichotomy.trying;

import net.xyzsd.dichotomy.Empty;

import static net.xyzsd.dichotomy.TestUtils.*;
import static net.xyzsd.dichotomy.trying.Try.OK;
import static net.xyzsd.dichotomy.trying.Try.Err;
import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import net.xyzsd.dichotomy.trying.function.SpecExSupplier;
import org.junit.jupiter.api.Test;

import net.xyzsd.dichotomy.TestUtils.SingleUseConsumer;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TryTest {


    // standard test values
    static final String OK_VALUE = "[ok]";
    static final String ERR_VALUE_MESSAGE = "*no such element*";
    static final RuntimeException ERR_VALUE = new NoSuchElementException( ERR_VALUE_MESSAGE );


    // preface with "TEST" to avoid confusion between implementation classes
    static final Try<String, RuntimeException> TEST_OK = Try.ofOK( OK_VALUE );
    static final Try<String, RuntimeException> TEST_ERR = Try.ofErr( ERR_VALUE );


    // standard mapping functions for testing
    static final String fnOK_VAL = "*MAPPED_STRING";
    static final RuntimeException fnERR_VAL = new NoSuchElementException( "*MAPPED_" + ERR_VALUE_MESSAGE );

    // mapping functions
    static final Function<String, String> fnOK = s -> fnOK_VAL;
    static final Function<RuntimeException, RuntimeException> fnERR2ERR = ex -> fnERR_VAL;
    static final Function<RuntimeException, String> fnERR2STR = ex -> fnERR_VAL.getMessage();

    static final String AEX_MESSAGE = "mapped_via_fn";
    static final RuntimeException AEX = new ArithmeticException(AEX_MESSAGE);
    static final Function<RuntimeException, RuntimeException> fnEX2AEX = (ex) -> AEX;
    static final Function<String, RuntimeException> fnSTR2AEX = s -> AEX;


    // flat-mapping functions
    static final Function<String, Try<String, RuntimeException>> fnOK_FLAT = (x) -> Try.ofOK( fnOK_VAL );
    static final Function<RuntimeException, Try<String, RuntimeException>> fnERR_FLAT = (e) -> Try.ofErr( fnERR_VAL );


    private static final RuntimeException CANNOT_SUPPLY_RT = new RuntimeException( "Failure!" );
    private static final Exception CANNOT_SUPPLY_CHECKED = new Exception( "Failure!" );

    private static final ArithmeticException ARITHMETIC_EXCEPTION = new ArithmeticException("Failure!");


    private static final ExSupplier<String> STRING_SUPPLIER_OK = () -> OK_VALUE;
    private static final ExSupplier<String> STRING_SUPPLIER_RUNTIME = () -> {throw CANNOT_SUPPLY_RT;};
    private static final ExSupplier<String> STRING_SUPPLIER_CHECKED = () -> {throw CANNOT_SUPPLY_CHECKED;};

    private static final SpecExSupplier<String, RuntimeException> X_SPEC_STRING_SUPPLIER_OK = () -> OK_VALUE;
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
        Try<String, IOException> try2s = new Try.OK<>("An OK String!");
        Try<String, IOException> try2ec = new Try.Err<>( new IOException("an IO exception") );
        Try<String, RuntimeException> try2er = new Try.Err<>( new RuntimeException("a runtime exception") );

        assertThrows(IOException.class, try2ec::expect );
        assertThrows(RuntimeException.class, try2er::expect );

    }



    // TODO: everything below here needs to be redone for Try2

    @Test
    void ofSupplier() {
        assertEquals( new Try.OK<>( OK_VALUE ), Try.of( STRING_SUPPLIER_OK ) );
        assertEquals( new Try.Err<>( CANNOT_SUPPLY_RT ), Try.of( STRING_SUPPLIER_RUNTIME ) );
        assertEquals( new Try.Err<>( CANNOT_SUPPLY_CHECKED ), Try.of( STRING_SUPPLIER_CHECKED ) );
    }

    @Test
    void fromSupplier() {
        assertDoesNotThrow( () -> Try.from( neverSupplier() ) );

        Supplier<Try<String, Exception>> supplier;

        supplier = Try.from( STRING_SUPPLIER_OK );
        assertNotNull( supplier );
        assertEquals( Try.ofOK( OK_VALUE ), supplier.get() );

        supplier = Try.from( STRING_SUPPLIER_RUNTIME );
        assertNotNull( supplier );
        assertEquals( Try.ofErr( CANNOT_SUPPLY_RT ), supplier.get() );

        supplier = Try.from( STRING_SUPPLIER_CHECKED );
        assertNotNull( supplier );
        assertEquals( Try.ofErr( CANNOT_SUPPLY_CHECKED ), supplier.get() );
    }


    @Test
    void ofSpecExSupplier() {
        assertEquals( Try.ofOK( OK_VALUE ), Try.of( X_SPEC_STRING_SUPPLIER_OK, RuntimeException.class ) );

        assertEquals( Try.ofErr( CANNOT_SUPPLY_RT ), Try.of( X_SPEC_STRING_SUPPLIER_RUNTIME, RuntimeException.class ) );
        assertEquals( Try.ofErr( ARITHMETIC_EXCEPTION ), Try.of( X_SPEC_STRING_SUPPLIER_AE_AE, ArithmeticException.class ) );

        // X_SPEC_STRING_SUPPLIER_RUNTIME_AE: We will catch (into a Result) any RuntimeException, which includes ArithmeticExceptions
        assertEquals( Try.ofErr( ARITHMETIC_EXCEPTION ), Try.of( X_SPEC_STRING_SUPPLIER_RUNTIME_AE, RuntimeException.class ) );

        // X_SPEC_STRING_SUPPLIER_AE_RT: we will handle ArithmeticExceptions, but any other RuntimeExceptions are not allowed and will throw
        assertThrows( RuntimeException.class, () -> Try.of( X_SPEC_STRING_SUPPLIER_AE_RT, ArithmeticException.class ) );

        // the following line does not compile, and it should not compile!
        //assertEquals( Try2.ofErr( CANNOT_SUPPLY_RT ), Try2.of( X_SPEC_STRING_SUPPLIER_CHECKED, RuntimeException.class ) );
    }


    @Test
    void ofFn() {
        assertEquals( Try.ofOK("900"), Try.of(9, XFN) );
        assertEquals( Try.ofErr( IAE_NEGATIVE ), Try.of(-9, XFN) );
    }

    @Test
    void fromFn() {
        assertDoesNotThrow( () -> Try.from( in -> { throw new IllegalStateException(); } ) );

        final Function<Integer, Try<String, Exception>> fn = Try.from( XFN );
        assertEquals( Try.ofOK("900"), fn.apply(9) );
        assertEquals( Try.ofErr( IAE_NEGATIVE ), fn.apply(-9) );
    }




    /////////////////////////////////////////////////////////////////////////
    // test methods similar to Result
    /////////////////////////////////////////////////////////////////////////



    @Test
    void ofOK() {
        // testing the OK_VALUE created as a static
        assertEquals( OK.class, TEST_OK.getClass() );
        assertEquals( OK_VALUE, ((OK<String, RuntimeException>) TEST_OK).get() );
        // ensure null values disallowed
        assertThrows( NullPointerException.class, () -> Try.ofOK( null ) );
    }

    @Test
    void ofOK_NONE() {
        final Try<Empty, RuntimeException> result = Try.ofOK();
        assertEquals( Try.OK.class, result.getClass() );
        assertEquals( Empty.getInstance(), ((Try.OK<Empty, RuntimeException>) result).get() );
    }




    @Test
    void ofErr() {
        Try<String, RuntimeException> result = Try.ofErr( ERR_VALUE );
        assertEquals( Err.class, result.getClass() );
        assertEquals( ERR_VALUE, ((Err<String, RuntimeException>) result).get() );
        assertEquals( result, TEST_ERR );
        assertThrows( NullPointerException.class, () -> {Try.ofErr( null );} );
    }


    @Test
    void ok() {
        Try<String, RuntimeException> resultERR = Try.ofErr( ERR_VALUE );
        assertTrue( resultERR.ok().isEmpty() );

        Try<String, RuntimeException> resultOK = Try.ofOK( OK_VALUE );
        assertTrue( resultOK.ok().isPresent() );
        assertEquals( resultOK.ok().get(),  OK_VALUE );
    }

    @Test
    void err() {
        Try<String, RuntimeException> resultERR = Try.ofErr( ERR_VALUE );
        assertTrue( resultERR.err().isPresent() );
        assertEquals( resultERR.err().get(),  ERR_VALUE );

        Try<String, RuntimeException> resultOK = Try.ofOK( OK_VALUE );
        assertTrue( resultOK.err().isEmpty() );
    }



    @Test
    void biMatch() {
        {
            SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
            SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();
            //
            TEST_OK.biMatch( okConsumer, errConsumer );
            assertTrue( okConsumer.usedJustOnce() );
            assertTrue( errConsumer.neverUsed() );
        }

        {
            SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
            SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();
            //
            TEST_ERR.biMatch( okConsumer, errConsumer );
            assertTrue( okConsumer.neverUsed() );
            assertTrue( errConsumer.usedJustOnce() );
        }
    }

    @Test
    void biMap() {
        Try<String, RuntimeException> mapped = TEST_OK.biMap( fnOK, fnEX2AEX );
        assertTrue( mapped.contains( fnOK_VAL ) );

        mapped = TEST_ERR.biMap( fnOK, fnEX2AEX );
        assertTrue( mapped.containsErr( AEX ) );

        // functions must not return null
        assertThrows( NullPointerException.class, () -> {
            TEST_OK.biMap( x -> null, fnEX2AEX );
        } );

        assertThrows( NullPointerException.class, () -> {
            TEST_ERR.biMap( fnOK, x -> null );
        } );

        // unused side must not apply function
        assertDoesNotThrow( () -> TEST_OK.biMap( fnOK, neverFunction() ) );
        assertDoesNotThrow( () -> TEST_ERR.biMap( neverFunction(), fnEX2AEX ) );
    }

    @Test
    void biFlatMap() {
        assertEquals(
                Try.ofOK( fnOK_VAL ),
                TEST_OK.biFlatMap( fnOK_FLAT, fnERR_FLAT )
        );

        assertEquals(
                Try.ofErr( fnERR_VAL ),
                TEST_ERR.biFlatMap( fnOK_FLAT, fnERR_FLAT )
        );

        assertThrows( NullPointerException.class, () -> {
            TEST_OK.biFlatMap( x -> null, fnERR_FLAT );
        } );

        assertThrows( NullPointerException.class, () -> {
            TEST_ERR.biFlatMap( fnOK_FLAT, x -> null );
        } );

        assertDoesNotThrow( () -> TEST_OK.biFlatMap( fnOK_FLAT, neverFunction() ) );
        assertDoesNotThrow( () -> TEST_ERR.biFlatMap( neverFunction(), fnERR_FLAT ) );
    }

    @Test
    void fold() {
        assertEquals( fnOK_VAL, TEST_OK.fold( fnOK, neverFunction() ) );
        assertEquals( fnERR_VAL, TEST_ERR.fold( neverFunction(), fnERR2ERR ) );

        // null-returning function test
        assertThrows( NullPointerException.class, () -> {
            TEST_OK.fold( x -> null, neverFunction() );
        } );

        assertThrows( NullPointerException.class, () -> {
            TEST_ERR.fold( neverFunction(), x -> null );
        } );
    }

    @Test
    void stream() {
        assertEquals( 1, TEST_OK.stream().count() );
        assertTrue( TEST_OK.stream().allMatch( OK_VALUE::equals ) );
        assertEquals( 0, TEST_ERR.stream().count() );
    }

    @Test
    void filter() {
        // Err->Err
        assertEquals( TEST_ERR, TEST_ERR.filter( x -> true, neverFunction() ) );
        assertEquals( TEST_ERR, TEST_ERR.filter( x -> false, neverFunction() ) );

        // OK->OK if predicate matches
        assertEquals( TEST_OK, TEST_OK.filter( x -> true, neverFunction() ) );

        // OK->Err if predicate doesn't match
        assertEquals( Try.ofErr( AEX ), TEST_OK.filter( x -> false, fnSTR2AEX ) );
    }

    @Test
    void match() {
        SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
        TEST_OK.match( okConsumer );
        assertTrue( okConsumer.usedJustOnce() );

        SingleUseConsumer<String> errConsumer = new SingleUseConsumer<>();
        TEST_ERR.match( errConsumer );
        assertTrue( errConsumer.neverUsed() );
    }

    @Test
    void map() {
        assertEquals( Try.ofOK( fnOK_VAL ), TEST_OK.map( fnOK ) );
        assertEquals( TEST_ERR, TEST_ERR.map( fnOK ) );
        assertDoesNotThrow( () -> {TEST_ERR.map( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> TEST_OK.map( x -> null ) );
    }

    @Test
    void flatMap() {
        assertEquals( Try.ofOK( fnOK_VAL ), TEST_OK.flatMap( fnOK_FLAT ) );
        assertEquals( TEST_ERR, TEST_ERR.flatMap( fnOK_FLAT ) );
        assertDoesNotThrow( () -> {TEST_ERR.flatMap( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> TEST_OK.flatMap( x -> null ) );
    }

    @Test
    void matches() {
        assertTrue( TEST_OK.ifPredicate( x -> true ) );
        assertFalse( TEST_OK.ifPredicate( x -> false ) );

        assertFalse( TEST_ERR.ifPredicate( x -> true ) );
        assertFalse( TEST_ERR.ifPredicate( x -> false ) );
    }

    @Test
    void contains() {
        assertTrue( TEST_OK.contains( OK_VALUE ) );
        assertFalse( TEST_OK.contains( "" ) );
    }

    @Test
    void orElse() {
        assertEquals( OK_VALUE, TEST_OK.orElse( "orElse-alternate" ) );
        assertEquals( "orElse-alternate", TEST_ERR.orElse( "orElse-alternate" ) );
    }

    @Test
    void orElseGet() {
        assertEquals( OK_VALUE, TEST_OK.orElse( neverSupplier() ) );
        assertEquals( "orElse-alternate", TEST_ERR.orElse( () -> "orElse-alternate" ) );
    }

    @Test
    void recover() {
        assertEquals( OK_VALUE, TEST_OK.recover( neverFunction() ) );
        assertEquals( fnERR_VAL.getMessage(), TEST_ERR.recover( fnERR2STR ) );
    }

    @Test
    void streamErr() {
        assertEquals( 1, TEST_ERR.streamErr().count() );
        assertTrue( TEST_ERR.streamErr().allMatch( ERR_VALUE::equals ) );
        assertEquals( 0, TEST_OK.streamErr().count() );
    }

    @Test
    void matchErr() {
        SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();
        TEST_ERR.matchErr( errConsumer );
        assertTrue( errConsumer.usedJustOnce() );

        SingleUseConsumer<RuntimeException> okConsumer = new SingleUseConsumer<>();
        TEST_OK.matchErr( okConsumer );
        assertTrue( okConsumer.neverUsed() );
    }

    @Test
    void mapErr() {
        assertEquals( Try.ofErr( fnERR_VAL ), TEST_ERR.mapErr( fnERR2ERR ) );
        assertEquals( TEST_OK, TEST_OK.mapErr( fnERR2ERR ) );
        assertDoesNotThrow( () -> {TEST_OK.mapErr( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> TEST_ERR.mapErr( x -> null ) );
    }

    @Test
    void flatMapErr() {
        assertEquals( Try.ofErr( fnERR_VAL ), TEST_ERR.flatMapErr( fnERR_FLAT ) );
        assertEquals( TEST_OK, TEST_OK.flatMapErr( fnERR_FLAT ) );
        assertDoesNotThrow( () -> {TEST_OK.flatMapErr( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> TEST_ERR.flatMapErr( x -> null ) );
    }

    @Test
    void matchesErr() {
        assertTrue( TEST_ERR.ifPredicateErr( x -> true ) );
        assertFalse( TEST_ERR.ifPredicateErr( x -> false ) );

        assertFalse( TEST_OK.ifPredicateErr( x -> true ) );
        assertFalse( TEST_OK.ifPredicateErr( x -> false ) );
    }

    @Test
    void containsErr() {
        assertTrue( TEST_ERR.containsErr( ERR_VALUE ) );
        assertFalse( TEST_ERR.containsErr( new RuntimeException( "?" ) ) );
    }

    @Test
    void orElseErr() {
        assertEquals( ERR_VALUE, TEST_ERR.orElseErr( new RuntimeException( "*invalid*" ) ) );
        assertEquals( fnERR_VAL, TEST_OK.orElseErr( fnERR_VAL ) );
    }

    @Test
    void orElseErr_Supplier() {
        assertEquals( ERR_VALUE, TEST_ERR.orElseErr( neverSupplier() ) );
        assertEquals( fnERR_VAL, TEST_OK.orElseErr( () -> fnERR_VAL ) );
    }

    @Test
    void forfeit() {
        assertEquals( ERR_VALUE, TEST_ERR.forfeit( neverFunction() ) );
        assertEquals( AEX, TEST_OK.forfeit( fnSTR2AEX ) );
    }

    @Test
    void and() {
        assertEquals( TEST_ERR, TEST_ERR.and( Try.ofOK( "nextResult" ) ) );
        assertEquals( Try.ofOK( "nextResult" ), TEST_OK.and( Try.ofOK( "nextResult" ) ) );
    }

    @Test
    void testAnd() {
        assertEquals( TEST_ERR, TEST_ERR.and( neverSupplier() ) );

        assertEquals(
                Try.ofOK( "nextResult" ),
                TEST_OK.and( () -> Try.ofOK( "nextResult" ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> { TEST_OK.and( () -> null ); }
        );
    }

    @Test
    void or() {
        assertEquals( TEST_OK, TEST_OK.or( Try.ofOK( "nextResult" ) ) );
        assertEquals(
                Try.ofErr( AEX ),
                TEST_ERR.or( Try.ofErr( AEX ) )
        );
    }

    @Test
    void testOr() {
        assertEquals( TEST_OK, TEST_OK.or( neverSupplier() ) );

        assertEquals(
                Try.ofErr( AEX ),
                TEST_ERR.or( () -> Try.ofErr( AEX ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> { TEST_ERR.or( () -> null ); }
        );
    }


    @Test
    void orThrow() {
        final String s = assertDoesNotThrow( () -> TEST_OK.getOrThrow( neverFunction() ) );
        assertEquals( OK_VALUE, s );
        assertThrows(
                IOException.class,
                () -> TEST_ERR.getOrThrow( IOException::new )
        );
        assertThrows(
                RuntimeException.class,
                () -> TEST_ERR.getOrThrow( RuntimeException::new )
        );

        // but also see this
        final String MESSAGE = "My Error Message";
        final Try<?,RuntimeException> result = Try.ofErr( new RuntimeException(MESSAGE) );
        assertThrows(
                IOException.class,
                () -> result.getOrThrow( IOException::new )
        );

        try {
            result.getOrThrow( IOException::new );
        } catch(IOException e) {
            assertEquals( MESSAGE, e.getCause().getMessage() );
        }

        final Try<?,IOException> resultChecked = Try.ofErr( new IOException(MESSAGE) );

        // resultChecked.expect();      // must be wrapped in a try
        try {
            resultChecked.expect();
        } catch (IOException e) {
            assertEquals( MESSAGE, e.getMessage() );
        }


    }


    @Test
    void expect() {
        assertEquals( OK_VALUE, TEST_OK.expect() );
        assertDoesNotThrow( TEST_OK::expect );

        assertThrows( RuntimeException.class,
                        TEST_ERR::expect );

        final String MESSAGE = "My Error Message";
        final Try<?,IOException> resultChecked = Try.ofErr( new IOException(MESSAGE) );

    }

    @Test
    void monadicLaws() {
        // NOTE: the terms 'left identity' and 'right identity' below, are general terms for monad properties,
        // for ease of applying functions, we will use a new results type: Try2<String, Integer>
        final String MTEST_OK_VALUE = "monadic-laws-test-ok-value";
        final RuntimeException MTEST_ERR_VALUE = new RuntimeException("666"); // because why not
        final Try<String,RuntimeException> MTEST_OK = Try.ofOK(MTEST_OK_VALUE);
        final Try<String,RuntimeException> MTEST_ERR = Try.ofErr( MTEST_ERR_VALUE );

        // FIRST LAW: LEFT IDENTITY
        // Simply: data is wrapped, not manipulated/changed.
        //
        // Given a Result with a value in it and a function that takes the same type of value and
        // returns the same type of Result, then applying the function to the value should
        // be equivalent to flatMapping on the Try2.
        //
        // given: the following function:
        final Function<String, Try<String,RuntimeException>> concat = s -> Try.ofOK(s+s);
        // then, the following should be equivalent:
        assertEquals(
                concat.apply(MTEST_OK_VALUE),
                MTEST_OK.flatMap( concat )
        );

        // similarly, for Err:
        final Function<RuntimeException, Try<String,RuntimeException>> errConcat = e ->
            Try.ofErr(new RuntimeException(e.getMessage()+e.getMessage()));

        final Try<String, RuntimeException> op1 = errConcat.apply( MTEST_ERR_VALUE );
        final Try<String, RuntimeException> op2 = MTEST_ERR.flatMapErr( errConcat );
        final String op1msg = op1.err().get().getMessage();
        final String op2msg = op2.err().get().getMessage();

        assertEquals( op1msg, op2msg );


        // SECOND LAW: RIGHT IDENTITY
        // Simply: data is wrapped, not manipulated/changed.
        //
        assertEquals(
                MTEST_OK,
                MTEST_OK.flatMap( Try::ofOK )
        );

        assertEquals(
                MTEST_OK,
                MTEST_OK.flatMapErr( Try::ofErr )
        );

        assertEquals(
                MTEST_ERR,
                MTEST_ERR.flatMap( Try::ofOK )
        );

        assertEquals(
                MTEST_ERR,
                MTEST_ERR.flatMapErr( Try::ofErr )
        );


        // THIRD LAW: ASSOCIATIVITY
        // flatmap nesting order should not matter
        // given: functions from above (for OK and Err monads)
        // given: the following functions:
        final Function<String, Try<String,RuntimeException>> delim = s -> Try.ofOK(s+","+s);
        final Function<RuntimeException, Try<String,RuntimeException>> ellipse = e -> Try.ofErr(new RuntimeException(e.getMessage()+"..."));
        // then:
        assertEquals(
                MTEST_OK.flatMap( delim ).flatMap( concat ),
                MTEST_OK.flatMap( s -> delim.apply( s ).flatMap( concat ) )
        );


        final Try<String, RuntimeException> errOp1 = MTEST_ERR.flatMapErr( ellipse ).flatMapErr( errConcat );
        final Try<String, RuntimeException> errOp2 = MTEST_ERR.flatMapErr( s -> ellipse.apply( s ).flatMapErr( errConcat ));
        final String errOp1Msg = op1.err().get().getMessage();
        final String errOp2Msg = op2.err().get().getMessage();

        assertEquals( errOp1Msg, errOp2Msg );

    }







}