package net.xyzsd.dichotomy;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// Test utilities
public interface TestUtils {

    // TODO: needs to be in a common util class; copied from EitherTest
    // 'never' types: check if implementation calls a supplier / function when it doesn't need to
    // Using rawtypes is easier. if the cast fails... we have an error anyway

    // A supplier that should NOT be invoked
    Supplier<Object> NEVERSUPPLIER = () -> {
        throw new IllegalStateException( "NEVERSUPPLIER::get invoked!" );
    };

    // A function that should NOT be invoked
    Function<Object, Object> NEVERFUNCTION = (x) -> {
        throw new IllegalStateException( "NEVERFUNCTION::apply invoked!" );
    };

    // a runner that should NOT be invoked
    Runnable NEVERRUNNABLE = () -> {
        throw new IllegalStateException( "NEVERRUN was invoked!" );
    };

    // a consumer that should NOT be invoked
    Consumer<Object> NEVERCONSUMER = (x) -> {
        throw new IllegalStateException( "NEVERCONSUMER was invoked!" );
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
                throw new IllegalStateException( "ILLEGAL re-use of single use item after query. Create a new item!" );
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
