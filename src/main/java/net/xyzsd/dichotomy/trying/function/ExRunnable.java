package net.xyzsd.dichotomy.trying.function;

import org.jetbrains.annotations.NotNull;


import static java.util.Objects.requireNonNull;

/**
 * An {@link ExRunnable} is equivalent to a {@link Runnable}, but the
 * {@link ExRunnable#run()} method can throw a checked Exception.
 *
 */
@FunctionalInterface
public interface ExRunnable {

    /**
     * Execute the given operation.
     * @throws Throwable Exception
     */
    void run() throws Throwable;

    /**
     * Convert a Runnable to an ExRunnable. The Consumer is only invoked when used.
     *
     * @param runnable the Runnable
     * @return ExRunnable
     */
    @NotNull static ExRunnable from(@NotNull final Runnable runnable) {
        requireNonNull( runnable );
        return runnable::run;
    }

}
