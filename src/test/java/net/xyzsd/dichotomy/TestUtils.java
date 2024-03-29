package net.xyzsd.dichotomy;

import net.xyzsd.dichotomy.trying.function.ExFunction;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// Test utilities
public interface TestUtils {

    // A supplier that should NOT be invoked
    // For simplicity, we throw a fatal nonchecked Error, rather than the preferred IllegalStateException.
    // This way we can check that these are thrown when testing a Try.
    Supplier<?> NEVERSUPPLIER = () -> {
        throw new LinkageError( "NEVERSUPPLIER::get invoked!" );
    };

    // A function that should NOT be invoked
    Function<?, ?> NEVERFUNCTION = (x) -> {
        throw new LinkageError( "NEVERFUNCTION::apply invoked!" );
    };

    ExFunction<?, ?> EX_NEVERFUNCTION = (x) -> {
        throw new LinkageError( "EX_NEVERFUNCTION::apply invoked!" );
    };

    // a runner that should NOT be invoked
    Runnable NEVERRUNNABLE = () -> {
        throw new LinkageError( "NEVERRUN was invoked!" );
    };

    // a consumer that should NOT be invoked
    Consumer<?> NEVERCONSUMER = (x) -> {
        throw new LinkageError( "NEVERCONSUMER was invoked!" );
    };


    // coerced types of above
    @SuppressWarnings("unchecked")
    static <T> Supplier<T> neverSupplier() {
        return (Supplier<T>) NEVERSUPPLIER;
    }

    @SuppressWarnings("unchecked")
    static <T, U> Function<T, U> neverFunction() {
        return (Function<T, U>) NEVERFUNCTION;
    }

    @SuppressWarnings("unchecked")
    static <T> Consumer<T> neverConsumer() {
        return (Consumer<T>) NEVERCONSUMER;
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable, U extends Throwable> Function<T, U> neverFunctionT() {
        return (Function<T, U>)   NEVERFUNCTION;
    }

    @SuppressWarnings("unchecked")
    static <T,U> ExFunction<T, U> neverExFunction() {
        return (ExFunction<T, U>) EX_NEVERFUNCTION;
    }






    // A Consumer that keeps track of the amount of times accept() was called.
    // Should only be created once and then discarded. Do not reuse.
    final class SingleUseConsumer<T> implements Consumer<T> {

        private final SingleUseRunnable runnable = new SingleUseRunnable();

        @Override
        public void accept(T t) {
            runnable.run();
        }

        public boolean usedJustOnce() {
            return runnable.usedJustOnce();
        }

        public int activationCount() {
           return runnable.activationCount();
        }

        public boolean neverUsed() {
            return runnable.neverUsed();
        }
    }

    final class SingleUseRunnable implements Runnable {
        // DO NOT query directly. Only use query() method
        private int count = 0;


        private static void verifyUnused(final int counter) {
            if (counter < 0) {
                throw new IllegalStateException( "ILLEGAL re-use of single use item after query. Improperly written test? Create a new item!" );
            }
        }

        public int activationCount() {
            synchronized (this) {
                int finalCount = count;
                verifyUnused( finalCount );
                count = -1;
                return finalCount;
            }
        }

        @Override
        public void run() {
            synchronized (this) {
                verifyUnused(count);
                count++;
            }
        }

        public boolean usedJustOnce() {
            return (activationCount() == 1);
        }



        public boolean neverUsed() {
            return (activationCount() == 0);
        }
    }

}
