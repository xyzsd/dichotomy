package net.xyzsd.dichotomy.either;

import net.xyzsd.dichotomy.result.Result;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EitherTest {

    static final String LVAL = "left";
    static final int RVAL = 42;
    static final Either<String, Integer> LEFT = Either.ofLeft( LVAL );
    static final Either<String, Integer> RIGHT = Either.ofRight( RVAL );


    public static class SingleUseConsumer<T> implements Consumer<T> {
        private final AtomicInteger useCount = new AtomicInteger();

        @Override
        public final void accept(T t) {
            useCount.incrementAndGet();
        }

        public final boolean wasActivatedOnce() {
            return (useCount.get() == 1);
        }
    }


    // 'never' types: check if implementation calls a supplier / function when it doesn't need to
    // Using rawtypes is easier. if the cast fails... we have an error anyway
    @SuppressWarnings("rawtypes")
    public static final Supplier NEVERSUPPLIER = () -> {throw new IllegalStateException( "NEVERSUPPLIER::get invoked!" );};

    @SuppressWarnings("rawtypes")
    public static final Function NEVERFUNCTION = (x) -> {throw new IllegalStateException( "NEVERFUNCTION::apply invoked!" );};


    @org.junit.jupiter.api.Test
    void ofLeft() {
        assertEquals( Left.class, LEFT.getClass() );
        assertEquals( LVAL, ((Left<String, Integer>) LEFT).get() );
    }

    @org.junit.jupiter.api.Test
    void ofRight() {
        assertEquals( Right.class, RIGHT.getClass() );
        assertEquals( RVAL, ((Right<String, Integer>) RIGHT).get() );
    }

    @org.junit.jupiter.api.Test
    void fromResult() {
        assertEquals( RIGHT, Either.fromResult( Result.ofOK(RVAL)) );
        assertEquals( LEFT, Either.fromResult( Result.ofErr(LVAL)) );
    }

    @org.junit.jupiter.api.Test
    void left() {
        assertEquals( Optional.of( LVAL ), LEFT.left() );
        assertTrue( LEFT.right().isEmpty() );
    }

    @org.junit.jupiter.api.Test
    void right() {
        assertEquals( Optional.of( RVAL ), RIGHT.right() );
        assertTrue( RIGHT.left().isEmpty() );
    }

    @org.junit.jupiter.api.Test
    void unwrap() {
        assertEquals( LVAL, LEFT.unwrap() );
        assertEquals( RVAL, RIGHT.unwrap() );
    }

    @org.junit.jupiter.api.Test
    void isLeft() {
        assertTrue( LEFT.isLeft() );
        assertFalse( LEFT.isRight() );
    }

    @org.junit.jupiter.api.Test
    void isRight() {
        assertTrue( RIGHT.isRight() );
        assertFalse( RIGHT.isLeft() );
    }

    // match(Consumer,Consumer)
    @org.junit.jupiter.api.Test
    void biMatch() {
        SingleUseConsumer<String> leftConsumer = new SingleUseConsumer<>();
        SingleUseConsumer<Integer> rightConsumer = new SingleUseConsumer<>();

        LEFT.biMatch( leftConsumer, rightConsumer );
        assertTrue( leftConsumer.wasActivatedOnce() );
        assertFalse( rightConsumer.wasActivatedOnce() );

        RIGHT.biMatch( leftConsumer, rightConsumer );
        assertTrue( leftConsumer.wasActivatedOnce() );
        assertTrue( rightConsumer.wasActivatedOnce() );
    }

    @org.junit.jupiter.api.Test
    void biMap() {
        Function<String, String> fnSS = s -> "STRING";
        Function<Integer, String> fnIS = i -> "INT";

        Either<String, String> mapped = LEFT.biMap( fnSS, fnIS );
        assertTrue( mapped.isLeft() );
        assertTrue( mapped.containsLeft( "STRING" ) );

        mapped = RIGHT.biMap( fnSS, fnIS );
        assertTrue( mapped.isRight() );
        assertTrue( mapped.contains( "INT" ) );

        // functions must not return null
        assertThrows( NullPointerException.class, () -> {
            LEFT.biMap( x -> null, fnIS );
        } );

        assertThrows( NullPointerException.class, () -> {
            RIGHT.biMap( fnSS, x -> null );
        } );

        // and must not execute if not needed
        assertDoesNotThrow( () -> RIGHT.biMap( NEVERFUNCTION, fnIS ) );
        assertDoesNotThrow( () -> LEFT.biMap( fnSS, NEVERFUNCTION ) );
    }

    @org.junit.jupiter.api.Test
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

        assertDoesNotThrow( () -> RIGHT.biFlatMap( NEVERFUNCTION, fnIntToE ) );
        assertDoesNotThrow( () -> LEFT.biFlatMap( fnStoE, NEVERFUNCTION ) );
    }

    @org.junit.jupiter.api.Test
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

        assertDoesNotThrow( () -> LEFT.fold( fnSL, NEVERFUNCTION ) );
        assertDoesNotThrow( () -> RIGHT.fold( NEVERFUNCTION, fnIL ) );
    }

    @org.junit.jupiter.api.Test
    void filter() {
        final Function<Integer, String> fn = x -> "FUNCTION";

        // left returns left, always
        assertEquals( LEFT, LEFT.filter( x -> true, fn ) );
        assertEquals( LEFT, LEFT.filter( x -> false, fn ) );
        assertDoesNotThrow( () -> LEFT.filter( x -> true, NEVERFUNCTION ) );
        assertDoesNotThrow( () -> LEFT.filter( x -> false, NEVERFUNCTION ) );

        // right returns right, if predicate matches
        assertEquals( RIGHT, RIGHT.filter( x -> true, fn ) );
        assertDoesNotThrow( () -> RIGHT.filter( x -> true, NEVERFUNCTION ) );

        // right returns mapper result, if predicate fails
        final Either<String, Integer> filter = RIGHT.filter( x -> false, fn );
        assertTrue( filter.containsLeft( "FUNCTION" ) );
    }

    @org.junit.jupiter.api.Test
    void forfeit() {
        final Function<Integer, String> fn = x -> "FUNCTION";
        assertEquals( LVAL, LEFT.forfeit( fn ) );
        assertEquals( "FUNCTION", RIGHT.forfeit( fn ) );
        assertDoesNotThrow( () -> LEFT.forfeit( NEVERFUNCTION ) );
    }

    @org.junit.jupiter.api.Test
    void recover() {
        final Function<String, Integer> fn = x -> 999;
        assertEquals( 999, LEFT.recover( fn ) );
        assertEquals( RVAL, RIGHT.recover( fn ) );
        assertDoesNotThrow( () -> RIGHT.recover( NEVERFUNCTION ) );
    }

    @org.junit.jupiter.api.Test
    void stream() {
        assertEquals( 1, RIGHT.stream().count() );
        assertTrue( RIGHT.stream().allMatch( (i) -> (i == RVAL) ) );
        assertEquals( 0, RIGHT.streamLeft().count() );
    }

    @org.junit.jupiter.api.Test
    void streamLeft() {
        assertEquals( 1, LEFT.streamLeft().count() );
        assertTrue( LEFT.streamLeft().allMatch( (s) -> s.equals( LVAL ) ) );
        assertEquals( 0, LEFT.stream().count() );
    }

    @org.junit.jupiter.api.Test
    void contains() {
        assertTrue( RIGHT.contains( 42 ) );
        assertFalse( RIGHT.contains( 1000 ) );
    }

    @org.junit.jupiter.api.Test
    void containsLeft() {
        assertTrue( LEFT.containsLeft( LVAL ) );
        assertFalse( LEFT.containsLeft( "" ) );
    }

    @org.junit.jupiter.api.Test
    void matches() {
        assertTrue( RIGHT.matches( x -> true ) );
        assertTrue( RIGHT.matches( x -> (x == RVAL) ) );
        assertFalse( RIGHT.matches( x -> false ) );

        // left Eithers should NEVER match any predicate for the right either (matches() is right biased)
        assertFalse( LEFT.matches( x -> true ) );
        assertFalse( LEFT.matches( x -> (x == RVAL) ) );
        assertFalse( LEFT.matches( x -> false ) );
    }

    @org.junit.jupiter.api.Test
    void matchesLeft() {
        // right Eithers should NEVER match any predicate for the left Either
        assertFalse( RIGHT.matchesLeft( x -> true ) );
        assertFalse( RIGHT.matchesLeft( LVAL::equals ) );
        assertFalse( RIGHT.matchesLeft( x -> false ) );

        assertTrue( LEFT.matchesLeft( x -> true ) );
        assertTrue( LEFT.matchesLeft( LVAL::equals ) );
        assertFalse( LEFT.matchesLeft( x -> false ) );
    }

    // right-biased map
    @org.junit.jupiter.api.Test
    void testMap() {
        Function<Integer, String> fnIS = i -> "INT";

        assertEquals( Either.ofLeft( LVAL ), LEFT.map( fnIS ) );
        assertDoesNotThrow( () -> LEFT.map( NEVERFUNCTION ) );

        assertEquals( Either.ofRight( "INT" ), RIGHT.map( fnIS ) );
        assertThrows( NullPointerException.class, () -> RIGHT.map( x -> null ) );

    }

    // right-biased flatmap
    @org.junit.jupiter.api.Test
    void testFlatMap() {
        final Function<Integer, Either<String, Long>> fnIntToE = (x) -> Either.ofRight( 2222L );

        assertEquals( Either.ofLeft( LVAL ), LEFT.flatMap( fnIntToE ) );
        assertDoesNotThrow( () -> LEFT.flatMap( NEVERFUNCTION ) );

        assertEquals( Either.ofRight( 2222L ), RIGHT.flatMap( fnIntToE ) );
        assertThrows( NullPointerException.class, () -> RIGHT.flatMap( x -> null ) );
    }

    @org.junit.jupiter.api.Test
    void mapLeft() {
        Function<String, String> fnSS = s -> "STRING";

        assertEquals( Either.ofRight( RVAL ), RIGHT.mapLeft( fnSS ) );
        assertDoesNotThrow( () -> RIGHT.mapLeft( NEVERFUNCTION ) );

        assertEquals( Either.ofLeft( "STRING" ), LEFT.mapLeft( fnSS ) );
        assertThrows( NullPointerException.class, () -> LEFT.mapLeft( x -> null ) );
    }

    @org.junit.jupiter.api.Test
    void flatMapLeft() {
        final Function<String, Either<Long, Integer>> fnStoE = (x) -> Either.ofLeft( 1111L );

        assertEquals( Either.ofLeft( 1111L ), LEFT.flatMapLeft( fnStoE ) );
        assertThrows( NullPointerException.class, () -> LEFT.flatMapLeft( x -> null ) );

        assertEquals( Either.ofRight( RVAL ), RIGHT.flatMapLeft( fnStoE ) );
        assertDoesNotThrow( () -> RIGHT.flatMapLeft( NEVERFUNCTION ) );
    }

    @org.junit.jupiter.api.Test
    void matchLeft() {
        final SingleUseConsumer<String> consumer = new SingleUseConsumer<>();

        LEFT.matchLeft( consumer );
        assertTrue( consumer.wasActivatedOnce() );

        RIGHT.matchLeft( consumer );
        assertTrue( consumer.wasActivatedOnce() );
    }

    // match(Consumer) with right bias
    @org.junit.jupiter.api.Test
    void testMatch() {
        final SingleUseConsumer<Integer> consumer = new SingleUseConsumer<>();

        RIGHT.match( consumer );
        assertTrue( consumer.wasActivatedOnce() );

        LEFT.match( consumer );
        assertTrue( consumer.wasActivatedOnce() );
    }

    @org.junit.jupiter.api.Test
    void orElse() {
        assertEquals( RVAL, RIGHT.orElse( 222 ) );
        assertEquals( 222, LEFT.orElse( 222 ) );
    }

    @org.junit.jupiter.api.Test
    void orElseLeft() {
        assertEquals( "ALTERNATE", RIGHT.orElseLeft( "ALTERNATE" ) );
        assertEquals( LVAL, LEFT.orElseLeft( "ALTERNATE" ) );
    }

    @org.junit.jupiter.api.Test
    void orElseGet() {
        // correctness
        assertEquals( RVAL, RIGHT.orElseGet( () -> 222 ) );
        assertEquals( 222, LEFT.orElseGet( () -> 222 ) );

        // efficiency
        assertEquals( RVAL, RIGHT.orElseGet( NEVERSUPPLIER ) );
    }

    @org.junit.jupiter.api.Test
    void orElseGetLeft() {
        // correctness
        assertEquals( "ALTERNATE", RIGHT.orElseGetLeft( () -> "ALTERNATE" ) );
        assertEquals( LVAL, LEFT.orElseGetLeft( () -> "ALTERNATE" ) );

        // efficiency
        assertEquals( LVAL, LEFT.orElseGetLeft( NEVERSUPPLIER ) );
    }

    @org.junit.jupiter.api.Test
    void and() {
        assertEquals( LEFT, LEFT.and( Either.ofRight( "RIGHT_STRING" ) ) );

        // if RIGHT, return next either given; left stays left
        assertEquals( Either.ofRight( "RIGHT_STRING" ),
                RIGHT.and( Either.ofRight( "RIGHT_STRING" ) )
        );
    }

    @org.junit.jupiter.api.Test
    void testAnd() {
        assertEquals( LEFT, LEFT.and( NEVERSUPPLIER ) );

        assertEquals( Either.ofRight( "RIGHT_STRING" ),
                RIGHT.and( () -> Either.ofRight( "RIGHT_STRING" ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> RIGHT.and( () -> null )
        );
    }

    @org.junit.jupiter.api.Test
    void or() {
        assertEquals( Either.ofLeft( 555L),
                LEFT.or(  Either.ofLeft( 555L ) )
        );

        assertEquals( RIGHT, RIGHT.or( LEFT ) );
    }

    @org.junit.jupiter.api.Test
    void testOr() {
        assertEquals( Either.ofLeft( 555L),
                LEFT.or( () -> Either.ofLeft( 555L ) )
        );

        assertThrows(
                NullPointerException.class,
                () -> LEFT.or( () -> null )
        );

        assertEquals( RIGHT, RIGHT.or( NEVERSUPPLIER ) );
    }

    @org.junit.jupiter.api.Test
    void getOrThrow() {
        assertThrows(
                ArithmeticException.class,
                () -> LEFT.getOrThrow( ArithmeticException::new )
        );

        assertEquals( RVAL, RIGHT.getOrThrow( ArithmeticException::new ) );
    }

    @org.junit.jupiter.api.Test
    void getOrThrowWrapped() {
        assertThrows(
                ArithmeticException.class,
                () -> LEFT.getOrThrowWrapped( ArithmeticException::new )
        );

        try {
            LEFT.getOrThrowWrapped( IOException::new );
        } catch(IOException e) {
            // getOrThrowWrapped() calls the IOException(String) constructor,
            // since LEFT is a String type, rather than the no-arg IOException
            // constructor, as getOrThrow() via a Supplier would.
            assertEquals( LVAL, e.getMessage() );
        }

        assertEquals( RVAL, RIGHT.getOrThrowWrapped( ArithmeticException::new ) );
    }

    @org.junit.jupiter.api.Test
    void swap() {
        Either<String, String> left = Either.ofLeft( "left" );
        Either<String, String> leftSwap = left.swap();
        assertTrue( leftSwap.isRight() );
        assertTrue( leftSwap.contains( "left" ) );

        Either<String, String> right = Either.ofRight( "right" );
        Either<String, String> rightSwap = right.swap();
        assertTrue( rightSwap.isLeft() );
        assertTrue( rightSwap.containsLeft( "right" ) );
    }

    @org.junit.jupiter.api.Test
    void toResult() {
        assertEquals( Result.ofErr(LVAL),LEFT.toResult() );
        assertEquals( Result.ofOK(RVAL), RIGHT.toResult() );
    }

    // because we don't want illegal monads
    @org.junit.jupiter.api.Test
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
        final Function<String, Either<String,Integer>> concat = s -> Either.ofLeft(s+s);
        // then the following should be equivalent:
        assertEquals(
                concat.apply(LVAL),
                LEFT.flatMapLeft( concat )
        );

        // and similarly, for right Eithers:
        final Function<Integer, Either<String,Integer>> square = i -> Either.ofRight( i*i);
        assertEquals(
                square.apply(RVAL) ,
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
        final Function<String,Either<String,Integer>> delim = s -> Either.ofLeft(s+","+s);
        final Function<Integer,Either<String,Integer>> hundredMore = i -> Either.ofRight(i+100);
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

    @org.junit.jupiter.api.Test
    void swapEquivalences() {
        final Either<String, Integer> original = Either.ofLeft( "left" );
        final Either<Integer, String> swapped = original.swap();

        // could do this for multiple methods....
        // as an example, the following should be equivalent:
        assertEquals(
                original.forfeit( NEVERFUNCTION ),
                swapped.recover(NEVERFUNCTION)
        );
    }

}