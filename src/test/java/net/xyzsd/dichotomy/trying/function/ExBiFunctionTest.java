package net.xyzsd.dichotomy.trying.function;

import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ExBiFunctionTest {

    final static BiFunction<Integer,Integer,Integer> total = Integer::sum;
    final static Function<Integer,Integer> square = (a) -> (a*a);


    @Test
    void from() {
        assertThrows( NullPointerException.class, () -> ExBiFunction.from( null ) );
        assertDoesNotThrow(  () -> ExBiFunction.from( (__,___) ->   null  ) );
        try {
            final ExBiFunction<Integer, Integer, Integer> from = ExBiFunction.from( total );
            assertEquals( 7, from.apply(4,3) );
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }

    @Test
    void andThen() {
        try {
            final ExBiFunction<Integer, Integer, Integer> from = ExBiFunction.from( total );
            final ExFunction<Integer, Integer> squares = ExFunction.from( square );
            final ExBiFunction<Integer, Integer, Integer> composed = from.andThen( squares );
            assertEquals( 49, composed.apply( 4,3 ) );
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }
}