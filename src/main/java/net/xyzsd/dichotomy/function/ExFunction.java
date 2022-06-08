package net.xyzsd.dichotomy.function;

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
     */
    @NotNull R apply(T t) throws Exception;


    // we do not supply a generic parameter for Exception ... that potentially complicates
    // things, particularly function composition and how attempt() works for exception types
    // not included.
    //
    // exception types could be limited. either typetoken/type could be used, or the type could
    // be specified.
    //
    // composing functions with exceptions would have to use typetokens to find a common supertype
    // which at worst would be RuntimeException or (if there are any checked exceptions) Exception
    // [in most cases, unless there was an unusual case of a custom Throwable].
    //
    // Alternatively... a synthetic exception could be created that wraps the given exceptions....
    // with the wrapped exception as the cause (or as suppressed)


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
     * @return a composed {@code ExFunction} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     * @see #andThenEx(ExFunction)
     */
    @NotNull
    default <V> ExFunction<T, V> andThen(@NotNull Function<? super R, ? extends V> after) {
        requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }



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
