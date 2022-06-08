package net.xyzsd.dichotomy.result;

import org.junit.jupiter.api.Test;
import net.xyzsd.dichotomy.None;
import net.xyzsd.dichotomy.either.EitherTest.SingleUseConsumer;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;


class ResultTest {

    // standard test values
    // for Result<String,RuntimeException>
    static final String OK_VALUE = "[ok]";
    static final String ERR_VALUE_MESSAGE = "*no such element*";
    static final RuntimeException ERR_VALUE = new NoSuchElementException( ERR_VALUE_MESSAGE );
    //
    static final Result<String, RuntimeException> OK = Result.ofOK( OK_VALUE );
    static final Result<String, RuntimeException> ERR = Result.ofErr( ERR_VALUE );


    // standard mapping functions for testing
    static final String fnOK_VAL = "*MAPPED_STRING";
    static final RuntimeException fnERR_VAL = new NoSuchElementException( "*MAPPED_" + ERR_VALUE_MESSAGE );
    // mapping functions
    static final Function<String, String> fnOK = s -> fnOK_VAL;
    static final Function<RuntimeException, RuntimeException> fnERR2ERR = ex -> fnERR_VAL;
    static final Function<RuntimeException, String> fnERR2STR = ex -> fnERR_VAL.getMessage();

    static final RuntimeException fnSTR2ERR_VAL = new ArithmeticException( "mapped_via_fnSTR2ERR" );
    static final Function<String, RuntimeException> fnSTR2ERR = s -> fnSTR2ERR_VAL;
    // flat-mapping functions
    static final Function<String, Result<String, RuntimeException>> fnOK_FLAT = (x) -> Result.ofOK( fnOK_VAL );
    static final Function<RuntimeException, Result<String, RuntimeException>> fnERR_FLAT = (e) -> Result.ofErr( fnERR_VAL );


    // TODO: needs to be in a common util class; copied from EitherTest
    // 'never' types: check if implementation calls a supplier / function when it doesn't need to
    // Using rawtypes is easier. if the cast fails... we have an error anyway
    @SuppressWarnings("rawtypes")
    public static final Supplier NEVERSUPPLIER = () -> {
        throw new IllegalStateException( "NEVERSUPPLIER::get invoked!" );
    };

    @SuppressWarnings("rawtypes")
    public static final Function NEVERFUNCTION = (x) -> {
        throw new IllegalStateException( "NEVERFUNCTION::apply invoked!" );
    };


    @Test
    void ofOK() {
        assertEquals( OK.class, OK.getClass() );
        assertEquals( OK_VALUE, ((OK<String, RuntimeException>) OK).get() );

        assertThrows( NullPointerException.class, () -> {Result.ofOK( null );} );
    }

    @Test
    void testOfOK() {
        final Result<None, Object> result = Result.ofOK();
        assertEquals( OK.class, result.getClass() );
        assertEquals( new None(), ((OK<None, Object>) result).get() );
    }

    @Test
    void ofNullable() {
        // this test assumes that other method tests pass
        Result<String, None> resultFromNull = Result.ofNullable( null );
        assertTrue( resultFromNull.isErr() );

        Result<String, None> resultNotNull = Result.ofNullable( OK_VALUE );
        assertTrue( resultNotNull.isOK() );
    }


    @Test
    void from() {
        // this test assumes that other method tests pass
        Result<String, None> resultFromEmpty = Result.from( Optional.<String>empty() );
        assertTrue( resultFromEmpty.isErr() );

        Result<String, None> result = Result.from( Optional.of( OK_VALUE ) );
        assertTrue( result.isOK() );
    }

    @Test
    void ofErr() {
        Result<String, RuntimeException> result = Result.ofErr( ERR_VALUE );
        assertEquals( Err.class, result.getClass() );
        assertEquals( ERR_VALUE, ((Err<String, RuntimeException>) result).get() );
        assertEquals( result, ERR );
        assertThrows( NullPointerException.class, () -> {Result.ofErr( null );} );
    }

    @Test
    void attempt() {
        // todo
    }

    @Test
    void testAttempt() {
        // todo
    }

    @Test
    void withAttempt() {
        // todo
    }

