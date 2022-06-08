package net.xyzsd.dichotomy.function;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a Supplier that can throw an exception.
 *
 * @param <T> type which is being Supplied
 */
@FunctionalInterface
public interface ExSupplier<T> {

    /** Get a Result or throw an Exception */
    @NotNull T get() throws Exception;

}
