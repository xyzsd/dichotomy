package net.xyzsd.dichotomy.trying.function;

import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts one argument and produces a result, and
 * that can throw an exception.
 *
 * @param <T> input type
 * @param <R> output type
 */
@NullMarked
@FunctionalInterface
public interface ExFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Exception exception
     */
    R apply(T t) throws Throwable;


    /**
     * Convert a Function to an ExFunction. The Function is only invoked when used.
     *
     * @param fn    Function to convert
     * @param <IN>  function input
     * @param <OUT> function return type
     * @return ExFunction
     * @throws NullPointerException if input fn is null OR return value is null
     */
    static <IN, OUT> ExFunction<IN, OUT> from(final Function<IN, OUT> fn) {
        requireNonNull( fn );
        return requireNonNull( fn::apply );
    }


    /**
     * Composes an ExFunction with another ExFunction, applying the before function first
     * and then applying this function to the result.
     *
     * @param <V>    the type of the input to the before function
     * @param before the ExFunction to apply before this function
     * @return a composed ExFunction that applies the before function followed by this function
     * @throws NullPointerException if before is null
     */

    default <V> ExFunction<V, R> compose(ExFunction<? super V, ? extends T> before) {
        requireNonNull( before );
        return (V v) -> apply( before.apply( v ) );
    }


    /**
     * Returns a composed {@code ExFunction} that performs, in sequence, this
     * operation followed by the {@code after} operation.
     * <p>
     * Note that the {@code after} operation will not be performed if the
     * first operation throws an exception.
     * </p>
     *
     * @param after the operation to perform after this operation
     * @param <V>   output type of the after function and of the composed function
     * @return a composed {@code ExFunction} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     */

    default <V> ExFunction<T, V> andThen(ExFunction<? super R, ? extends V> after) {
        requireNonNull( after );
        return (T t) -> after.apply( apply( t ) );
    }

    /**
     * Identity function.
     *
     * @param <T> input type
     * @return returns the input argument.
     */

    static <T> ExFunction<T, T> identity() {
        return t -> t;
    }
}
