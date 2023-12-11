package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts one argument and produces a result, and
 * that can throw an exception.
 *
 * @param <T> input type
 * @param <R> output type
 */
@FunctionalInterface
public interface ExFunction<T, R>  {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Exception exception
     */
    @NotNull R apply(T t) throws Exception;


    /**
     * Composes this ExFunction with a regular Function.
     *
     * @param before the Function to be composed with
     * @param <V> the input type of the composed Function
     * @return the composed ExFunction
     * @throws NullPointerException if the before Function is null
     */
    // compose with a regular Function
    @NotNull
    default <V> ExFunction<V, R> compose(@NotNull Function<? super V, ? extends T> before) {
        requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }


    /**
     * Returns a composed {@code ExFunction} that performs, in sequence, this
     * operation followed by the {@code after} operation.
     * <p>
     *     Note that the {@code after} operation will not be performed if the
     *     first operation throws an exception.
     * </p>
     *
     * @param after the operation to perform after this operation
     * @param <V> output type of the after function and of the composed function
     * @return a composed {@code ExFunction} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     * @see #andThenEx(ExFunction)
     */
    @NotNull
    default <V> ExFunction<T, V> andThen(@NotNull Function<? super R, ? extends V> after) {
        requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }




    /**
     * Composes an ExFunction with another ExFunction, applying the before function first
     * and then applying this function to the result.
     *
     * @param <V> the type of the input to the before function
     * @param before the ExFunction to apply before this function
     * @return a composed ExFunction that applies the before function followed by this function
     * @throws NullPointerException if before is null
     */
    @NotNull
    default <V> ExFunction<V, R> composeEx(@NotNull ExFunction<? super V, ? extends T> before) {
        requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }


    /**
     * Returns a composed {@code ExFunction} that performs, in sequence, this
     * operation followed by the {@code after} operation.
     * <p>
     *     Note that the {@code after} operation will not be performed if the
     *     first operation throws an exception.
     * </p>
     *
     * @param after the operation to perform after this operation
     * @param <V> output type of the after function and of the composed function
     * @return a composed {@code ExFunction} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     * @see #andThen(Function) 
     */
    @NotNull
    default <V> ExFunction<T, V> andThenEx(@NotNull ExFunction<? super R, ? extends V> after) {
        requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Identity function.
     *
     * @return returns the input argument.
     * @param <T> input type
     */
    @NotNull
    static <T> ExFunction<T,T> identity() {
        return t -> t;
    }
}
