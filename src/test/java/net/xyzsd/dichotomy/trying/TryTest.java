package net.xyzsd.dichotomy.trying;

import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static net.xyzsd.dichotomy.TestUtils.neverFunction;
import static org.junit.jupiter.api.Assertions.*;

class TryTest {

    public static final String MSG_EX_AUTOCLOSE = "AutoCloseException";
    public static final String MSG_EX_FN = "FunctionException";
    public static final String MSG_EX_SUPPLIER = "SupplierException";
    public static final String MSG_SUCCESS = "SucessfulTry";
    public static final String MSG_FAIL = "FailureTry";
    public static final ArithmeticException FAILURE_AE = new ArithmeticException(MSG_FAIL);
    public static final Try<String> TRY_SUCCESS = Try.ofSuccess( MSG_SUCCESS );
    public static final Try<String> TRY_FAILURE = Try.ofFailure( FAILURE_AE );
    public static final Try<Integer> TRY_FAILURE_INT = Try.ofFailure( FAILURE_AE );


    public static final IOException IOE = new IOException( "IOE: An IOException" );
    public static final RuntimeException RE = new RuntimeException( "RE: A RuntimeException" );

    public static final String SUPPLIED_STRING = "SuppliedString";

    public static final ExSupplier<String> EX_STRING_SUPPLIER_OK = () -> SUPPLIED_STRING;
    public static final ExSupplier<String> EX_STRING_SUPPLIER_FAIL = () -> {throw IOE;};

    public static final Supplier<String> STRING_SUPPLIER_OK = () -> SUPPLIED_STRING;
    public static final Supplier<String> STRING_SUPPLIER_FAIL = () -> {throw RE;};


    public static final ExSupplier<String> FATAL_SUPPLIER_ERROR = () -> {throw new LinkageError( "LinkageError" );};
    public static final ExSupplier<String> FATAL_SUPPLIER_EX = () -> {
        throw new InterruptedException( "InterruptedException" );
    };


    // function for mapping Strings to Integers
    public static final int STRING_2_INT_VALUE = 666;
    public static final Function<String,Integer> STRING_2_INT = (s) -> STRING_2_INT_VALUE;
    public static final ExFunction<String,Integer> STRING_2_INT_FAIL = (s) -> { throw RE; };



    class FAILWRITER extends StringWriter {
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

    // assert failure has the given Exception class type
    private static void assertException(final Class<? extends Exception> exceptionClass, Try<?> tried) {
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
                            Arrays.asList(failure.err().getSuppressed())
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
            assertSuppressed(0, stn);
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
            assertSuppressed(0, stn);
        }

        {
            // fatal exceptions should be fatal
            assertThrows( LinkageError.class, () -> Try.of( FATAL_SUPPLIER_ERROR ) );
            assertThrows( InterruptedException.class, () -> Try.of( FATAL_SUPPLIER_EX ) );
        }
    }


    @Test
    void ofFn() {
    }

    @Test
    void ofBiFn() {
    }

    @Test
    void from() {
    }

    @Test
    void testFrom() {
    }

    @Test
    void testFrom1() {
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
            assertSuppressed(0, tried);
        }

        // function fail
        {
            final Try<String> tried = Try.withResources( StringWriter::new,
                    (x) -> {
                        throw new IOException( MSG_EX_FN );
                    } );
            assertTrue( tried.isFailure() );
            assertMessage( MSG_EX_FN, tried );
            assertSuppressed(0, tried);
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
            assertSuppressed(0, tried);
        }

        // function failure AND exception during autoclose
        {
            final Try<String> tried = Try.withResources( FAILWRITER::new,
                    (x) -> {
                        throw new IOException( MSG_EX_FN );
                    } );
            assertTrue( tried.isFailure() );
            assertMessage( MSG_EX_FN, tried );      // primary exception
            assertSuppressed(1,  tried);            // suppressed IOException(MSG_EX_AUTOCLOSE) from FAILWRITER
        }
    }

    @Test
    void withResources2() {
        // todo
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
    void biFlatMap() {
    }

    @Test
    void fold() {
    }

    @Test
    void stream() {

    }

    @Test
    void filter() {
    }

    @Test
    void exec() {
    }

    @Test
    void consume() {
    }

    @Test
    void map() {
        // null testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.map( null ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.map( x -> null ) );
        assertException( NullPointerException.class, TRY_SUCCESS.map( x -> null ) );

        // success tests
        assertEquals( Try.ofSuccess( STRING_2_INT_VALUE ), TRY_SUCCESS.map(STRING_2_INT::apply));
        assertEquals( Try.ofFailure( RE ), TRY_SUCCESS.map(STRING_2_INT_FAIL));

        // failure
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.map(STRING_2_INT::apply));
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.map(STRING_2_INT_FAIL));
        assertDoesNotThrow( () -> TRY_FAILURE.map(neverFunction()::apply));
    }

    @Test
    void flatMap() {
        // null testing
        assertThrows( NullPointerException.class, () -> TRY_SUCCESS.flatMap( null ) );
        assertDoesNotThrow( () -> TRY_SUCCESS.flatMap( x -> null ) );
        assertException( NullPointerException.class, TRY_SUCCESS.flatMap( x -> null ) );

        final ExFunction<String,Try<Integer>> fmf = (s) -> Try.ofSuccess( STRING_2_INT_VALUE );
        final ExFunction<String,Try<Integer>> fmf_ioe = (s) -> Try.ofFailure( IOE );
        final ExFunction<String,Try<Integer>> fmf_fail = (s) -> { throw RE; };
        final ExFunction<String,Try<Integer>> fmf_really_fail = (s) -> { throw new LinkageError("very bad"); };

        // success testing
        assertEquals( Try.ofSuccess( STRING_2_INT_VALUE ), TRY_SUCCESS.flatMap(fmf));
        assertEquals( Try.ofFailure( RE ), TRY_SUCCESS.flatMap(fmf_fail));
        assertEquals( Try.ofFailure( IOE ), TRY_SUCCESS.flatMap(fmf_ioe));
        assertThrows( LinkageError.class,  () -> TRY_SUCCESS.flatMap(fmf_really_fail));

        // failure testing
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.flatMap(fmf));
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.flatMap(fmf_fail));
        assertEquals( TRY_FAILURE_INT, TRY_FAILURE.flatMap(fmf_ioe));
        assertDoesNotThrow( () -> TRY_FAILURE.flatMap(fmf_really_fail));
    }


    @Test
    void ifPredicate() {
    }

    @Test
    void contains() {
        assertTrue( Try.Success.of( SUPPLIED_STRING ).contains( "SuppliedString" ) );
        assertFalse( Try.Success.of( SUPPLIED_STRING ).contains( "randomstring#*@(#$" ) );
        assertFalse( Try.Failure.of( IOE ).contains( "SuppliedString" ) );
    }

    @Test
    void orElse() {
    }

    @Test
    void orElseGet() {
    }

    @Test
    void recover() {
    }


    @Test
    void forfeit() {
    }

    @Test
    void testBiFlatMap() {
    }

    @Test
    void consumeErr() {
    }

    @Test
    void mapErr() {
    }

    @Test
    void biMap() {
    }

    @Test
    void flatMapErr() {
    }

    @Test
    void expect() {

    }

    @Test
    void getOrThrow() {


    }

}