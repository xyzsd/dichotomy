package net.xyzsd.dichotomy.trying.function;

import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;


/**
 * Represents a Supplier that can throw an exception.
 *
 * @param <T> type which is being Supplied
 */
@NullMarked
@FunctionalInterface
public interface ExSupplier<T> {

    /**
     * Get a type T or throw an Exception
     *
     * @return T object of supplied type
     * @throws Exception exception on failure
     */
    T get() throws Throwable;

    /**
     * Convert a Supplier to an ExSupplier. The Supplier is only invoked when used.
     *
     * @param supplier Supplier
     * @param <V>      Supplier type parameter
     * @return ExSupplier
     * @throws NullPointerException if input fn is null OR return value is null
     */
    static <V> ExSupplier<V> from(final Supplier<V> supplier) {
        requireNonNull( supplier );
        return requireNonNull( supplier::get );
    }


}
