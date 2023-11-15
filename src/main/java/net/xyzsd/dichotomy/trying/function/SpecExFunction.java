package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;


// DOES NOT compose exceptions ... that is rather difficult
@FunctionalInterface
public interface SpecExFunction<T, R, X extends Exception> {


    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    @NotNull R apply(T t) throws Exception;



    // compose with a regular Function
    @NotNull
    default <V> SpecExFunction<V, R, X> compose(@NotNull Function<? super V, ? extends T> before) {
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
     * @return a composed {@code ExFunction} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     */
    @NotNull
    default <V> SpecExFunction<T, V, X> andThen(@NotNull Function<? super R, ? extends V> after) {
        requireNonNull( after );
        return (T t) -> after.apply( apply( t ) );
    }


    @NotNull
    default <V> SpecExFunction<V, R, X> composeEx(@NotNull SpecExFunction<? super V, ? extends T, X> before) {
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
     * @return a composed {@code ExFunction} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     * @see #andThen(Function)
     */
    @NotNull
    default <V> SpecExFunction<T, V, X> andThenEx(@NotNull SpecExFunction<? super R, ? extends V, X> after) {
        requireNonNull( after );
        return (T t) -> after.apply( apply( t ) );
    }

    /**
     * Identity function.
     *
     * @param <T> input type
     * @return returns the input argument.
     */
    @NotNull
    static <T, X extends Exception> SpecExFunction<T, T, X> identity() {
        return t -> t;
    }

}
