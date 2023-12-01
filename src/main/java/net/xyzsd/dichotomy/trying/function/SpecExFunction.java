package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;


/**
 * A functional interface for a special exception-throwing function that takes an argument of type {@code T} and returns a result of type {@code R}.
 * The function can throw an exception of type {@code X}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <X> the type of the exception that can be thrown by the function
 */
@FunctionalInterface
public interface SpecExFunction<T, R, X extends Exception> {

    // DOES NOT compose exceptions ... that is rather difficult


    /**
     * Applies the function to the given argument.
     *
     * @param t the argument to apply the function to
     * @return the result of applying the function to the argument
     * @throws Exception if an exception occurs while applying the function
     */
    @NotNull R apply(T t) throws Exception;



    /**
     * Composes this SpecExFunction with a regular Function.
     *
     * @param <V>    the type of input to the before function
     * @param before the function to compose with
     * @return a new SpecExFunction that is the composition of this SpecExFunction and the before function
     * @throws NullPointerException if before is null
     */
    @NotNull
    default <V> SpecExFunction<V, R, X> compose(@NotNull Function<? super V, ? extends T> before) {
        requireNonNull( before );
        return (V v) -> apply( before.apply( v ) );
    }


    /**
     * Composes this SpecExFunction with a regular Function.
     *
     * @param <V>    the type of input to the {@code after} function
     * @param after  the function to compose with
     * @return a new SpecExFunction that is the composition of this SpecExFunction and the {@code after} function
     * @throws NullPointerException if {@code after} is null
     */
    @NotNull
    default <V> SpecExFunction<T, V, X> andThen(@NotNull Function<? super R, ? extends V> after) {
        requireNonNull( after );
        return (T t) -> after.apply( apply( t ) );
    }


    /**
     * Composes this SpecExFunction with another SpecExFunction before.
     * <p>
     * Note: Exceptions are not composed.
     *
     * @param before the SpecExFunction to compose with
     * @param <V>    the type of input to the before SpecExFunction
     * @return a new SpecExFunction that is the composition of this SpecExFunction and the before SpecExFunction
     * @throws NullPointerException if before is null
     */
    @NotNull
    default <V> SpecExFunction<V, R, X> composeEx(@NotNull SpecExFunction<? super V, ? extends T, X> before) {
        requireNonNull( before );
        return (V v) -> apply( before.apply( v ) );
    }


    /**
     * Composes this SpecExFunction with another SpecExFunction after.
     * <p>
     * Note: Exceptions are not composed.
     *
     * @param <V>    the type of output from the after SpecExFunction
     * @param after the SpecExFunction to compose with
     * @return a new SpecExFunction that is the composition of this SpecExFunction and the after SpecExFunction
     * @throws NullPointerException if after is null
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
     * @param <X> Exception type
     * @return returns the input argument.
     */
    @NotNull
    static <T, X extends Exception> SpecExFunction<T, T, X> identity() {
        return t -> t;
    }

}
