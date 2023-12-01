package net.xyzsd.dichotomy;

import net.xyzsd.dichotomy.util.Conversion;
import org.junit.jupiter.api.Test;

import static net.xyzsd.dichotomy.TestUtils.neverFunction;
import static net.xyzsd.dichotomy.TestUtils.neverSupplier;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public class EitherTest {

    static final String LVAL = "left";
    static final int RVAL = 42;
    static final Either<String, Integer> LEFT = Either.ofLeft( LVAL );
    static final Either<String, Integer> RIGHT = Either.ofRight( RVAL );


    @Test
    void ofLeft() {
        assertEquals( Either.Left.class, LEFT.getClass() );
        assertEquals( LVAL, ((Either.Left<String, Integer>) LEFT).get() );
    }

    @Test
    void ofRight() {
        assertEquals( Either.Right.class, RIGHT.getClass() );
        assertEquals( RVAL, ((Either.Right<String, Integer>) RIGHT).get() );
    }

    @Test
    void fromResult() {
        assertEquals( RIGHT, Conversion.toEither( Result.ofOK( RVAL ) ) );
        assertEquals( LEFT, Conversion.toEither( Result.ofErr( LVAL ) ) );
    }

    @Test
    void left() {
        assertEquals( Optional.of( LVAL ), LEFT.left() );
        assertTrue( LEFT.right().isEmpty() );
    }

    @Test
    void right() {
        assertEquals( Optional.of( RVAL ), RIGHT.right() );
        assertTrue( RIGHT.left().isEmpty() );
    }


    // match(Consumer,Consumer)
    @Test
    void biMatch() {
        {
            TestUtils.SingleUseConsumer<String> leftConsumer = new TestUtils.SingleUseConsumer<>();
            TestUtils.SingleUseConsumer<Integer> rightConsumer = new TestUtils.SingleUseConsumer<>();
            //
            LEFT.biMatch( leftConsumer, rightConsumer );
            assertTrue( leftConsumer.usedJustOnce() );
            assertTrue( rightConsumer.neverUsed() );
        }


        {
            TestUtils.SingleUseConsumer<String> leftConsumer = new TestUtils.SingleUseConsumer<>();
            TestUtils.SingleUseConsumer<Integer> rightConsumer = new TestUtils.SingleUseConsumer<>();
            //
            RIGHT.biMatch( leftConsumer, rightConsumer );
            assertTrue( leftConsumer.neverUsed() );
            assertTrue( rightConsumer.usedJustOnce() );
        }
    }

    @Test
    void biMap() {
        Function<String, String> fnSS = s -> "STRING";
        Function<Integer, String> fnIS = i -> "INT";

        Either<String, String> mapped = LEFT.biMap( fnSS, fnIS );
        assertTrue( mapped.containsLeft( "STRING" ) );

        mapped = RIGHT.biMap( fnSS, fnIS );
        assertTrue( mapped.contains( "INT" ) );

        // functions must not return null
        assertThrows( NullPointerException.class, () -> {
            LEFT.biMap( x -> null, fnIS );
        } );

        assertThrows( NullPointerException.class, () -> {
            RIGHT.biMap( fnSS, x -> null );
        } );

        // and must not execute if not needed
        assertDoesNotThrow( () -> RIGHT.biMap( neverFunction(), fnIS ) );
        assertDoesNotThrow( () -> LEFT.biMap( fnSS, neverFunction() ) );
    }

    @Test
    void biFlatMap() {
        final Function<String, Either<Long, Long>> fnStoE = (x) -> Either.ofLeft( 1111L );
        final Function<Integer, Either<Long, Long>> fnIntToE = (x) -> Either.ofRight( 2222L );

        assertEquals( Either.ofLeft( 1111L ), LEFT.biFlatMap( fnStoE, fnIntToE ) );
        assertEquals( Either.ofRight( 2222L ), RIGHT.biFlatMap( fnStoE, fnIntToE ) );

        assertThrows( NullPointerException.class, () -> {
            LEFT.biFlatMap( x -> null, fnIntToE );
        } );

        assertThrows( NullPointerException.class, () -> {
            RIGHT.biFlatMap( fnStoE, x -> null );
        } );

        assertDoesNotThrow( () -> RIGHT.biFlatMap( neverFunction(), fnIntToE ) );
        assertDoesNotThrow( () -> LEFT.biFlatMap( fnStoE, neverFunction() ) );
    }

    @Test
    void fold() {
        Function<String, Long> fnSL = s -> 1111L;
        Function<Integer, Long> fnIL = i -> -9999L;

        long fold = LEFT.fold( fnSL, fnIL );
        assertEquals( 1111L, fold );

        fold = RIGHT.fold( fnSL, fnIL );
        assertEquals( -9999L, fold );

        // null-returning function test
        assertThrows( NullPointerException.class, () -> {
            LEFT.fold( x -> null, fnIL );
        } );

        assertThrows( NullPointerException.class, () -> {
            RIGHT.fold( fnSL, x -> null );
        } );

        assertDoesNotThrow( () -> LEFT.fold( fnSL, neverFunction() ) );
        assertDoesNotThrow( () -> RIGHT.fold( neverFunction(), fnIL ) );
    }

    @Test
    void filter() {
        final Function<Integer, String> fn = x -> "FUNCTION";

        // left returns left, always
        assertEquals( LEFT, LEFT.filter( x -> true, fn ) );
        assertEquals( LEFT, LEFT.filter( x -> false, fn ) );
        assertDoesNotThrow( () -> LEFT.filter( x -> true, neverFunction() ) );
        assertDoesNotThrow( () -> LEFT.filter( x -> false, neverFunction() ) );

        // right returns right, if predicate matches
        assertEquals( RIGHT, RIGHT.filter( x -> true, fn ) );
        assertDoesNotThrow( () -> RIGHT.filter( x -> true, neverFunction() ) );

        // right returns mapper result, if predicate fails
        final Either<String, Integer> filter = RIGHT.filter( x -> false, fn );
        assertTrue( filter.containsLeft( "FUNCTION" ) );
    }

    @Test
    void forfeit() {
        final Function<Integer, String> fn = x -> "FUNCTION";
        assertEquals( LVAL, LEFT.forfeit( fn ) );
        assertEquals( "FUNCTION", RIGHT.forfeit( fn ) );
        assertDoesNotThrow( () -> LEFT.forfeit( neverFunction() ) );
    }

    @Test
    void recover() {
        final Function<String, Integer> fn = x -> 999;
        assertEquals( 999, LEFT.recover( fn ) );
        assertEquals( RVAL, RIGHT.recover( fn ) );
        assertDoesNotThrow( () -> RIGHT.recover( neverFunction() ) );
    }

    @Test
    void stream() {
        assertEquals( 1, RIGHT.stream().count() );
        assertTrue( RIGHT.stream().allMatch( (i) -> (i == RVAL) ) );
        assertEquals( 0, RIGHT.streamLeft().count() );
    }

    @Test
    void streamLeft() {
        assertEquals( 1, LEFT.streamLeft().count() );
        assertTrue( LEFT.streamLeft().allMatch( (s) -> s.equals( LVAL ) ) );
        assertEquals( 0, LEFT.stream().count() );
    }

    @Test
    void contains() {
        assertTrue( RIGHT.contains( 42 ) );
        assertFalse( RIGHT.contains( 1000 ) );
    }

    @Test
    void containsLeft() {
        assertTrue( LEFT.containsLeft( LVAL ) );
        assertFalse( LEFT.containsLeft( "" ) );
    }

    @Test
    void matches() {
        assertTrue( RIGHT.ifPredicate( x -> true ) );
        assertTrue( RIGHT.ifPredicate( x -> (x == RVAL) ) );
        assertFalse( RIGHT.ifPredicate( x -> false ) );

        // left Eithers should NEVER match any predicate for the right either (matches() is right biased)
        assertFalse( LEFT.ifPredicate( x -> true ) );
        assertFalse( LEFT.ifPredicate( x -> (x == RVAL) ) );
        assertFalse( LEFT.ifPredicate( x -> false ) );
    }

    @Test
    void matchesLeft() {
        // right Eithers should NEVER match any predicate for the left Either
        assertFalse( RIGHT.ifPredicateLeft( x -> true ) );
        assertFalse( RIGHT.ifPredicateLeft( LVAL::equals ) );
        assertFalse( RIGHT.ifPredicateLeft( x -> false ) );

        assertTrue( LEFT.ifPredicateLeft( x -> true ) );
        assertTrue( LEFT.ifPredicateLeft( LVAL::equals ) );
        assertFalse( LEFT.ifPredicateLeft( x -> false ) );
    }

    // right-biased map
    @Test
    void testMap() {
        Function<Integer, String> fnIS = i -> "INT";

        assertEquals( Either.ofLeft( LVAL ), LEFT.map( fnIS ) );
        assertDoesNotThrow( () -> LEFT.map( neverFunction() ) );

        assertEquals( Either.ofRight( "INT" ), RIGHT.map( fnIS ) );
        assertThrows( NullPointerException.class, () -> RIGHT.map( x -> null ) );

    }

    // right-biased flatmap
    @Test
    void testFlatMap() {
        final Function<Integer, Either<String, Long>> fnIntToE = (x) -> Either.ofRight( 2222L );

        assertEquals( Either.ofLeft( LVAL ), LEFT.flatMap( fnIntToE ) );
        assertDoesNotThrow( () -> LEFT.flatMap( neverFunction() ) );

        assertEquals( Either.ofRight( 2222L ), RIGHT.flatMap( fnIntToE ) );
        assertThrows( NullPointerException.class, () -> RIGHT.flatMap( x -> null ) );
    }

    @Test
    void mapLeft() {
        Function<String, String> fnSS = s -> "STRING";

        assertEquals( Either.ofRight( RVAL ), RIGHT.mapLeft( fnSS ) );
        assertDoesNotThrow( () -> RIGHT.mapLeft( neverFunction() ) );

        assertEquals( Either.ofLeft( "STRING" ), LEFT.mapLeft( fnSS ) );
        assertThrows( NullPointerException.class, () -> LEFT.mapLeft( x -> null ) );
    }

    @Test
    void flatMapLeft() {
        final Function<String, Either<Long, Integer>> fnStoE = (x) -> Either.ofLeft( 1111L );

        assertEquals( Either.ofLeft( 1111L ), LEFT.flatMapLeft( fnStoE ) );
        assertThrows( NullPointerException.class, () -> LEFT.flatMapLeft( x -> null ) );

        assertEquals( Either.ofRight( RVAL ), RIGHT.flatMapLeft( fnStoE ) );
        assertDoesNotThrow( () -> RIGHT.flatMapLeft( neverFunction() ) );
    }

    @Test
    void matchLeft() {

        final TestUtils.SingleUseConsumer<String> leftConsumer = new TestUtils.SingleUseConsumer<>();
        LEFT.matchLeft( leftConsumer );
        assertTrue( leftConsumer.usedJustOnce() );

        final TestUtils.SingleUseConsumer<String> rightConsumer = new TestUtils.SingleUseConsumer<>();
        RIGHT.matchLeft( rightConsumer );
        assertTrue( rightConsumer.neverUsed() );
    }

    // match(Consumer) with right bias
    @Test
    void testMatch() {
        final TestUtils.SingleUseConsumer<Integer> rightConsumer = new TestUtils.SingleUseConsumer<>();
        RIGHT.match( rightConsumer );
        assertTrue( rightConsumer.usedJustOnce() );

        final TestUtils.SingleUseConsumer<Integer> leftConsumer = new TestUtils.SingleUseConsumer<>();
        LEFT.match( leftConsumer );
        assertTrue( leftConsumer.neverUsed() );
    }

    @Test
    void orElse() {
        assertEquals( RVAL, RIGHT.orElse( 222 ) );
        assertEquals( 222, LEFT.orElse( 222 ) );
    }

    @Test
    void orElseLeft() {
        assertEquals( "ALTERNATE", RIGHT.orElseLeft( "ALTERNATE" ) );
        assertEquals( LVAL, LEFT.orElseLeft( "ALTERNATE" ) );
    }

    @Test
    void orElseGet() {
        // correctness
        assertEquals( RVAL, RIGHT.orElseGet( () -> 222 ) );
        assertEquals( 222, LEFT.orElseGet( () -> 222 ) );

        // efficiency
        assertEquals( RVAL, RIGHT.orElseGet( neverSupplier() ) );
    }

    @Test
    void orElseGetLeft() {
        // correctness
        assertEquals( "ALTERNATE", RIGHT.orElseGetLeft( () -> "ALTERNATE" ) );
        assertEquals( LVAL, LEFT.orElseGetLeft( () -> "ALTERNATE" ) );

        // efficiency
        assertEquals( LVAL, LEFT.orElseGetLeft( neverSupplier() ) );
    }

    @Test
    void and() {
        assertEquals( LEFT, LEFT.and( Either.ofRight( "RIGHT_STRING" ) ) );

        // if RIGHT, return next either given; left stays left
        assertEquals( Either.ofRight( "RIGHT_STRING" ),
                RIGHT.and( Either.ofRight( "RIGHT_STRING" ) )
        );
    }

    @Test
    void testAnd() {
        assertEquals( LEFT, LEFT.and( neverSupplier() ) );

        assertEquals( Either.ofRight( "RIGHT_STRING" ),
                RIGHT.and( () -> Either.ofRight( "RIGHT_STRING" ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> RIGHT.and( () -> null )
        );
    }

    @Test
    void or() {
        assertEquals( Either.ofLeft( 555L ),
                LEFT.or( Either.ofLeft( 555L ) )
        );

        assertEquals( RIGHT, RIGHT.or( LEFT ) );
    }

    @Test
    void testOr() {
        assertEquals( Either.ofLeft( 555L ),
                LEFT.or( () -> Either.ofLeft( 555L ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> LEFT.or( () -> null )
        );

        assertEquals( RIGHT, RIGHT.or( neverSupplier() ) );
    }

    @Test
    void getOrThrow() {
        assertThrows(
                ArithmeticException.class,
                () -> LEFT.expect( ArithmeticException::new )
        );

        assertEquals( RVAL, RIGHT.expect( ArithmeticException::new ) );
    }

    @Test
    void getOrThrowWrapped() {
        assertThrows(
                ArithmeticException.class,
                () -> LEFT.getOrThrow( ArithmeticException::new )
        );

        try {
            LEFT.getOrThrow( IOException::new );
        } catch (IOException e) {
            // getOrThrowWrapped() calls the IOException(String) constructor,
            // since LEFT is a String type, rather than the no-arg IOException
            // constructor, as getOrThrow() via a Supplier would.
            assertEquals( LVAL, e.getMessage() );
        }

        assertEquals( RVAL, RIGHT.getOrThrow( ArithmeticException::new ) );
    }

    @Test
    void swap() {
        Either<String, String> left = Either.ofLeft( "left" );
        Either<String, String> leftSwap = left.swap();
        assertTrue( leftSwap.contains( "left" ) );

        Either<String, String> right = Either.ofRight( "right" );
        Either<String, String> rightSwap = right.swap();
        assertTrue( rightSwap.containsLeft( "right" ) );
    }

    @Test
    void toResult() {
        assertEquals( Result.ofErr( LVAL ), Conversion.toResult( LEFT ) );
        assertEquals( Result.ofOK( RVAL ), Conversion.toResult( RIGHT ) );
    }

    // because we don't want illegal monads
    @Test
    void monadicLaws() {
        // NOTE: the terms 'left identity' and 'right identity' below, are general terms for monad properties,
        //       and do not refer to right and left Eithers!

        // FIRST LAW: LEFT IDENTITY
        // Simply: data is wrapped, not manipulated/changed.
        //
        // Given An Either with a value in it and a function that takes the same type of value and
        // returns the same type of Either, then applying the function to the value should
        // be equivalent to flatMapping on the Either.
        //
        // given: LVAL (our value) [already defined]
        // given: LEFT (our Either which wraps LVAL) [already defined]
        // given: the following function:
        final Function<String, Either<String, Integer>> concat = s -> Either.ofLeft( s + s );
        // then the following should be equivalent:
        assertEquals(
                concat.apply( LVAL ),
                LEFT.flatMapLeft( concat )
        );

        // and similarly, for right Eithers:
        final Function<Integer, Either<String, Integer>> square = i -> Either.ofRight( i * i );
        assertEquals(
                square.apply( RVAL ),
                RIGHT.flatMap( square )
        );

        // SECOND LAW: RIGHT IDENTITY
        // Simply: data is wrapped, not manipulated/changed.
        //
        // If we flatMap() an Either in the same type of Either, without
        // changing the value, the value should be unchanged.
        assertEquals(
                LEFT,
                LEFT.flatMapLeft( Either::ofLeft )
        );

        assertEquals(
                LEFT,
                LEFT.flatMap( Either::ofRight )
        );

        assertEquals(
                RIGHT,
                RIGHT.flatMap( Either::ofRight )
        );

        assertEquals(
                RIGHT,
                RIGHT.flatMapLeft( Either::ofLeft )
        );

        // THIRD LAW: ASSOCIATIVITY
        // flatmap nesting order should not matter
        // given: functions square & concat from above (for right and left monads, respectively)
        // given: the following functions:
        final Function<String, Either<String, Integer>> delim = s -> Either.ofLeft( s + "," + s );
        final Function<Integer, Either<String, Integer>> hundredMore = i -> Either.ofRight( i + 100 );
        // then:
        assertEquals(
                LEFT.flatMapLeft( delim ).flatMapLeft( concat ),
                LEFT.flatMapLeft( s -> delim.apply( s ).flatMapLeft( concat ) )
        );
        // and for a right Either:
        assertEquals(
                RIGHT.flatMap( hundredMore ).flatMap( square ),
                RIGHT.flatMap( s -> hundredMore.apply( s ).flatMap( square ) )
        );

    }

    @Test
    void swapEquivalences() {
        final Either<String, Integer> original = Either.ofLeft( "left" );
        final Either<Integer, String> swapped = original.swap();

        // could do this for multiple methods....
        // as an example, the following should be equivalent:
        assertEquals(
                original.forfeit( neverFunction() ),
                swapped.recover( neverFunction() )
        );
    }


}