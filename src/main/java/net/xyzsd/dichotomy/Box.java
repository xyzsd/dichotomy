package net.xyzsd.dichotomy;

import org.jetbrains.annotations.NotNull;

import net.xyzsd.dichotomy.either.Either;
import net.xyzsd.dichotomy.result.Result;

import java.util.Optional;

/**
 * Common (super) interface for {@link Either}s and {@link Result}s.
 *
 * <blockquote>
 *     {@code A Box holds Left or Right values,}<br>
 *     {@code but never both or neither,}<br>
 *     {@code otherwise it'd be a tuple,}<br>
 *     {@code rather than an Either.}
 * </blockquote>
 * <p>
 *     Currently this interface has a most minimal set of methods.
 * </p>
 *
 * @param <L> Left value
 * @param <R> Right value
 */
public interface Box<L,R> {


    /**
     * Get the left value.
     *
     * @return an Optional containing the left value, if present.
     */
    @NotNull Optional<L> left();

    /**
     * Get the right value.
     *
     * @return an Optional containing the left value, if present.
     */
    @NotNull Optional<R> right();


    /**
     * Return a new Box with the left and right values swapped.
     * @return Box with swapped parameters.
     */
    @NotNull Box<R,L> swap();

}
