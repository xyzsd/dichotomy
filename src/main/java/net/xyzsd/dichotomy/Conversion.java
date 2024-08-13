package net.xyzsd.dichotomy;

import net.xyzsd.dichotomy.trying.Try;
import org.jspecify.annotations.NullMarked;


/**
 * Conversion utilities.
 * <p>
 * Convert {@link Either}s, {@link Result}s, or {@link Try}s to another monadic type.
 */
@NullMarked
public interface Conversion {

    /**
     * Converts a {@link Result} into an {@link Either}.
     *
     * @param <L>    the type of the left value in the {@link Either}
     * @param <R>    the type of the right value in the {@link Either}
     * @param result a {@link Result} to be converted into an {@link Either}
     * @return an {@link Either} object representing the converted result
     * @throws IllegalArgumentException if the {@code result} parameter is null
     */

    static <L, R> Either<L, R> toEither(Result<R, L> result) {
        return result.fold( Either::ofRight, Either::ofLeft );
    }


    /**
     * Converts a {@link Try} object into an {@link Either} object.
     * <p>
     * This method takes a {@link Try} object and converts it into an {@link Either} object.
     * The left value of the {@link Either} represents an exception that occurred during the try operation,
     * and the right value represents the result of the try operation.
     *
     * @param <V> the type of the right value in the {@link Either}
     * @param tri a {@link Try} object to be converted into an {@link Either} object
     * @return an {@link Either} object representing the converted {@link Try} result
     * @throws IllegalArgumentException if the {@code tri} parameter is null
     */

    static <V> Either<Throwable, V> toEither(Try<V> tri) {
        return tri.fold( Either::ofRight, Either::ofLeft );
    }

    /**
     * Converts an {@link Either} object into a {@link Result} object.
     *
     * @param <L>    the type of the left value in the {@link Either}
     * @param <R>    the type of the right value in the {@link Either}
     * @param either the {@link Either} object to be converted
     * @return a {@link Result} object representing the converted {@link Either}
     * @throws IllegalArgumentException if the {@code either} parameter is null
     */

    static <L, R> Result<R, L> toResult(final Either<L, R> either) {
        return either.fold( Result::ofErr, Result::ofOK );
    }


    /**
     * Converts a {@link Try} object into a {@link Result} object.
     *
     * @param <V> the type of the value in the {@link Result}
     * @param tri a {@link Try} object to be converted into a {@link Result}
     * @return a {@link Result} object representing the converted {@link Try} result
     * @throws IllegalArgumentException if the {@code tri} parameter is null
     */

    static <V> Result<V, Throwable> toResult(Try<V> tri) {
        return tri.fold( Result::ofOK, Result::ofErr );
    }


    /**
     * Converts an {@link Either} object into a {@link Try} object.
     *
     * @param <L>    the type of the left value in the {@link Either}
     * @param <R>    the type of the right value in the {@link Either}
     * @param either the {@link Either} object to be converted
     * @return a {@link Try} object representing the converted {@link Either}
     * @throws IllegalArgumentException if the {@code either} parameter is null
     */

    static <L extends Exception, R> Try<R> toTry(Either<L, R> either) {
        return either.fold( Try::ofFailure, Try::ofSuccess );
    }


    /**
     * Converts a {@link Result} into a {@link Try}.
     *
     * @param <V>    the type of the value in the {@link Result}
     * @param <X>    the type of the exception in the {@link Try}
     * @param result a {@link Result} to be converted into a {@link Try}
     * @return a {@link Try} object representing the converted result
     * @throws IllegalArgumentException if the {@code result} parameter is null
     */

    static <V, X extends Exception> Try<V> toTry(Result<V, X> result) {
        return result.fold( Try::ofSuccess, Try::ofFailure );
    }


}