    @Test
    void testWithAttempt() {
        // todo
    }

    @Test
    void isOK() {
        assertTrue( OK.isOK() );
        assertFalse( OK.isErr() );
    }

    @Test
    void isFailure() {
        assertTrue( ERR.isErr() );
        assertFalse( ERR.isOK() );
    }

    @Test
    void ok() {
        assertEquals( Optional.of( OK_VALUE ), OK.left() );
        assertTrue( OK.right().isEmpty() );
    }

    @Test
    void err() {
        assertEquals( Optional.of( ERR_VALUE ), ERR.right() );
        assertTrue( ERR.left().isEmpty() );
    }

    @Test
    void left() {
        assertEquals( Optional.of( OK_VALUE ), OK.left() );
        assertTrue( OK.right().isEmpty() );
        assertEquals( OK.err(), OK.right() );
        assertEquals( OK.ok(), OK.left() );
    }

    @Test
    void right() {
        assertEquals( Optional.of( ERR_VALUE ), ERR.right() );
        assertTrue( ERR.left().isEmpty() );
        assertEquals( ERR.err(), ERR.right() );
        assertEquals( ERR.ok(), ERR.left() );
    }

    @Test
    void swap() {
        Result<String, String> left = Result.ofOK( "ok!" );
        assertTrue( left.isOK() );
        Result<String, String> leftSwap = left.swap();      // ok -> err (left->right)
        assertTrue( leftSwap.isErr() );
        assertTrue( leftSwap.containsErr( "ok!" ) );
        assertTrue( left.isOK() );  // ensure original unchanged

        Result<String, String> right = Result.ofErr( "error!" );
        assertTrue( right.isErr() );
        Result<String, String> rightSwap = right.swap();
        assertTrue( rightSwap.isOK() );
        assertTrue( rightSwap.contains( "error!" ) );
        assertTrue( right.isErr() );    // ensure original unchanged
    }

    @Test
    void unwrap() {
        assertEquals( OK_VALUE, OK.unwrap() );
        assertEquals( ERR_VALUE, ERR.unwrap() );
    }

    @Test
    void biMatch() {
        SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
        SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();

        OK.biMatch( okConsumer, errConsumer );
        assertTrue( okConsumer.wasActivatedOnce() );
        assertFalse( errConsumer.wasActivatedOnce() );

        ERR.biMatch( okConsumer, errConsumer );
        assertTrue( okConsumer.wasActivatedOnce() );
        assertTrue( errConsumer.wasActivatedOnce() );
    }

    @Test
    void biMap() {
        Result<String, String> mapped = OK.biMap( fnOK, fnERR2STR );
        assertTrue( mapped.isOK() );
        assertTrue( mapped.contains( fnOK_VAL ) );

        mapped = ERR.biMap( fnOK, fnERR2STR );
        assertTrue( mapped.isErr() );
        assertTrue( mapped.containsErr( fnERR_VAL.getMessage() ) );

        // functions must not return null
        assertThrows( NullPointerException.class, () -> {
            OK.biMap( x -> null, fnERR2STR );
        } );

        assertThrows( NullPointerException.class, () -> {
            ERR.biMap( fnOK, x -> null );
        } );

        // unused side must not apply function
        assertDoesNotThrow( () -> OK.biMap( fnOK, NEVERFUNCTION ) );
        assertDoesNotThrow( () -> ERR.biMap( NEVERFUNCTION, fnERR2STR ) );
    }

    @Test
    void biFlatMap() {
        assertEquals(
                Result.ofOK( fnOK_VAL ),
                OK.biFlatMap( fnOK_FLAT, fnERR_FLAT )
        );

        assertEquals(
                Result.ofErr( fnERR_VAL ),
                ERR.biFlatMap( fnOK_FLAT, fnERR_FLAT )
        );

        assertThrows( NullPointerException.class, () -> {
            OK.biFlatMap( x -> null, fnERR_FLAT );
        } );

        assertThrows( NullPointerException.class, () -> {
            ERR.biFlatMap( fnOK_FLAT, x -> null );
        } );

        assertDoesNotThrow( () -> OK.biFlatMap( fnOK_FLAT, NEVERFUNCTION ) );
        assertDoesNotThrow( () -> ERR.biFlatMap( NEVERFUNCTION, fnERR_FLAT ) );
    }

