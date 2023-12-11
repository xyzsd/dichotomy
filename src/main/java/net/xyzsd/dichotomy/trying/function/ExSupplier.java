package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;


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
    @NotNull T get() throws Exception;



}
