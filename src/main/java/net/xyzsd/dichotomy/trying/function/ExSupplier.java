package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;


/**
 * Represents a Supplier that can throw an exception.
 *
 * @param <T> type which is being Supplied
 */
@FunctionalInterface
public interface ExSupplier<T> {

    /**
     * Get a type T or throw an Exception
     * @return T object of supplied type
     * @throws Exception exception on failure
     */
    @NotNull T get() throws Throwable;

    /**
     * Convert a Supplier to an ExSupplier. The Supplier is only invoked when used.
     *
     * @param supplier Supplier
     * @return ExSupplier
     * @param <OUT> Supplier type parameter
     * @throws NullPointerException if input fn is null OR return value is null
     */
    @NotNull static <OUT> ExSupplier<OUT> from(@NotNull final Supplier<OUT> supplier) {
        requireNonNull( supplier );
        return requireNonNull( supplier::get);
    }


}
