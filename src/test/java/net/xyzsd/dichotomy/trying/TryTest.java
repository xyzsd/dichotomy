package net.xyzsd.dichotomy.trying;

import net.xyzsd.dichotomy.Empty;
import net.xyzsd.dichotomy.TestUtils.SingleUseConsumer;
import net.xyzsd.dichotomy.trying.function.ExConsumer;
import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;
import static net.xyzsd.dichotomy.TestUtils.*;
import static net.xyzsd.dichotomy.TestUtils.neverFunction;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TryTest {

    public static final String MSG_EX_AUTOCLOSE = "AutoCloseException";
    public static final String MSG_EX_FN = "FunctionException";
    public static final String MSG_EX_SUPPLIER = "SupplierException";
    public static final String MSG_SUCCESS = "SucessfulTry";
    public static final String MSG_FAIL = "FailureTry";
    public static final ArithmeticException FAILURE_AE = new ArithmeticException( MSG_FAIL );
    public static final Try<String> TRY_SUCCESS = Try.ofSuccess( MSG_SUCCESS );
    public static final Try<String> TRY_FAILURE = Try.ofFailure( FAILURE_AE );
    public static final Try<Integer> TRY_FAILURE_INT = Try.ofFailure( FAILURE_AE );


    public static final String IOE_MESSAGE = "IOE: An IOException";
    public static final IOException IOE = new IOException( IOE_MESSAGE );
    public static final RuntimeException RE = new RuntimeException( "RE: A RuntimeException" );

    public static final String SUPPLIED_STRING = "SuppliedString";

    public static final ExSupplier<String> EX_STRING_SUPPLIER_OK = () -> SUPPLIED_STRING;
    public static final ExSupplier<String> EX_STRING_SUPPLIER_FAIL = () -> {throw IOE;};

    public static final Supplier<String> STRING_SUPPLIER_OK = () -> SUPPLIED_STRING;
    public static final Supplier<String> STRING_SUPPLIER_FAIL = () -> {throw RE;};

    // throwable->throwable mapping
    public static final Function<Throwable, Throwable> E2E = (x) -> IOE;

    public static final ExSupplier<String> FATAL_SUPPLIER_ERROR = () -> {throw new LinkageError( "LinkageError" );};
    public static final ExSupplier<String> FATAL_SUPPLIER_EX = () -> {
        throw new InterruptedException( "InterruptedException" );
    };


    // function for mapping Strings to Integers
    public static final int STRING_2_INT_VALUE = 666;
    public static final Function<String, Integer> STRING_2_INT = (s) -> STRING_2_INT_VALUE;
    public static final Function<String, Try<Integer>> STRING_2_INT_FM = (s) -> Try.ofSuccess( STRING_2_INT_VALUE );
    public static final ExFunction<String, Integer> STRING_2_INT_FAIL = (s) -> {throw RE;};
    public static final ExFunction<String, Try<Integer>> STRING_2_INT_FAIL_FM = (s) -> {throw RE;};

    public static final ExFunction<Throwable, Throwable> E2E_FAIL = (e) -> {throw RE;};


    // A StringWriter that fails during (Auto)Close
    static class FAILWRITER extends StringWriter {
        @Override
        public void close() throws IOException {
            throw new IOException( MSG_EX_AUTOCLOSE );
        }
    }


    // match a given String with the contained Exception message
    // expects trie
    private static void assertMessage(final String expectedText, Try<?> tried) {
        requireNonNull( expectedText );
        requireNonNull( tried );
        switch (tried) {
            case Try.Success<?> __ -> throw new AssertionError( "Expected Failure, not " + tried );
            case Try.Failure<?> failure -> {
                if (!Objects.equals( expectedText, failure.err().getMessage() )) {
                    throw new AssertionError( String.format( "Expected '%s' Actual: '%s'",
                            expectedText,
                            failure.err().getMessage()
                    ) );
                }
            }
        }
    }

    // assert failure has the given Throwable class type
    private static <T extends Throwable> void assertException(final Class<T> exceptionClass, Try<?> tried) {
        requireNonNull( exceptionClass );
        requireNonNull( tried );
        switch (tried) {
            case Try.Success<?> __ -> throw new AssertionError( "Expected Failure, not " + tried );
            case Try.Failure<?> failure -> {
                if (!Objects.equals( exceptionClass, failure.err().getClass() )) {
                    throw new AssertionError( String.format( "Expected class '%s' Actual: '%s'",
                            exceptionClass.getClass(),
                            failure.err().getClass()
                    ) );
                }
            }
        }
    }


    private static void assertSuppressed(final int nSuppressed, Try<?> tried) {
        if (nSuppressed < 0) {throw new IllegalArgumentException();}
        requireNonNull( tried );
        switch (tried) {
            case Try.Success<?> __ -> throw new AssertionError( "Expected Failure, not " + tried );
            case Try.Failure<?> failure -> {
                if (nSuppressed != failure.err().getSuppressed().length)
                    throw new AssertionError( String.format( "Expected %d suppressed exceptions; actual count: %d: %s",
                            nSuppressed,
                            failure.err().getSuppressed().length,
                            Arrays.asList( failure.err().getSuppressed() )
                    ) );
            }
        }
    }


    @Test
    void ofSupplier() {
        // test simple suppliers
        {
            // success
            final Try<String> stn = Try.of( STRING_SUPPLIER_OK::get );
            assertEquals( Try.Success.class, stn.getClass() );
            assertTrue( stn.contains( SUPPLIED_STRING ) );
        }

        {
            // fail
            final Try<String> stn = Try.of( STRING_SUPPLIER_FAIL::get );
            assertEquals( Try.Failure.class, stn.getClass() );
            assertFalse( stn.contains( SUPPLIED_STRING ) );
            assertMessage( RE.getMessage(), stn );
            assertSuppressed( 0, stn );
        }

        // checked suppliers
        {
            // success
            final Try<String> stn = Try.of( EX_STRING_SUPPLIER_OK );
            assertEquals( Try.Success.class, stn.getClass() );
            assertTrue( stn.contains( SUPPLIED_STRING ) );
        }

        {
            // fail
            final Try<String> stn = Try.of( EX_STRING_SUPPLIER_FAIL );
            assertEquals( Try.Failure.class, stn.getClass() );
            assertFalse( stn.contains( SUPPLIED_STRING ) );
            assertMessage( IOE.getMessage(), stn );
            assertSuppressed( 0, stn );
        }

        {
            // fatal exceptions should be fatal
            assertThrows( LinkageError.class, () -> Try.of( FATAL_SUPPLIER_ERROR ) );
            assertThrows( InterruptedException.class, () -> Try.of( FATAL_SUPPLIER_EX ) );
        }
    }


    @Test
    void withResources1() {
        // success
        {
            final Try<String> helloTry = Try.withResources( StringWriter::new,
                    (x) -> {
                        x.write( "HelloWorld" );
                        return x.toString();
                    } );
            switch (helloTry) {
                case Try.Failure<String> __ -> throw new AssertionError();
                case Try.Success<String> success -> assertEquals( "HelloWorld", success.value() );
            }
        }

        // supplier fail
        {
            final Try<Object> tried = Try.withResources(
                    () -> {throw new RuntimeException( MSG_EX_SUPPLIER );},
                    neverFunction()::apply
            );
            assertTrue( tried.isFailure() );
            assertMessage( MSG_EX_SUPPLIER, tried );
            assertSuppressed( 0, tried );
        }

        // function fail
        {
            final Try<String> tried = Try.withResources( StringWriter::new,
                    (x) -> {
                        throw new IOException( MSG_EX_FN );
                    } );
            assertTrue( tried.isFailure() );
            assertMessage( MSG_EX_FN, tried );
            assertSuppressed( 0, tried );
        }

        // fail during (auto)close
        {
            final Try<String> tried = Try.withResources( FAILWRITER::new,
                    (x) -> {
                        x.write( "HelloWorld" );
                        return x.toString();
                    } );
            assertTrue( tried.isFailure() );
            assertMessage( MSG_EX_AUTOCLOSE, tried );  // first exception
            assertSuppressed( 0, tried );
        }

        // function failure AND exception during autoclose
        {
            final Try<String> tried = Try.withResources( FAILWRITER::new,
                    (x) -> {
                        throw new IOException( MSG_EX_FN );
                    } );
            assertTrue( tried.isFailure() );
            assertMessage( MSG_EX_FN, tried );      // primary exception
            assertSuppressed( 1, tried );            // suppressed IOException(MSG_EX_AUTOCLOSE) from FAILWRITER
        }
    }

    @Test
    void withResources2() {
        // TODO: combinatorially should have more testing...
        // success
        {
            final Try<String> helloTry = Try.withResources(
                    StringWriter::new,
                    StringWriter::new,
                    (s1,s2) -> {
                        s1.write( "Hello" );
                        s2.write( "World");
                        return s1.toString()+ s2;
                    } );


            switch (helloTry) {
                case Try.Failure<String> __ -> throw new AssertionError();
                case Try.Success<String> success -> assertEquals( "HelloWorld", success.value() );
            }
        }

        // function fail + both FAILWRITERs fail at close (suppressed)
        {
            final Try<String> totalFailure = Try.withResources(
                    FAILWRITER::new,
                    FAILWRITER::new,
                    (s1,s2) -> {
                        throw new IOException( MSG_EX_FN );
                    } );

            assertTrue( totalFailure.isFailure() );
            // ExBiFunction exception
            assertMessage( MSG_EX_FN, totalFailure );
            // suppressed AutoClose exceptions from both FAILWRITERs
            assertSuppressed( 2, totalFailure );
        }

    }

    @Test
    void isFailure() {
        assertFalse( Try.Success.of( SUPPLIED_STRING ).isFailure() );
        assertTrue( Try.Failure.of( IOE ).isFailure() );
    }

    @Test
    void isSuccess() {
        assertTrue( Try.Success.of( SUPPLIED_STRING ).isSuccess() );
        assertFalse( Try.Failure.of( IOE ).isSuccess() );
    }


    @Test
    void fold() {
        // null
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.fold( null, neverFunction() ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.fold( null, neverFunction() ) );
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.fold( neverFunction(), null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.fold( neverFunction(), null ) );

        // null function results -> throw an exception (since we return a value, not a Try)
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.fold( x -> null, neverFunction() ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.fold( neverFunction(), x -> null ) );

        // success and failures
        assertEquals( "ok",
                TRY_SUCCESS.fold( s -> "ok", neverFunction() ) );
        assertEquals( "ok",
                TRY_FAILURE.fold( neverFunction(), t -> "ok" ) );
        assertEquals( "ok",
                TRY_SUCCESS.fold( s -> "ok", t -> "not ok" ) );
        assertEquals( "not ok",
                TRY_FAILURE.fold( s -> "ok", t -> "not ok" ) );
    }

    @Test
    void stream() {
        assertEquals( 1, TRY_SUCCESS.stream().count() );
        assertEquals( 0, TRY_FAILURE.stream().count() );
    }

    @Test
    void filter() {
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.filter( null, neverExFunction() ) );
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.filter( (x) -> true, null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.filter( null, neverExFunction() ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.filter( (x) -> true, null ) );


        // exception in predicate class leads to failed try
        assertException( ArithmeticException.class, TRY_SUCCESS.filter(
                (s) -> {throw new ArithmeticException( "Arithmetic error In Predicate!" );},
                IOException::new
        ) );

        // success tests
        assertException( IOException.class, TRY_SUCCESS.filter(
                (s) -> false,
                IOException::new
        ) );
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.filter(
                (s) -> true,
                IOException::new
        ) );

        // failure tests
        assertEquals( TRY_FAILURE, TRY_FAILURE.filter(
                (s) -> true,
                IOException::new
        ) );
        assertEquals( TRY_FAILURE, TRY_FAILURE.filter(
                (s) -> false,
                IOException::new
        ) );
        // really, predicate is never tested for failures
        assertEquals( TRY_FAILURE, TRY_FAILURE.filter(
                (s) -> {throw new LinkageError( "shouldn't occur!" );},
                IOException::new
        ) );
    }

    @Test
    void exec() {
        // null input testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.exec( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.exec( null ) );
        // Try.Failure: testing : should never exec() anything
        assertDoesNotThrow( () -> TRY_FAILURE.exec( ExConsumer.from( neverConsumer() ) ) );
        assertDoesNotThrow( () -> TRY_FAILURE.exec( neverConsumer()::accept ) );    // alternate pattern
        // Try.Success
        SingleUseConsumer<String> consumer = new SingleUseConsumer<>();
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.exec( consumer::accept ) );
        assertTrue( consumer.usedJustOnce() );
        assertException( ArithmeticException.class, TRY_SUCCESS.exec( s -> {
            throw new ArithmeticException( "nope: " + s );
        } ) );
    }

    @Test
    void consume() {
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.consume( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.consume( null ) );
        assertThrows( LinkageError.class, () -> TRY_SUCCESS.consume( neverConsumer() ) );
        assertDoesNotThrow( () -> TRY_FAILURE.consume( neverConsumer() ) );
        //
        SingleUseConsumer<String> consumer = new SingleUseConsumer<>();
        TRY_SUCCESS.consume( consumer );
        assertTrue( consumer.usedJustOnce() );
    }

    @Test
    void consumeErr() {
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.consumeErr( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.consumeErr( null ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.consumeErr( neverConsumer() ) );
        assertThrows( LinkageError.class, () -> TRY_FAILURE.consumeErr( neverConsumer() ) );
        //
        SingleUseConsumer<Throwable> consumer = new SingleUseConsumer<>();
        TRY_FAILURE.consumeErr( consumer );
        assertTrue( consumer.usedJustOnce() );
    }

    @Test
    void map() {
        // null testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.map( null ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.map( x -> null ) );
        assertException( NullPointerException.class, TRY_SUCCESS.map( x -> null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.map( null ) );

        // success tests
        assertEquals( Try.ofSuccess( STRING_2_INT_VALUE ), TRY_SUCCESS.map( STRING_2_INT::apply ) );
        assertEquals( Try.ofFailure( RE ), TRY_SUCCESS.map( STRING_2_INT_FAIL ) );

        // failure
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.map( STRING_2_INT::apply ) );
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.map( STRING_2_INT_FAIL ) );
        assertDoesNotThrow( () -> TRY_FAILURE.map( neverFunction()::apply ) );
    }

    @Test
    void mapErr() {
        // null testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.mapErr( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.mapErr( null ) );
        assertDoesNotThrow( () -> TRY_FAILURE.mapErr( x -> null ) );
        assertException( NullPointerException.class, TRY_FAILURE.mapErr( x -> null ) );

        // success try
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.mapErr( E2E::apply ) );
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.mapErr( E2E::apply ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.mapErr( neverFunctionT()::apply ) );

        // failure try
        assertEquals( Try.ofFailure( IOE ), TRY_FAILURE.mapErr( E2E::apply ) );
        assertEquals( Try.ofFailure( RE ), TRY_FAILURE.mapErr( E2E_FAIL ) );
    }

    @Test
    void biMap() {
        // input null testing
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.biMap( null, neverFunctionT()::apply ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.biMap( null, neverFunctionT()::apply ) );
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.biMap( neverFunction()::apply, null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.biMap( neverFunction()::apply, null ) );

        // success try : including null function results
        assertDoesNotThrow( () -> TRY_SUCCESS.biMap( x -> null, neverFunctionT()::apply ) );
        assertException( NullPointerException.class,
                TRY_SUCCESS.biMap( x -> null, neverFunctionT()::apply ) );
        assertEquals( Try.ofSuccess( STRING_2_INT_VALUE ),
                TRY_SUCCESS.biMap( STRING_2_INT::apply, neverFunctionT()::apply ) );
        assertEquals( Try.ofFailure( RE ),
                TRY_SUCCESS.biMap( STRING_2_INT_FAIL, neverFunctionT()::apply ) );

        // failure try : including null function result
        assertDoesNotThrow( () -> TRY_FAILURE.biMap( neverFunction()::apply, x -> null ) );
        assertException( NullPointerException.class,
                TRY_FAILURE.biMap( neverFunction()::apply, x -> null ) );
        assertEquals( Try.ofFailure( IOE ),
                TRY_FAILURE.biMap( neverFunction()::apply, E2E::apply ) );
        assertEquals( Try.ofFailure( RE ),
                TRY_FAILURE.biMap( neverFunction()::apply, E2E_FAIL ) );
    }

    @Test
    void flatMap() {
        // null testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.flatMap( null ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.flatMap( x -> null ) );
        assertException( NullPointerException.class, TRY_SUCCESS.flatMap( x -> null ) );

        final ExFunction<String, Try<Integer>> fmf = (s) -> Try.ofSuccess( STRING_2_INT_VALUE );
        final ExFunction<String, Try<Integer>> fmf_ioe = (s) -> Try.ofFailure( IOE );
        final ExFunction<String, Try<Integer>> fmf_fail = (s) -> {throw RE;};
        final ExFunction<String, Try<Integer>> fmf_really_fail = (s) -> {throw new LinkageError( "very bad" );};

        // success testing
        assertEquals( Try.ofSuccess( STRING_2_INT_VALUE ), TRY_SUCCESS.flatMap( fmf ) );
        assertEquals( Try.ofFailure( RE ), TRY_SUCCESS.flatMap( fmf_fail ) );
        assertEquals( Try.ofFailure( IOE ), TRY_SUCCESS.flatMap( fmf_ioe ) );
        assertThrows( LinkageError.class, () -> TRY_SUCCESS.flatMap( fmf_really_fail ) );

        // failure testing
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.flatMap( fmf ) );
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.flatMap( fmf_fail ) );
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.flatMap( fmf_ioe ) );
        assertDoesNotThrow( () -> TRY_FAILURE.flatMap( fmf_really_fail ) );
    }

    @Test
    void flatMapErr() {
        // null testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.flatMapErr( null ) );
        assertDoesNotThrow( () -> TRY_FAILURE.flatMapErr( x -> null ) );
        assertException( NullPointerException.class, TRY_FAILURE.flatMapErr( x -> null ) );

        final ExFunction<Throwable, Try<String>> fmf = (s) -> Try.ofSuccess( "NewValue" );
        final ExFunction<Throwable, Try<String>> fmf_fail = (s) -> {throw RE;};
        final ExFunction<Throwable, Try<String>> fmf_ioe = (s) -> Try.ofFailure( IOE );
        final ExFunction<Throwable, Try<String>> fmf_really_fail = (s) -> {throw new LinkageError( "very bad" );};

        // success testing
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.flatMapErr( fmf ) );
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.flatMapErr( fmf_fail ) );
        assertEquals( TRY_SUCCESS, TRY_SUCCESS.flatMapErr( fmf_ioe ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.flatMapErr( fmf_really_fail ) );

        // failure testing
        assertEquals( Try.ofSuccess( "NewValue" ), TRY_FAILURE.flatMapErr( fmf ) );
        assertEquals( Try.ofFailure( RE ), TRY_FAILURE.flatMapErr( fmf_fail ) );
        assertEquals( Try.ofFailure( IOE ), TRY_FAILURE.flatMapErr( fmf_ioe ) );
        assertThrows( LinkageError.class, () -> TRY_FAILURE.flatMapErr( fmf_really_fail ) );
    }

    @Test
    void biFlatMap() {
        // input null testing
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.biFlatMap( null, neverExFunction() ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.biFlatMap( null, neverExFunction() ) );
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.<String>biFlatMap( neverExFunction(), null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.biFlatMap( neverExFunction(), null ) );

        // successes
        assertDoesNotThrow( () -> TRY_SUCCESS.biFlatMap( x -> null, neverExFunction() ) );
        assertException( NullPointerException.class,
                TRY_SUCCESS.biFlatMap( x -> null, neverExFunction() ) );
        assertEquals( Try.ofSuccess( STRING_2_INT_VALUE ),
                TRY_SUCCESS.biFlatMap( STRING_2_INT_FM::apply, neverExFunction() ) );
        assertEquals( Try.ofFailure( RE ),
                TRY_SUCCESS.biFlatMap( STRING_2_INT_FAIL_FM, neverExFunction() ) );

        // failures
        assertDoesNotThrow( () -> TRY_FAILURE.biFlatMap( STRING_2_INT_FM::apply, x -> null ) );
        assertException( NullPointerException.class, TRY_FAILURE.biFlatMap( STRING_2_INT_FM::apply, x -> null ) );
        assertException( ArithmeticException.class, TRY_FAILURE.biFlatMap( STRING_2_INT_FM::apply,
                x -> { throw new ArithmeticException("Bad Math");} ) );
        assertException( ArithmeticException.class, TRY_FAILURE.biFlatMap( STRING_2_INT_FM::apply,
                x ->  Try.ofFailure( new ArithmeticException("Bad Math 2")) ) );
        // fail -> success (!), same type
        assertEquals(
                Try.ofSuccess( "Fail2Success" ),
                TRY_FAILURE.biFlatMap( STRING_2_INT_FM::apply, x -> Try.ofSuccess( "Fail2Success" ))
                );
        // fail -> success (!), different type
        assertEquals(
                Try.ofSuccess( 31415 ),
                TRY_FAILURE.biFlatMap( STRING_2_INT_FM::apply, x -> Try.ofSuccess( 31415 ))
        );
    }

    @Test
    void ifPredicate() {
        final Predicate<String> predicateS = x -> x.startsWith( "S" );
        assertTrue( TRY_SUCCESS.ifPredicate( predicateS ) );
        assertFalse( TRY_SUCCESS.ifPredicate( predicateS.negate() ) );
        //
        assertFalse( TRY_FAILURE.ifPredicate( predicateS ) );
        assertFalse( TRY_FAILURE.ifPredicate( predicateS.negate() ) );
    }

    @Test
    void contains() {
        assertTrue( Try.Success.of( SUPPLIED_STRING ).contains( "SuppliedString" ) );
        assertFalse( Try.Success.of( SUPPLIED_STRING ).contains( "randomstring#*@(#$" ) );
        assertFalse( Try.Failure.of( IOE ).contains( "SuppliedString" ) );
        // null : allowed but always false.
        assertFalse( TRY_SUCCESS.contains( null ) );
        assertFalse( TRY_FAILURE.contains( null ) );
    }

    @Test
    void orElse() {
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.orElse( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.orElse( null ) );
        //
        assertEquals( MSG_SUCCESS, TRY_SUCCESS.orElse( "Hello" ) );
        assertEquals( "Hello", TRY_FAILURE.orElse( "Hello" ) );
    }

    @Test
    void orElseGet() {
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.orElseGet( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.orElseGet( null ) );
        //
        assertDoesNotThrow( () -> TRY_SUCCESS.orElseGet( neverSupplier() ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.orElseGet( () -> null ) );
        //
        assertEquals( MSG_SUCCESS, TRY_SUCCESS.orElseGet( () -> "Hello" ) );
        assertEquals( "Hello", TRY_FAILURE.orElseGet( () -> "Hello" ) );
    }


    @Test
    void recover() {
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.recover( null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.recover( null ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.recover( (x) -> null ) ); // because fn not used
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.recover( (x) -> null ) );
        //
        final Function<Throwable, String> e2v = Throwable::getMessage;
        assertEquals( MSG_FAIL, TRY_FAILURE.recover( e2v ) );
        assertEquals( MSG_SUCCESS, TRY_SUCCESS.recover( e2v ) );
    }


    @Test
    void forfeit() {
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.forfeit( null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.forfeit( null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_SUCCESS.forfeit( (x) -> null ) );
        assertDoesNotThrow( () -> TRY_FAILURE.forfeit( (x) -> null ) ); // because fn not used
        //
        final Function<String, Throwable> e2v = ArithmeticException::new;
        assertEquals( MSG_FAIL, TRY_FAILURE.forfeit( e2v ).getMessage() );
        assertEquals( MSG_SUCCESS, TRY_SUCCESS.forfeit( e2v ).getMessage() );
    }


    @Test
    void expect() {
        assertDoesNotThrow( TRY_SUCCESS::expect );
        assertEquals( MSG_SUCCESS, TRY_SUCCESS.expect() );
        //
        assertThrows( NoSuchElementException.class, () -> TRY_FAILURE.expect() );
        try {
            TRY_FAILURE.expect();
        } catch (NoSuchElementException e) {
            // ensure we set the Throwable as a cause for the created NoSuchElementException
            assertNotNull( e.getCause() );
            assertEquals( MSG_FAIL, e.getCause().getMessage() );
        }
    }

    @Test
    void getOrThrow() {
        // null input
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.getOrThrow( null ) );
        assertThrows( NullPointerException.class, () -> TRY_FAILURE.getOrThrow( null ) );
        assertThrows( NullPointerException.class,
                () -> TRY_FAILURE.getOrThrow( (x) -> null ) );
        //
        //
        assertDoesNotThrow( () -> TRY_SUCCESS.getOrThrow( neverFunction() ) );
        // if we use an IOException::new here, we would have to wrap it in a try-catch.
        assertEquals( MSG_SUCCESS, TRY_SUCCESS.getOrThrow( RuntimeException::new ) );
        //
        //
        assertThrows( IOException.class, () -> TRY_FAILURE.getOrThrow( IOException::new ) );
        try {
            // this form will use the IOException(Throwable) constructor
            TRY_FAILURE.getOrThrow( IOException::new );
        } catch (IOException e) {
            // ensure we are applying function correctly
            assertNotNull( e.getCause() );
            assertEquals( MSG_FAIL, e.getCause().getMessage() );
        }

        try {
            // this form will use the IOException(String) constructor; more like a supplier
            // (function input argument is ignored)
            TRY_FAILURE.getOrThrow( __ -> new IOException( "Here be Exceptions" ) );
        } catch (IOException e) {
            // ensure we are applying function correctly
            assertNull( e.getCause() );
            assertEquals( "Here be Exceptions", e.getMessage() );
        }

    }


    @Test
    void randomstuff() {
        int div = 1;
        final Try<Integer> integerTry = Try.of(
                () -> {
                    System.out.println( "beginnign a codeblock" );
                    int i = 318794;
                    i = i / Random.from( RandomGenerator.getDefault() ).nextInt();
                    i = i / div;
                    System.out.println( "ending code block Ok" );
                    return i;
                }
        );

        final Try<Empty> asdf = Try.of(
                () -> {
                    System.out.println( "beginnign a codeblock" );
                    int i = 318794;
                    i = i / Random.from( RandomGenerator.getDefault() ).nextInt();
                    i = i / div;
                    System.out.println( "ending code block Ok" );
                    return Empty.getInstance();
                }
        );

        // so above would be an int or if /0 then a failure
    }
}