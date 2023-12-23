package net.xyzsd.dichotomy.trying.function;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ExFunctionTest {

    final static Function<Integer, Integer> SQUARE = (a) -> (a * a);
    final static Function<Integer, Integer> TRIPLE = (a) -> (3 * a);

    @Test
    void from() {
        assertThrows( NullPointerException.class, () -> ExFunction.from( null ) );
        assertDoesNotThrow( () -> ExFunction.from( (__) -> null ) );
        try {
            final ExFunction<Integer, Integer> from = ExFunction.from( SQUARE );
            assertEquals( 49, from.apply( 7 ) );
        } catch (Throwable t) {
            throw new AssertionError( t );
        }
    }

    @Test
    void compose() {
        try {
            final ExFunction<Integer, Integer> from = ExFunction.from( SQUARE );
            final ExFunction<Integer, Integer> triple = ExFunction.from( TRIPLE );
            final ExFunction<Integer, Integer> composed = from.compose( triple );
            assertEquals( ((7*3)*(7*3)), composed.apply( 7 ) );
        } catch (Throwable t) {
            throw new AssertionError( t );
        }
    }

    @Test
    void andThen() {
        try {
            final ExFunction<Integer, Integer> from = ExFunction.from( SQUARE );
            final ExFunction<Integer, Integer> triple = ExFunction.from( TRIPLE );
            final ExFunction<Integer, Integer> andThen = from.andThen( triple );
            assertEquals( ((7*7)*3), andThen.apply( 7 ) );
        } catch (Throwable t) {
            throw new AssertionError( t );
        }
    }

    @Test
    void identity() {
        try {
            assertEquals(
                    "in=out",
                    ExFunction.identity().apply( "in=out" )
            );
        } catch (Throwable t) {
            throw new AssertionError( t );
        }
    }
}