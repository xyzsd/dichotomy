package net.xyzsd.dichotomy.util;

import net.xyzsd.dichotomy.Either;
import net.xyzsd.dichotomy.Result;
import net.xyzsd.dichotomy.trying.Try2;
import org.jetbrains.annotations.NotNull;

// conversion utils. keeps dependencies separate
public interface Conversion {


    /**
     * Create an {@link Either} from a {@link Result}.
     */
    @NotNull
    static <L, R> Either<L, R> toEither(@NotNull Result<R, L> result) {
        return result.fold( Either::ofRight, Either::ofLeft );
    }


    /**
     * Create a {@link Either} from a {@link Try2}.
     */
    @NotNull
    static <V, X extends Exception> Either<X, V> toEither(@NotNull Try2<V, X> tri) {
        return tri.fold( Either::ofRight, Either::ofLeft );
    }

    /**
     * Create a new {@code Result} from the given {@link Either}.
     *
     * @return a new {@code Result}, equivalent to this {@link Either}.
     */
    @NotNull
    static <L, R> Result<R, L> toResult(@NotNull final Either<L, R> either) {
        return either.fold( Result::ofErr, Result::ofOK );
    }


    /**
     * Create a {@link Result} from a {@link Try2}.
     */
    @NotNull
    static <V, X extends Exception> Result<V, X> toResult(@NotNull Try2<V, X> tri) {
        return tri.fold( Result::ofOK, Result::ofErr );
    }


    /**
     * Create a {@link Try2} from an {@link Either}.
     */
    @NotNull
    static <L extends Exception, R> Try2<R, L> toTry(@NotNull Either<L, R> either) {
        return either.fold( Try2.Err::new, Try2.OK::new );
    }


    /**
     * Create a {@link Try2} from a {@link Result}.
     */
    @NotNull
    static <V, X extends Exception> Try2<V, X> toTry(@NotNull Result<V, X> result) {
        return result.fold( Try2.OK::new, Try2.Err::new );
    }


}
