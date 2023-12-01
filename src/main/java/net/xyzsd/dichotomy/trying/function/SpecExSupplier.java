package net.xyzsd.dichotomy.trying.function;


import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that represents a supplier of a value that may throw an exception of type X.
 *
 * @param <T> the type of the value to be supplied
 * @param <X> the type of exception that may be thrown
 */
@FunctionalInterface
public interface SpecExSupplier<T, X extends Exception> {

    /**
     * Retrieves a value of type T.
     *
     * @return The retrieved value of type T.
     *
     * @throws X if there is an exception of type X while retrieving the value.
     */
    @NotNull T get() throws X;

}