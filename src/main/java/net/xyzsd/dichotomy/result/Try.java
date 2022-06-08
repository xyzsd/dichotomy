package net.xyzsd.dichotomy.result;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.xyzsd.dichotomy.function.ExFunction;
import net.xyzsd.dichotomy.function.ExSupplier;
import net.xyzsd.dichotomy.function.SpecExSupplier;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 *
 * A {@link Try} wraps the output of exception-producing {@link Supplier}s and functions into {@link Result}s
 * (the {@code of...()} methods), or creates non-{@link Exception}-producing {@link Supplier}s and
 * {@link Function}s which return {@link Result}s (the {@code from...()} methods).
 *
 */
public final class Try {

    // this is a utility class
    private Try() {}

    /**
     * Get a {@link Result} from a {@link Supplier}.
     * <p>
     * The {@link Supplier} may only throw {@link RuntimeException}s,
     * which will be wrapped in an {@link Err} {@link Result}.
     * </p>
     *
     * @param supplier Supplier to invoke
     * @param <V>      type of result supplied
     * @return A {@link Result} containing the supplied value or a {@link RuntimeException}
     * @see #of(ExSupplier)
     * @see #from(Supplier)
     * @see #from(ExSupplier)
     */
    @NotNull
    public static <V> Result<V, RuntimeException> of(@NotNull Supplier<V> supplier) {
        requireNonNull( supplier );

        try {
            return Result.ofOK( supplier.get() );
        } catch (RuntimeException ex) {
            return Result.ofErr( ex );
        }
    }


    /**
     * Get a {@link Result} from an {@link ExSupplier}.
     * <p>
     * The {@link ExSupplier} may throw a checked or unchecked {@link Exception},
     * which will be wrapped in an {@link Err} {@link Result}.
     * </p>
     *
     * @param exSupplier Supplier to invoke
     * @param <V>        type of result supplied
     * @return A {@link Result} containing the supplied value or an {@link Exception}
     * @see #from(ExSupplier)
     * @see #of(Supplier)
     * @see #from(Supplier)
     */
    @NotNull
    public static <V> Result<V, Exception> of(@NotNull ExSupplier<V> exSupplier) {
        requireNonNull( exSupplier );

        try {
            return Result.ofOK( exSupplier.get() );
        } catch (Exception ex) {
            return Result.ofErr( ex );
        }
    }

    /**
     * Converts an {@link Supplier} which may throw {@link RuntimeException}s to a {@link Supplier} of {@link Result}s.
     *
     * @param supplier Supplier to invoke
     * @param <V>      type of result supplied
     * @return A {@link Result} containing the supplied value or a {@link RuntimeException}
     * @see #of(Supplier)
     * @see #of(ExSupplier)
     * @see #from(ExSupplier)
     */
    @NotNull
    public static <V> Supplier<Result<V, RuntimeException>> from(@NotNull Supplier<V> supplier) {
        requireNonNull( supplier );
        return () -> of( supplier );
    }


    /**
     * Converts an {@link ExSupplier} to a {@link Supplier} of {@link Result}s.
     * <p>
     * The {@link ExSupplier} may throw a checked or unchecked {@link Exception},
     * which will be wrapped in an {@link Err} {@link Result}.
     * </p>
     *
     * @param exSupplier Supplier to invoke
     * @param <V>        type of result supplied
     * @return A {@link Result} containing the supplied value or an {@link Exception}
     */
    @NotNull
    public static <V> Supplier<Result<V, Exception>> from(@NotNull ExSupplier<V> exSupplier) {
        requireNonNull( exSupplier );
        return () -> of( exSupplier );
    }


    /**
     * Converts a {@link SpecExSupplier} to a {@link Supplier} of {@link Result}s.
     * <p>
     * The returned {@link Supplier} will only wrap {@link Exception}s of the given class
     * parameter {@code <X>} (and subclasses); all other {@link RuntimeException}s will be thrown.
     * </p>
     *
     * @param exSupplier {@link SpecExSupplier} to wrap
     * @param <V>        value wrapped by the given {@link Result}.
     * @param <X>        {@link RuntimeException} wrapped by the given {@link Result}.
     * @return A {@link Supplier} of {@link Result}s containing the supplied value or the specified
     * {@link RuntimeException}{@code <X>}
     * @see #of(SpecExSupplier, Class)
     * @see #from(ExSupplier)
     * @see #from(Supplier)
     */
    @NotNull
    public static <V, X extends RuntimeException> Supplier<Result<V, X>> from(@NotNull SpecExSupplier<V, X> exSupplier, final Class<X> exClass) {
        requireNonNull( exSupplier );
        return () -> of( exSupplier, exClass );
    }


    /**
     * Execute a {@link SpecExSupplier} to a supply a {@link Result}.
     * <p>
     * The returned {@link Result} will contain the supplied value or the {@link RuntimeException} of the given class
     * parameter {@code <X>} (and subclasses); all other {@link RuntimeException}s will be thrown.
     * </p>
     *
     * @param exSupplier {@link SpecExSupplier} to wrap
     * @param <V>        supplied value wrapped by a {@link Result}.
     * @param <X>        {@link RuntimeException} type wrapped by a {@link Result}.
     * @return A {@link Result} containing the supplied value or the specified {@link RuntimeException}
     *
     * @see #from(SpecExSupplier, Class)
     */
    @NotNull
    public static <V, X extends RuntimeException> Result<V, X> of(@NotNull SpecExSupplier<V, X> exSupplier, final Class<X> exClass) {
        requireNonNull( exSupplier );
        requireNonNull( exClass );

        try {
            return Result.ofOK( exSupplier.get() );
        } catch (RuntimeException ex) {
            if (exClass.isAssignableFrom( ex.getClass() )) {
                return Result.ofErr( exClass.cast( ex ) );
            }
            throw ex;
        }
    }


    /**
     * Apply the given {@link ExFunction}, returning a {@link Result} which wraps either the return value
     * of the {@link ExFunction} or an {@link Exception}.
     *
     * @param in value applied
     * @param exFn function, which may throw an exception
     * @return A {@link Result} wrapping either the output value or an {@link Exception}.
     * @param <T> function input
     * @param <R> function output
     *           
     * @see #from(ExFunction)           
     */
    @NotNull
    public static <T, R> Result<R, Exception> of(@Nullable T in, @NotNull final ExFunction<T, R> exFn) {
        requireNonNull( exFn );
        return fnToResult( in, exFn );
    }

    /**
     * Given an {@link Exception}-producing function ({@link ExFunction}), returns a {@link Function} which
     * returns a {@link Result} wrapping the function output or {@link Exception}. This is lazy; the function
     * is not evaluated.
     *
     * @param exFn {@link ExFunction} which may generate an exception
     * @return {@link Function} returning {@link Result}s which wrap the output or {@link Exception}
     * @param <T> function input
     * @param <R> function output
     *     
     * @see #of(Object, ExFunction)
     */
    @NotNull
    public static <T, R> Function<T, Result<R, Exception>> from(@NotNull final ExFunction<T, R> exFn) {
        requireNonNull( exFn );
        return (in) -> fnToResult( in, exFn );
    }



    private static <T, R> Result<R, Exception> fnToResult(@Nullable T in, @NotNull final ExFunction<T, R> exFn) {
        try {
            return Result.ofOK( exFn.apply( in ) );
        } catch (Exception ex) {
            return Result.ofErr( ex );
        }
    }


}
