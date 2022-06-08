package net.xyzsd.dichotomy.function;


import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SpecExSupplier<T, X extends Exception> {

    @NotNull T get() throws X;

}