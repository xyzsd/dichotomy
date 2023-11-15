package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * An {@link ExConsumer} is equivalent to a {@link Consumer}, but the
 * {@link ExConsumer#accept(Object)} method can throw an Exception.
 *
 * @param <T> value type accepted by the {@link ExConsumer}
 */
@FunctionalInterface
public interface ExConsumer<T> {

    /**
     * Performs this operation on the given argument.
     * @param t input
     * @throws Exception
     */
    void accept(@Nullable T t) throws Exception;


    /**
     * Returns a composed {@code ExConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation.
     * <p>
     *     Note that the {@code after} operation will not be performed if the
     *     first operation throws an exception.
     * </p>
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ExConsumer} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     */
    default ExConsumer<T> andThen(@NotNull Consumer<? super T> after) {
        requireNonNull(after);
        return (T t) -> { after.accept(t); this.accept(t); };
    }


    /**
     * Returns a composed {@code ExConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation.
     * <p>
     *     Note that the {@code after} operation will not be performed if the
     *     first operation throws an exception.
     * </p>
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ExConsumer} that performs this operation followed by the {@code after} operation.
     * @throws NullPointerException if {@code after} is null
     */
    default ExConsumer<T> andThenEx(@NotNull ExConsumer<? super T> after) {
        requireNonNull(after);
        return (T t) -> { after.accept(t); this.accept(t); };
    }



}
