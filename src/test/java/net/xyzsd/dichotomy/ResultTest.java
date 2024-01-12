package net.xyzsd.dichotomy;

import static net.xyzsd.dichotomy.Result.OK;
import static net.xyzsd.dichotomy.Result.Err;

import org.junit.jupiter.api.Test;
import net.xyzsd.dichotomy.TestUtils.SingleUseConsumer;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import static net.xyzsd.dichotomy.TestUtils.*;
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



    @Test
    void ofOK() {
        assertEquals( OK.class, OK.getClass() );
        assertEquals( OK_VALUE, ((OK<String, RuntimeException>) OK).get() );

        assertThrows( NullPointerException.class, () -> {Result.ofOK( null );} );
    }

    @Test
    void ofOK_NONE() {
        final Result<Empty, Object> result = Result.ofOK();
        assertEquals( OK.class, result.getClass() );
        assertEquals( Empty.getInstance(), ((OK<Empty, Object>) result).get() );
    }

    @Test
    void ofNullable() {
        // this test assumes that other method tests pass
        Result<String, Empty> resultFromNull = Result.ofNullable( null );
        assertInstanceOf( Err.class, resultFromNull );

        Result<String, Empty> resultNotNull = Result.ofNullable( OK_VALUE );
        assertInstanceOf( OK.class, resultNotNull );
    }


    @Test
    void from() {
        // this test assumes that other method tests pass
        Result<String, Empty> resultFromEmpty = Result.from( Optional.empty() );
        assertInstanceOf( Err.class, resultFromEmpty );

        Result<String, Empty> result = Result.from( Optional.of( OK_VALUE ) );
        assertInstanceOf( OK.class, result );
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
    void isOK() {
        assertTrue( OK.isOK() );
        assertFalse( ERR.isOK() );
    }

    @Test
    void isErr() {
        assertFalse( OK.isErr() );
        assertTrue( ERR.isErr() );
    }


    @Test
    void ok() {
        Result<String, RuntimeException> resultERR = Result.ofErr( ERR_VALUE );
        assertTrue( resultERR.ok().isEmpty() );

        Result<String, RuntimeException> resultOK = Result.ofOK( OK_VALUE );
        assertTrue( resultOK.ok().isPresent() );
        assertEquals( resultOK.ok().get(),  OK_VALUE );
    }

    @Test
    void err() {
        Result<String, RuntimeException> resultERR = Result.ofErr( ERR_VALUE );
        assertTrue( resultERR.err().isPresent() );
        assertEquals( resultERR.err().get(),  ERR_VALUE );

        Result<String, RuntimeException> resultOK = Result.ofOK( OK_VALUE );
        assertTrue( resultOK.err().isEmpty() );
    }

    @Test
    void swap() {
        Result<String, String> left = Result.ofOK( "ok!" );
        assertTrue( left.contains( "ok!" ) );
        Result<String, String> leftSwap = left.swap();      // ok -> err (left->right)
        assertTrue( leftSwap.containsErr( "ok!" ) );
        assertTrue( left.contains( "ok!" ) );   // ensure original unchanged

        Result<String, String> right = Result.ofErr( "value!" );
        assertTrue( right.containsErr( "value!" ) );
        Result<String, String> rightSwap = right.swap();
        assertTrue( rightSwap.contains( "value!" ) );
        assertTrue( rightSwap.contains( "value!" ) );
        assertTrue( right.containsErr( "value!" ) ); // ensure original unchanged
    }


    @Test
    void biMatch() {
        {
            SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
            SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();
            //
            OK.biMatch( okConsumer, errConsumer );
            assertTrue( okConsumer.usedJustOnce() );
            assertTrue( errConsumer.neverUsed() );
        }

        {
            SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
            SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();
            //
            ERR.biMatch( okConsumer, errConsumer );
            assertTrue( okConsumer.neverUsed() );
            assertTrue( errConsumer.usedJustOnce() );
        }
    }

    @Test
    void biMap() {
        Result<String, String> mapped = OK.biMap( fnOK, fnERR2STR );
        assertTrue( mapped.contains( fnOK_VAL ) );

        mapped = ERR.biMap( fnOK, fnERR2STR );
        assertTrue( mapped.containsErr( fnERR_VAL.getMessage() ) );

        // functions must not return null
        assertThrows( NullPointerException.class, () -> {
            OK.biMap( x -> null, fnERR2STR );
        } );

        assertThrows( NullPointerException.class, () -> {
            ERR.biMap( fnOK, x -> null );
        } );

        // unused side must not apply function
        assertDoesNotThrow( () -> OK.biMap( fnOK, neverFunction() ) );
        assertDoesNotThrow( () -> ERR.biMap( neverFunction(), fnERR2STR ) );
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

        assertDoesNotThrow( () -> OK.biFlatMap( fnOK_FLAT, neverFunction() ) );
        assertDoesNotThrow( () -> ERR.biFlatMap( neverFunction(), fnERR_FLAT ) );
    }

    @Test
    void fold() {
        assertEquals( fnOK_VAL, OK.fold( fnOK, neverFunction() ) );
        assertEquals( fnERR_VAL, ERR.fold( neverFunction(), fnERR2ERR ) );

        // null-returning function test
        assertThrows( NullPointerException.class, () -> {
            OK.fold( x -> null, neverFunction() );
        } );

        assertThrows( NullPointerException.class, () -> {
            ERR.fold( neverFunction(), x -> null );
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
        assertEquals( ERR, ERR.filter( x -> true, neverFunction() ) );
        assertEquals( ERR, ERR.filter( x -> false, neverFunction() ) );

        // OK->OK if predicate matches
        assertEquals( OK, OK.filter( x -> true, neverFunction() ) );

        // OK->Err if predicate doesn't match
        assertEquals( Result.ofErr( fnSTR2ERR_VAL ), OK.filter( x -> false, fnSTR2ERR ) );
    }

    @Test
    void match() {
        SingleUseConsumer<String> okConsumer = new SingleUseConsumer<>();
        OK.match( okConsumer );
        assertTrue( okConsumer.usedJustOnce() );

        SingleUseConsumer<String> errConsumer = new SingleUseConsumer<>();
        ERR.match( errConsumer );
        assertTrue( errConsumer.neverUsed() );
    }

    @Test
    void consume() {
        assertThrows( NullPointerException.class, () -> ERR.consume( null ) );
        assertThrows( NullPointerException.class, () -> OK.consume( null ) );
        assertDoesNotThrow( () -> ERR.consume( neverConsumer() ) );
        //
        // exceptions in consumers are thrown
        assertThrows( ArithmeticException.class,
                () -> OK.consume( s -> {throw new ArithmeticException("!"); } ) );

        //
        SingleUseConsumer<String> consumer = new SingleUseConsumer<>();
        OK.consume( consumer );
        assertTrue( consumer.usedJustOnce() );
    }

    @Test
    void map() {
        assertEquals( Result.ofOK( fnOK_VAL ), OK.map( fnOK ) );
        assertEquals( ERR, ERR.map( fnOK ) );
        assertDoesNotThrow( () -> {ERR.map( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> OK.map( x -> null ) );
    }

    @Test
    void flatMap() {
        assertEquals( Result.ofOK( fnOK_VAL ), OK.flatMap( fnOK_FLAT ) );
        assertEquals( ERR, ERR.flatMap( fnOK_FLAT ) );
        assertDoesNotThrow( () -> {ERR.flatMap( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> OK.flatMap( x -> null ) );
    }

    @Test
    void matches() {
        assertTrue( OK.ifPredicate( x -> true ) );
        assertFalse( OK.ifPredicate( x -> false ) );

        assertFalse( ERR.ifPredicate( x -> true ) );
        assertFalse( ERR.ifPredicate( x -> false ) );
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
        assertEquals( OK_VALUE, OK.orElse( neverSupplier() ) );
        assertEquals( "orElse-alternate", ERR.orElse( () -> "orElse-alternate" ) );
    }

    @Test
    void recover() {
        assertEquals( OK_VALUE, OK.recover( neverFunction() ) );
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
        SingleUseConsumer<RuntimeException> errConsumer = new SingleUseConsumer<>();
        ERR.matchErr( errConsumer );
        assertTrue( errConsumer.usedJustOnce() );

        SingleUseConsumer<RuntimeException> okConsumer = new SingleUseConsumer<>();
        OK.matchErr( okConsumer );
        assertTrue( okConsumer.neverUsed() );
    }

    @Test
    void mapErr() {
        assertEquals( Result.ofErr( fnERR_VAL ), ERR.mapErr( fnERR2ERR ) );
        assertEquals( OK, OK.mapErr( fnERR2ERR ) );
        assertDoesNotThrow( () -> {OK.mapErr( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> ERR.mapErr( x -> null ) );
    }

    @Test
    void flatMapErr() {
        assertEquals( Result.ofErr( fnERR_VAL ), ERR.flatMapErr( fnERR_FLAT ) );
        assertEquals( OK, OK.flatMapErr( fnERR_FLAT ) );
        assertDoesNotThrow( () -> {OK.flatMapErr( neverFunction() );} );
        assertThrows( NullPointerException.class, () -> ERR.flatMapErr( x -> null ) );
    }

    @Test
    void matchesErr() {
        assertTrue( ERR.ifPredicateErr( x -> true ) );
        assertFalse( ERR.ifPredicateErr( x -> false ) );

        assertFalse( OK.ifPredicateErr( x -> true ) );
        assertFalse( OK.ifPredicateErr( x -> false ) );
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
        assertEquals( ERR_VALUE, ERR.orElseErr( neverSupplier() ) );
        assertEquals( fnERR_VAL, OK.orElseErr( () -> fnERR_VAL ) );
    }

    @Test
    void forfeit() {
        assertEquals( ERR_VALUE, ERR.forfeit( neverFunction() ) );
        assertEquals( fnSTR2ERR_VAL, OK.forfeit( fnSTR2ERR ) );
    }

    @Test
    void and() {
        assertEquals( ERR, ERR.and( Result.ofOK( "nextResult" ) ) );
        assertEquals( Result.ofOK( "nextResult" ), OK.and( Result.ofOK( "nextResult" ) ) );
    }

    @Test
    void testAnd() {
        assertEquals( ERR, ERR.and( neverSupplier() ) );

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
        assertEquals( OK, OK.or( neverSupplier() ) );

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
                NoSuchElementException.class,
                () -> Result.ofErr(new IOException()).expect()
        );
        assertThrows(
                NoSuchElementException.class,
                () -> Result.ofErr(new AssertionError()).expect()
        );

        // non-exceptional values
        assertThrows(
                NoSuchElementException.class,
                () -> Result.ofErr(666).expect()
        );

        try {
            Result.ofErr(666).expect();
        } catch(NoSuchElementException e) {
            assertEquals( "666", e.getMessage() );
        }
    }

    @Test
    void orThrowWrapped() {
        final String s = assertDoesNotThrow( () -> OK.getOrThrow( neverFunction() ) );
        assertEquals( OK_VALUE, s );

        assertThrows(
                IOException.class,
                () -> ERR.getOrThrow( IOException::new )
        );
        assertThrows(
                RuntimeException.class,
                () -> ERR.getOrThrow(  RuntimeException::new )
        );

        // but also see this
        final String MESSAGE = "My Error Message";
        final Result<?,String> result = Result.ofErr( MESSAGE );
        assertThrows(
                IOException.class,
                () -> result.getOrThrow( IOException::new )
        );

        try {
            result.getOrThrow( IOException::new );
        } catch(IOException e) {
            assertEquals( MESSAGE, e.getMessage() );
        }
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

    @Test
    void highlyContrivedAndNotVeryGoodExamples() {
        // OK: first example
        final Result<Double, String> firstResult = Result.<Integer, String>ofOK( 3828 )  // returns an OK<Integer>
                .map( x -> x * 10.0 )        // map to float, after multiplying x 10
                .match( System.out::println )     // print "38280.0" to console
                .matchErr( System.err::println );// ignored, as this is an OK

        // prints 'value ok!'
        switch(firstResult) {
            case OK<Double,String> ok -> System.out.println("value ok! value: "+ok.value());
            case Err<Double,String> err -> System.err.println(err.value());
        }

        // prints 'positive'
        switch(firstResult) {
            case OK(Double x) when x > 0 -> System.out.println("positive");
            case OK(Double x) -> System.out.println("0 or negative");
            case Err(String s) -> System.err.println(s);
        }

        // Err: second example
        Result<Double,String> errResult = Result.<Integer, String>ofErr("Insufficient entropy")
                .map(x -> x*10.0 )       // ignored, as this is an Err
                .match(System.out::println)     // ignored, as this is an Err
                .matchErr(System.err::println);  // "Insufficient entropy" printed to System.err

        // prints 'ERROR: Insufficient entropy'
        switch(errResult) {
            case OK<Double,String> ok -> System.out.println("value ok! value: "+ok.value());
            case Err<Double,String> err -> System.err.printf("ERROR: %s\n", err.value());
        }

        // prints 'ERROR: Insufficient entropy'
        switch(errResult) {
            case OK(Double x) when x > 0 -> System.out.println("positive");
            case OK(Double x) -> System.out.println("0 or negative");
            case Err(String s) -> System.err.printf("ERROR: %s\n", s);
        }
    }
}