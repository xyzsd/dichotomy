package net.xyzsd.dichotomy.trying.function;

import org.jspecify.annotations.NullMarked;

import static java.util.Objects.requireNonNull;

/**
 * An {@link ExRunnable} is equivalent to a {@link Runnable}, but the
 * {@link ExRunnable#run()} method can throw a checked Exception.
 */
@NullMarked
@FunctionalInterface
public interface ExRunnable {

    /**
     * Execute the given operation.
     *
     * @throws Throwable Exception
     */
    void run() throws Throwable;

    /**
     * Convert a Runnable to an ExRunnable. The Consumer is only invoked when used.
     *
     * @param runnable the Runnable
     * @return ExRunnable
     */
    static ExRunnable from(final Runnable runnable) {
        requireNonNull( runnable );
        return runnable::run;
    }

}