    @Test
    void fold() {
        assertEquals( fnOK_VAL, OK.fold( fnOK, NEVERFUNCTION ) );
        assertEquals( fnERR_VAL, ERR.fold( NEVERFUNCTION, fnERR2ERR ) );

        // null-returning function test
        assertThrows( NullPointerException.class, () -> {
            OK.fold( x -> null, NEVERFUNCTION );
        } );

        assertThrows( NullPointerException.class, () -> {
            ERR.fold( NEVERFUNCTION, x -> null );
        } );
    }

    @Test
    void stream() {
        assertEquals( 1, OK.stream().count() );
        assertTrue( OK.stream().allMatch( OK_VALUE::equals ) );
        assertEquals( 0, ERR.stream().count() );
    }

    @Test
    void filter() {
        // Err->Err
        assertEquals( ERR, ERR.filter( x -> true, NEVERFUNCTION ) );
        assertEquals( ERR, ERR.filter( x -> false, NEVERFUNCTION ) );

        // OK->OK if predicate matches
        assertEquals( OK, OK.filter( x -> true, NEVERFUNCTION ) );

        // OK->Err if predicate doesn't match
        assertEquals( Result.ofErr( fnSTR2ERR_VAL ), OK.filter( x -> false, fnSTR2ERR ) );
    }

    @Test
    void match() {
        SingleUseConsumer<String> consumer = new SingleUseConsumer<>();
        OK.match( consumer );
        assertTrue( consumer.wasActivatedOnce() );
        ERR.match( consumer );
        assertTrue( consumer.wasActivatedOnce() );
    }

    @Test
    void map() {
        assertEquals( Result.ofOK( fnOK_VAL ), OK.map( fnOK ) );
        assertEquals( ERR, ERR.map( fnOK ) );
        assertDoesNotThrow( () -> {ERR.map( NEVERFUNCTION );} );
        assertThrows( NullPointerException.class, () -> OK.map( x -> null ) );
    }

    @Test
    void flatMap() {
        assertEquals( Result.ofOK( fnOK_VAL ), OK.flatMap( fnOK_FLAT ) );
        assertEquals( ERR, ERR.flatMap( fnOK_FLAT ) );
        assertDoesNotThrow( () -> {ERR.flatMap( NEVERFUNCTION );} );
        assertThrows( NullPointerException.class, () -> OK.flatMap( x -> null ) );
    }

    @Test
    void matches() {
        assertTrue( OK.matches( x -> true ) );
        assertFalse( OK.matches( x -> false ) );

        assertFalse( ERR.matches( x -> true ) );
        assertFalse( ERR.matches( x -> false ) );
    }

    @Test
    void contains() {
        assertTrue( OK.contains( OK_VALUE ) );
        assertFalse( OK.contains( "" ) );
    }

    @Test
    void orElse() {
        assertEquals( OK_VALUE, OK.orElse( "orElse-alternate" ) );
        assertEquals( "orElse-alternate", ERR.orElse( "orElse-alternate" ) );
    }

    @Test
    void orElseGet() {
        assertEquals( OK_VALUE, OK.orElseGet( NEVERSUPPLIER ) );
        assertEquals( "orElse-alternate", ERR.orElseGet( () -> "orElse-alternate" ) );
    }

    @Test
    void recover() {
        assertEquals( OK_VALUE, OK.recover( NEVERFUNCTION ) );
        assertEquals( fnERR_VAL.getMessage(), ERR.recover( fnERR2STR ) );
    }

    @Test
    void streamErr() {
        assertEquals( 1, ERR.streamErr().count() );
        assertTrue( ERR.streamErr().allMatch( ERR_VALUE::equals ) );
        assertEquals( 0, OK.streamErr().count() );
    }

    @Test
    void matchErr() {
        SingleUseConsumer<RuntimeException> consumer = new SingleUseConsumer<>();
        ERR.matchErr( consumer );
        assertTrue( consumer.wasActivatedOnce() );
        OK.matchErr( consumer );
        assertTrue( consumer.wasActivatedOnce() );
    }

