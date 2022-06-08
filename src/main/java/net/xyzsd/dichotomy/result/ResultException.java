package net.xyzsd.dichotomy.result;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ResultException} is a {@link RuntimeException} that is can be thrown by an {@link Err}
 * when {@link Result#expect()} is called.
 * <p>
 *      {@link ResultException}s are used in the {@link Result#expect()} method to wrap checked
 *      exceptions or {@link Throwable} types, as well as any type that does NOT extend {@link Throwable}.
 * </p>
 */
public final class ResultException extends RuntimeException {

    /**
     * Create a ResultException with the given message.
     */
    public ResultException(@NotNull String message) {
        super( requireNonNull(message) );
    }

    /**
     * Create a ResultException with the given cause.
     */
    public ResultException(@NotNull Throwable cause) {
        super( requireNonNull(cause) );
    }

    /**
     * Create a ResultException with the given message and cause.
     */
    public ResultException(@NotNull String message, @NotNull Throwable cause) {
        super( requireNonNull(message), requireNonNull(cause) );
    }

}
