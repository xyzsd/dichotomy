package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;

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
     * @throws Throwable exception
     */
    void accept(T t) throws Throwable;

    /**
     * Convert a Consumer to an ExConsumer. The Consumer is only invoked when used.
     *
     * @param consumer the Consumer
     * @return ExConsumer
     * @param <IN> Consumer input type parameter
     */
    @NotNull static <IN> ExConsumer<IN> from(@NotNull final Consumer<IN> consumer) {
        requireNonNull( consumer );
        return consumer::accept;
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
    default ExConsumer<T> andThen(@NotNull ExConsumer<? super T> after) {
        requireNonNull(after);
        return (T t) -> { after.accept(t); this.accept(t); };
    }



}