    @Test
    void mapErr() {
        assertEquals( Result.ofErr( fnERR_VAL ), ERR.mapErr( fnERR2ERR ) );
        assertEquals( OK, OK.mapErr( fnERR2ERR ) );
        assertDoesNotThrow( () -> {OK.mapErr( NEVERFUNCTION );} );
        assertThrows( NullPointerException.class, () -> ERR.mapErr( x -> null ) );
    }

    @Test
    void flatMapErr() {
        assertEquals( Result.ofErr( fnERR_VAL ), ERR.flatMapErr( fnERR_FLAT ) );
        assertEquals( OK, OK.flatMapErr( fnERR_FLAT ) );
        assertDoesNotThrow( () -> {OK.flatMapErr( NEVERFUNCTION );} );
        assertThrows( NullPointerException.class, () -> ERR.flatMapErr( x -> null ) );
    }

    @Test
    void matchesErr() {
        assertTrue( ERR.matchesErr( x -> true ) );
        assertFalse( ERR.matchesErr( x -> false ) );

        assertFalse( OK.matchesErr( x -> true ) );
        assertFalse( OK.matchesErr( x -> false ) );
    }

    @Test
    void containsErr() {
        assertTrue( ERR.containsErr( ERR_VALUE ) );
        assertFalse( ERR.containsErr( new RuntimeException( "?" ) ) );
    }

    @Test
    void orElseErr() {
        assertEquals( ERR_VALUE, ERR.orElseErr( new RuntimeException( "*invalid*" ) ) );
        assertEquals( fnERR_VAL, OK.orElseErr( fnERR_VAL ) );
    }

    @Test
    void orElseGetErr() {
        assertEquals( ERR_VALUE, ERR.orElseGetErr( NEVERSUPPLIER ) );
        assertEquals( fnERR_VAL, OK.orElseGetErr( () -> fnERR_VAL ) );
    }

    @Test
    void forfeit() {
        assertEquals( ERR_VALUE, ERR.forfeit( NEVERFUNCTION ) );
        assertEquals( fnSTR2ERR_VAL, OK.forfeit( fnSTR2ERR ) );
    }

    @Test
    void and() {
        assertEquals( ERR, ERR.and( Result.ofOK( "nextResult" ) ) );
        assertEquals( Result.ofOK( "nextResult" ), OK.and( Result.ofOK( "nextResult" ) ) );
    }

