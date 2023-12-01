package net.xyzsd.dichotomy;

import net.xyzsd.dichotomy.TestUtils.SingleUseConsumer;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.xyzsd.dichotomy.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MaybeTest {

    static final String TEST_STR = "TEST";
    static final String NEW_STRING = "NEW_STRING";
    static final String MORE_STRING = "MORE";

    static final Maybe<String> MAYBE_YES = Maybe.of( TEST_STR );
    static final Maybe<String> MAYBE_NOT = Maybe.ofNone();
    static final Maybe<String> MAYBE_MORE = Maybe.of( MORE_STRING );


    static final int INT666 = 666;
    static final int INT777 = 777;
    static final Function<String, Integer> STR2INT = (x) -> INT666;
    static final Function<Integer, Integer> INT2INT = (x) -> x + 111;

    static final Supplier<String> STRSUP = () -> NEW_STRING;
    static final Supplier<Integer> INTSUP = () -> 777;

    static final Supplier<String> NULLSUP = () -> null;

    @Test
    void of() {
        assertEquals( MAYBE_YES.getClass(), Maybe.Some.class );
    }

    @Test
    void ofNone() {
        assertEquals( MAYBE_NOT.getClass(), Maybe.None.class );
    }

    @Test
    void ofNullable() {
        Maybe<String> maybeYes = Maybe.ofNullable( TEST_STR );
        assertEquals( maybeYes.getClass(), Maybe.Some.class );

        Maybe<String> maybeNot = Maybe.ofNullable( null );
        assertEquals( maybeNot.getClass(), Maybe.None.class );
    }

    @Test
    void biMatch() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.biMatch( null, NEVERRUNNABLE ) );
        assertThrows( NullPointerException.class, () -> MAYBE_YES.biMatch( neverConsumer(), null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.biMatch( null, NEVERRUNNABLE ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.biMatch( neverConsumer(), null ) );

        SingleUseConsumer<String> stringConsumer = new SingleUseConsumer<>();
        final Maybe<String> maybeYes = assertDoesNotThrow( () -> MAYBE_YES.biMatch( stringConsumer, NEVERRUNNABLE ) );
        assertEquals( 1, stringConsumer.activationCount() );
        assertEquals( maybeYes, MAYBE_YES );

        SingleUseRunnable singleUseRunnable = new SingleUseRunnable();
        final Maybe<String> maybeNot = assertDoesNotThrow( () -> MAYBE_NOT.biMatch( neverConsumer(), singleUseRunnable ) );
        assertEquals( 1, singleUseRunnable.activationCount() );
        assertEquals( maybeNot, MAYBE_NOT );
    }

    @Test
    void filter() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.filter( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.filter( null ) );

        Predicate<String> predicate = "TEST"::equals;

        assertEquals( MAYBE_YES, MAYBE_YES.filter( predicate ) );
        assertEquals( Maybe.ofNone(), MAYBE_YES.filter( predicate.negate() ) );
        //
        assertEquals( Maybe.ofNone(), MAYBE_NOT.filter( predicate ) );
    }

    @Test
    void fold() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.fold( null, neverSupplier() ) );
        assertThrows( NullPointerException.class, () -> MAYBE_YES.fold( neverFunction(), null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.fold( null, neverSupplier() ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.fold( neverFunction(), null ) );

        assertEquals(
                INT666,
                MAYBE_YES.fold( STR2INT, neverSupplier() )
        );

        assertEquals(
                INT777,
                MAYBE_NOT.fold( neverFunction(), INTSUP )
        );
    }

    @Test
    void stream() {
        assertEquals( 1, MAYBE_YES.stream().count() );
        assertEquals( 0, MAYBE_NOT.stream().count() );
    }

    @Test
    void map() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.map( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.map( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_YES.map( (s) -> null ) );
        assertDoesNotThrow(  () -> MAYBE_NOT.map( neverFunction() ) );

        assertEquals(
                MAYBE_MORE,
                MAYBE_YES.map( (x) -> MORE_STRING )
        );
    }

    @Test
    void match() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.consume( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.consume( null ) );

        SingleUseConsumer<String> stringConsumer = new SingleUseConsumer<>();
        Maybe<String> maybeYes = assertDoesNotThrow( () -> MAYBE_YES.match( stringConsumer ) );
        assertEquals( 1, stringConsumer.activationCount() );
        assertEquals( maybeYes, MAYBE_YES );
    }

    @Test
    void consume() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.consume( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.consume( null ) );

        SingleUseConsumer<String> stringConsumer = new SingleUseConsumer<>();
        assertDoesNotThrow( () -> MAYBE_YES.consume( stringConsumer ) );
        assertEquals( 1, stringConsumer.activationCount() );
    }

    @Test
    void flatMap() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.flatMap( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.flatMap( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_YES.flatMap( (s) -> null ) );
        assertDoesNotThrow(  () -> MAYBE_NOT.flatMap( neverFunction() ) );

        assertEquals(
                MAYBE_MORE,
                MAYBE_YES.flatMap( (x) -> MAYBE_MORE )
        );
    }

    @Test
    void matches() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.matches( null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.matches( null ) );

        Predicate<String> predicate = "TEST"::equals;

        assertTrue( MAYBE_YES.matches( predicate ) );
        assertFalse( MAYBE_YES.matches( predicate.negate() ) );
        //
        assertFalse( MAYBE_NOT.matches( predicate ) );
    }

    @Test
    void contains() {
        assertTrue( MAYBE_YES.contains( TEST_STR ) );
        assertFalse( MAYBE_YES.contains( null ) );
        //
        assertFalse( MAYBE_NOT.contains( TEST_STR ) );
        assertTrue( MAYBE_NOT.contains( null ) );
    }

    @Test
    void hasSome() {
        assertTrue( MAYBE_YES.hasSome() );
        assertFalse( MAYBE_NOT.hasSome() );
    }

    @Test
    void orElse() {
        // assertThrows( NullPointerException.class, () -> MAYBE_YES.orElse( (String)null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.orElse( (String) null ) );
        assertEquals( TEST_STR, MAYBE_YES.orElse( "*invalid*" ) );
        assertEquals( "*invalid*", MAYBE_NOT.orElse( "*invalid*" ) );
    }

    @Test
    void testOrElse() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.orElse( (Supplier<String>) null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.orElse( (Supplier<String>) null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.orElse( NULLSUP ) );
        assertEquals( TEST_STR, MAYBE_YES.orElse( neverSupplier() ) );
        assertEquals( "*invalid*", MAYBE_NOT.orElse( () -> "*invalid*" ) );
    }

    @Test
    void and() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.and( (Maybe<String>) null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.and( (Maybe<String>) null ) );

        assertEquals( MAYBE_MORE, MAYBE_YES.and( MAYBE_MORE ) );
        assertEquals( MAYBE_NOT, MAYBE_YES.and( MAYBE_NOT ) );

        assertEquals( MAYBE_NOT, MAYBE_NOT.and( MAYBE_YES ) );
        assertEquals( MAYBE_NOT, MAYBE_NOT.and( MAYBE_NOT ) );
    }

    @Test
    void testAnd() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.and( (Supplier<Maybe<?>>) null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_YES.and( () -> null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.and( (Supplier<Maybe<?>>) null ) );

        assertEquals( MAYBE_MORE, MAYBE_YES.and( () -> MAYBE_MORE ) );
        assertEquals( MAYBE_NOT, MAYBE_YES.and( () -> MAYBE_NOT ) );

        assertEquals( MAYBE_NOT, MAYBE_NOT.and( () -> MAYBE_YES ) );
        assertEquals( MAYBE_NOT, MAYBE_NOT.and( () -> MAYBE_NOT ) );
    }

    @Test
    void or() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.or(  (Maybe<String>) null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.or(  (Maybe<String>) null ) );

        assertEquals( MAYBE_YES, MAYBE_YES.or( MAYBE_MORE ) );
        assertEquals( MAYBE_YES, MAYBE_YES.or( MAYBE_NOT ) );

        assertEquals( MAYBE_YES, MAYBE_NOT.or( MAYBE_YES ) );
        assertEquals( MAYBE_MORE, MAYBE_NOT.or( MAYBE_MORE ) );
    }

    @Test
    void testOr() {
        assertThrows( NullPointerException.class, () -> MAYBE_YES.or( (Supplier<Maybe<String>>) null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.or( () -> null ) );
        assertThrows( NullPointerException.class, () -> MAYBE_NOT.or( (Supplier<Maybe<String>>) null ) );

        assertEquals( MAYBE_YES, MAYBE_YES.or( () -> MAYBE_MORE ) );
        assertEquals( MAYBE_YES, MAYBE_YES.or( () -> MAYBE_NOT ) );

        assertEquals( MAYBE_YES, MAYBE_NOT.or( () -> MAYBE_YES ) );
        assertEquals( MAYBE_MORE, MAYBE_NOT.or( () -> MAYBE_MORE ) );
    }

    @Test
    void expect() {
        final String s = assertDoesNotThrow( MAYBE_YES::expect );
        assertEquals( TEST_STR, s );

        assertThrows( NoSuchElementException.class, MAYBE_NOT::expect );
    }

    @Test
    void getOrThrow() {
        final String s = assertDoesNotThrow( () -> MAYBE_YES.getOrThrow( ArithmeticException::new ) );
        assertEquals( TEST_STR, s );

        assertThrows( ArithmeticException.class, () -> MAYBE_NOT.getOrThrow( ArithmeticException::new ) );
    }
}