    @Test
    void testAnd() {
        assertEquals( ERR, ERR.and( NEVERSUPPLIER ) );

        assertEquals(
                Result.ofOK( "nextResult" ),
                OK.and( () -> Result.ofOK( "nextResult" ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> { OK.and( () -> null ); }
        );
    }

    @Test
    void or() {
        assertEquals( OK, OK.or( Result.ofOK( "nextResult" ) ) );
        assertEquals(
                Result.ofErr( "nextResult" ),
                ERR.or( Result.ofErr( "nextResult" ) )
        );
    }

    @Test
    void testOr() {
        assertEquals( OK, OK.or( NEVERSUPPLIER ) );

        assertEquals(
                Result.ofErr( "nextResult" ),
                ERR.or( () -> Result.ofErr( "nextResult" ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> { ERR.or( () -> null ); }
        );
    }

    @Test
    void expect() {
        assertEquals( OK_VALUE, OK.expect() );
        assertDoesNotThrow( OK::expect );

        // various errors and exception types
        assertThrows(
                ArithmeticException.class,
                () -> Result.ofErr(new ArithmeticException()).expect()
        );
        assertThrows(
                ResultException.class,
                () -> Result.ofErr(new IOException()).expect()
        );
        assertThrows(
                ResultException.class,
                () -> Result.ofErr(new AssertionError()).expect()
        );

        // non-exceptional values
        assertThrows(
                ResultException.class,
                () -> Result.ofErr(666).expect()
        );

        try {
            Result.ofErr(666).expect();
        } catch(ResultException e) {
            assertEquals( "666", e.getMessage() );
        }
    }

    @Test
    void orThrowWrapped() {
        assertDoesNotThrow( () -> OK.orThrowWrapped( NEVERFUNCTION ) );
        assertEquals( OK_VALUE, OK.orThrowWrapped(NEVERFUNCTION) );
        assertThrows(
                IOException.class,
                () -> ERR.orThrowWrapped( IOException::new )
        );
        assertThrows(
                RuntimeException.class,
                () -> ERR.orThrowWrapped( RuntimeException::new )
        );

        // but also see this
        final String MESSAGE = "My Error Message";
        final Result<?,String> result = Result.ofErr( MESSAGE );
        assertThrows(
                IOException.class,
                () -> result.orThrowWrapped( IOException::new )
        );

        try {
            result.orThrowWrapped( IOException::new );
        } catch(IOException e) {
            assertEquals( MESSAGE, e.getMessage() );
        }
    }

    @Test
    void orThrow() {
        assertDoesNotThrow( () -> OK.orThrow( NEVERSUPPLIER ) );
        assertEquals( OK_VALUE, OK.orThrow(NEVERSUPPLIER) );

        assertThrows(
                IOException.class,
                () -> ERR.orThrow( IOException::new )
        );
        assertThrows(
                RuntimeException.class,
                () -> ERR.orThrow( RuntimeException::new )
        );
    }

    @Test
    void monadicLaws() {
        // NOTE: the terms 'left identity' and 'right identity' below, are general terms for monad properties,
        // for ease of applying functions, we will use a new results type: Result<String, Integer>
        final String MTEST_OK_VALUE = "monadic-laws-test-ok-value";
        final int MTEST_ERR_VALUE = 666; // because why not
        final Result<String,Integer> MTEST_OK = Result.ofOK(MTEST_OK_VALUE);
        final Result<String,Integer> MTEST_ERR = Result.ofErr( MTEST_ERR_VALUE );

        // FIRST LAW: LEFT IDENTITY
        // Simply: data is wrapped, not manipulated/changed.
        //
        // Given a Result with a value in it and a function that takes the same type of value and
        // returns the same type of Result, then applying the function to the value should
        // be equivalent to flatMapping on the Result.
        //
        // given: the following function:
        final Function<String, Result<String,Integer>> concat = s -> Result.ofOK(s+s);
        // then, the following should be equivalent:
        assertEquals(
                concat.apply(MTEST_OK_VALUE),
                MTEST_OK.flatMap( concat )
        );
        // similarly, for Err:
        final Function<Integer, Result<String,Integer>> square = i -> Result.ofErr(i*i);
        assertEquals(
                square.apply(MTEST_ERR_VALUE),
                MTEST_ERR.flatMapErr( square )
        );


        // SECOND LAW: RIGHT IDENTITY
        // Simply: data is wrapped, not manipulated/changed.
        //
        // If we flatMap() a Result in the same type of Result, without changing the value,
        // the value should be unchanged.
        assertEquals(
                MTEST_OK,
                MTEST_OK.flatMap( Result::ofOK )
        );

        assertEquals(
                MTEST_OK,
                MTEST_OK.flatMapErr( Result::ofErr )
        );

        assertEquals(
                MTEST_ERR,
                MTEST_ERR.flatMap( Result::ofOK )
        );

        assertEquals(
                MTEST_ERR,
                MTEST_ERR.flatMapErr( Result::ofErr )
        );

        // THIRD LAW: ASSOCIATIVITY
        // flatmap nesting order should not matter
        // given: functions concat and square from above (for OK and Err Result monads)
        // given: the following functions:
        final Function<String,Result<String,Integer>> delim = s -> Result.ofOK(s+","+s);
        final Function<Integer,Result<String,Integer>> hundredMore = i -> Result.ofErr(i+100);
        // then:
        assertEquals(
                MTEST_OK.flatMap( delim ).flatMap( concat ),
                MTEST_OK.flatMap( s -> delim.apply( s ).flatMap( concat ) )
        );
        // and for a right Either:
        assertEquals(
                MTEST_ERR.flatMapErr( hundredMore ).flatMapErr( square ),
                MTEST_ERR.flatMapErr( s -> hundredMore.apply( s ).flatMapErr( square ) )
        );
    }
}