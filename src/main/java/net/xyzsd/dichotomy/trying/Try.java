package net.xyzsd.dichotomy.trying;

import net.xyzsd.dichotomy.Empty;
import net.xyzsd.dichotomy.trying.function.ExBiFunction;
import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

// basic rules of thumb for methods:
//  if we return a Try, we can capture exceptions.
//  if we return a value (e.g., <V> or throwable), we cannot!
// general rules of nulls
//  nulls to arguments -> throws an exception (contract violated)
//  nulls after function application -> CAUGHT if Try returned, but thrown if value returned

public sealed interface Try<V> permits Try.Failure, Try.Success {

    /*

        todo:
            * TESTS
            * docs

     */


    static <T> Try<T> ofSuccess(@NotNull T value) {
        return Success.of( value );
    }

    static <T, X extends Throwable> Try<T> ofFailure(@NotNull X failure) {
        return Failure.of( failure );
    }


    // pass in a regular supplier via of(Supplier::get)
    static <T> Try<T> of(ExSupplier<T> xSupplier) {
        requireNonNull( xSupplier );
        try {
            return Success.of( xSupplier.get() );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }

    // also takes a regular function via passing in (in, fn::apply)
    static <T, R> Try<R> of(final T in, ExFunction<T, R> xFn) {
        requireNonNull( xFn );
        try {
            return Success.of( xFn.apply( in ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }

    static <T, U, R> Try<R> of(final T tIn, final U uIn, ExBiFunction<T, U, R> xBiFn) {
        requireNonNull( xBiFn );
        try {
            return Success.of( xBiFn.apply( tIn, uIn ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }


    // Supplier/ExSupplier to checked supplier, only invoked when used (lazy)
    static <T> ExSupplier<Try<T>> from(Supplier<T> supplier) {
        requireNonNull( supplier );
        return () -> of( supplier::get );
    }

    // same for functions (lazy)
    static <T, R> ExFunction<T, Try<R>> from(Function<T, R> xFn) {
        requireNonNull( xFn );
        return (t) -> of( t, xFn::apply );
    }

    static <T, U, R> BiFunction<T, U, Try<R>> from(ExBiFunction<T, U, R> xBiFn) {
        requireNonNull( xBiFn );
        return (t, u) -> of( t, u, xBiFn );
    }


    // 1 resource
    static <T, AC extends AutoCloseable> Try<T> withResources(ExSupplier<AC> supplier, ExFunction<AC, T> fn) {
        requireNonNull( supplier );
        requireNonNull( fn );

        try (AC ac = supplier.get()) {
            return Try.Success.of( fn.apply( ac ) );
        } catch (Throwable t) {
            return Try.Failure.of( t );
        }
    }

    // 2 resources
    static <T, AC1 extends AutoCloseable, AC2 extends AutoCloseable> Try<T> withResources(ExSupplier<AC1> supplier1,
                                                                                          ExSupplier<AC2> supplier2,
                                                                                          ExBiFunction<AC1, AC2, T> biFn) {
        requireNonNull( supplier1 );
        requireNonNull( supplier2 );
        requireNonNull( biFn );

        try (AC1 ac1 = supplier1.get(); AC2 ac2 = supplier2.get()) {
            return Try.Success.of( biFn.apply( ac1, ac2 ) );
        } catch (Throwable t) {
            return Try.Failure.of( t );
        }
    }


    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }


    boolean isFailure();

    boolean isSuccess();


    /**
     * Returns a {@link Try}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced {@link Try} (which may be {@link Failure} or {@link Success}) can have different types.
     * </p>
     *
     * @param fnSuccess the mapping function for {@link Success} values.
     * @param fnFailure the mapping function for {@link Failure} values.
     * @return the {@link Try} produced from {@code fnSuccess} or {@code fnFailure}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(ExFunction)
     */
    @NotNull <V2> Try<V2> biFlatMap(@NotNull ExFunction<? super V, ? extends Try<? extends V2>> fnSuccess,
                                    @NotNull ExFunction<? super Throwable, ? extends Try<? extends V2>> fnFailure);




    /**
     * Returns a value, produced from one of the appropriate mapping functions.
     * <p>
     * The produced value can have any type (except {@link Void}) but mapping functions for
     * both {@link Failure} and {@link Success} types must produce the same value type.
     * </p>
     *
     * @param fnSuccess the mapping function for {@link Failure} values.
     * @param fnFailure the mapping function for {@link Success} values.
     * @return the value produced from {@code fnSuccess} or {@code fnFailure}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #recover(Function)
     * @see #forfeit(Function)
     */
    @NotNull <T> T fold(@NotNull Function<? super V, ? extends T> fnSuccess,
                        @NotNull Function<? super Throwable, ? extends T> fnFailure);

    /**
     * Return a {@link Stream}, containing either a single {@link Success} value, or an empty {@link Stream}
     * if this is an {@link Failure} value.
     */
    @NotNull Stream<V> stream();

    /**
     * Filter a {@link Try}.
     * <p>
     * If this {@link Try} is {@link Failure}, return {@link Failure} ({@code this}).
     * The {@code Predicate} is not tested, and the mapper {@code Function} is not executed.
     * </p>
     * <p>
     * If this {@link Try} is {@link Success}, return {@link Success} ({@code this}) if the {@code Predicate} matches.
     * If the {@code Predicate} fails to match, return an {@link Failure} {@link Try} produced by applying the
     * mapping function to the current {@link Try} ({@code this}).
     * </p>
     *
     * @param predicate the predicate used to test {@link Success} values.
     * @param fnUnmatched the mapping function for {@link Success} values that do not match the predicate.
     * @return a {@link Try} based on the algorithm described above.
     * @throws NullPointerException if the called mapping function returns {@code null}.
     */
    @NotNull Try<V> filter(@NotNull Predicate<? super V> predicate, @NotNull ExFunction<? super V, ? extends Throwable> fnUnmatched);

    /**
     * Executes the action iff this is an {@link Success} {@link Try}.
     *
     * @return {@code this} if successful, otherwise returns a @link Failure} containing the exception.
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #consume(Consumer)
     * @see #consumeErr(Consumer)
     */
    @NotNull Try<V> exec(@NotNull Consumer<? super V> successConsumer);


    /**
     * Executes the given consumer if this is a {@link Success}. This is a terminal operation.
     * Unlike {@link #exec(Consumer)}, this method does <b>NOT</b> handle Exceptions.
     *
     * @param successConsumer the consumer function to be executed
     */
    void consume(@NotNull Consumer<? super V> successConsumer);


    /**
     * Executes the given consumer if this is a {@link Failure}; otherwise do nothing. This is a terminal operation.
     * This method does <b>NOT</b> handle Exceptions.
     *
     * @param failureConsumer the consumer function to be executed
     */
    void consumeErr(@NotNull Consumer<? super Throwable> failureConsumer);


    /**
     * If this is an {@link Success}, return a new {@link Success} value produced by the given mapping function.
     * Otherwise, return the {@link Failure} value.
     * <p>
     * The type of the produced {@link Success} can be different. The mapping function is only invoked for
     * {@link Success} values.
     * </p>
     * <p>
     * This is equivalent to {@code map( Function.identity(), rightMapper )}.
     * </p>
     *
     * @param fnSuccess the mapping function producing a new {@link Success} value.
     * @return a new {@link Success} produced by the mapping function if this is {@link Success};
     * otherwise, returns an {@link Failure}.
     * @throws NullPointerException if the Try of the mapping function is {@code null}
     */
    @NotNull <V2> Try<V2> map(@NotNull ExFunction<? super V, ? extends V2> fnSuccess);

    @NotNull Try<V> mapErr(@NotNull ExFunction<? super Throwable, ? extends Throwable> fnFailure);

    @NotNull <V2> Try<V2> biMap(@NotNull ExFunction<? super V, ? extends V2> fnSuccess, @NotNull ExFunction<? super Throwable, ? extends Throwable> fnFailure);


        /**
         * If this is an {@link Success}, return the new {@link Try} supplied by the mapping function.
         * Note that while the {@link Failure} type must remain the same, the {@link Success} type returned
         * can be different.
         * <p>
         * This is also known as {@code join()} in other implementations.
         * </p>
         * <p>
         * No mapping is performed if this is an {@link Failure}, and the mapping function is not invoked.
         * </p>
         *
         * @param fnSuccess the mapping function that produces a new {@link Try}
         * @return a new {@link Success} produced by the mapping function if this is {@link Success};
         * otherwise, returns an {@link Failure}.
         * @throws NullPointerException if the Try of the mapping function is {@code null}
         * @see #biFlatMap(ExFunction, ExFunction)
         */
    @NotNull <V2> Try<V2> flatMap(@NotNull ExFunction<? super V, ? extends Try<? extends V2>> fnSuccess);


    @NotNull Try<V> flatMapErr(@NotNull ExFunction<? super Throwable, ? extends Try<? extends V>> fnFailure);


    /**
     * Determines if this {@link Success} {@link Try} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link Failure} {@link Try}
     * </p>
     *
     * @param okPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link Success} {@link Try} and the {@link Predicate} matches.
     * @see #contains(Object)
     */
    boolean ifPredicate(@NotNull Predicate<V> okPredicate);

    /**
     * Determines if this {@link Success} {@link Try} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param okValue value to compare
     * @return {@code true} iff this is an {@link Success} {@link Try} and the contained value equals {@code okValue}
     * @see #ifPredicate(Predicate)
     */
    boolean contains(@Nullable V okValue);

    /**
     * If this {@link Try} is {@link Failure}, return {@code rightAlternate}.
     * Otherwise, return {@code this} (an {@link Success} {@link Try}).
     *
     * @param okAlternate alternate {@link Success} {@link Try}
     * @return {@code this}, or {@code okAlternate} if {@code this} is {@link Failure}
     * @see #orElseGet(Supplier)
     */
    @NotNull V orElse(@NotNull V okAlternate);

    /**
     * If this {@link Try} is {@link Failure}, return the supplied {@link Success} {@link Try}.
     * Otherwise, return {@code this} (an {@link Success} {@link Try}) without
     * invoking the {@link Supplier}.
     *
     * @param okSupplier supplier of {@link Success} {@link Try}s
     * @return {@code this}, or the supplied {@link Success} {@link Try} if {@code this} is {@link Failure}
     * @see #orElse(Object)
     */
    @NotNull V orElseGet(@NotNull Supplier<? extends V> okSupplier);

    /**
     * Recover from an error; ignore the {@link Failure} value if present,
     * and apply the mapping function to get an {@link Success}.
     * <p>
     * If this is an {@link Success}, return it without applying the mapping function.
     * </p>
     * <p>
     * This method is equivalent in alternative implementations to {@code orElseMap()}.
     * </p>
     *
     * @param fnFailureToSuccess {@link Function} that produces an {@link Success} value.
     * @return A {@link Success} value, either the current {@link Success} if present, or the produced {@link Success} if not.
     * @throws NullPointerException if the Try of the mapping function is {@code null}.
     * @see #forfeit(Function)
     */
    @NotNull V recover(@NotNull Function<? super Throwable, ? extends V> fnFailureToSuccess);


    /**
     * Forfeit (ignore) the {@link Success} value if present, and apply the mapping function to get an {@link Failure}.
     * <p>
     * If this is an {@link Failure}, return it without applying the mapping function.
     * </p>
     * <p>
     * This method is equivalent in alternative implementations to {@code orElseMapErr()}.
     * </p>
     *
     * @param fnSuccessToFailure {@link Function} that produces an {@link Failure} value.
     * @return A {@link Failure} value, either the current {@link Failure} if present, or the produced {@link Failure} if not.
     * @throws NullPointerException if the Try of the mapping function is {@code null}.
     * @see #recover(Function)
     */
    @NotNull Throwable forfeit(@NotNull Function<? super V, ? extends Throwable> fnSuccessToFailure);


    /**
     *
     * @return V
     * @throws NoSuchElementException if this is a {@Link Failure}, wrapping the Throwable.
     */
    @NotNull V expect() throws NoSuchElementException;

    <X extends Exception> @NotNull V getOrThrow(@NotNull Function<? super Throwable, X> exFn) throws X;



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Internal helper methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static <IN, OUT> Try<OUT> mapChecked(IN in, ExFunction<? super IN, ? extends OUT> fn) {
        try {
            return Success.of(  fn.apply( in ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }

    @SuppressWarnings("unchecked")
    private static <IN, OUT> Try<OUT> flatMapChecked(IN in, ExFunction<? super IN, ? extends Try<? extends OUT>> fn) {
        try {
            return (Try<OUT>)  requireNonNull( fn.apply( in ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    record Success<T>(@NotNull T value) implements Try<T> {
        public Success {
            Objects.requireNonNull( value );
        }

        public static Success<Empty> of() {
            return new Success<>( Empty.getInstance() );

        }

        public static <U> Success<U> of(final U value) {
            return new Success<>( value );

        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }


        @Override
        public @NotNull <V2> Try<V2> biFlatMap(@NotNull ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess,
                                               @NotNull ExFunction<? super Throwable, ? extends Try<? extends V2>> fnFailure) {
            Objects.requireNonNull( fnFailure );
            return flatMap( fnSuccess );
        }

        @Override
        public @NotNull <V2> Try<V2> biMap(@NotNull ExFunction<? super T, ? extends V2> fnSuccess, @NotNull ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            Objects.requireNonNull( fnSuccess );
            Objects.requireNonNull( fnFailure );
            return Try.mapChecked( value, fnSuccess );
        }

        @Override
        public <T1> @NotNull T1 fold(@NotNull Function<? super T, ? extends T1> fnSuccess,
                                     @NotNull Function<? super Throwable, ? extends T1> fnFailure) {
            Objects.requireNonNull( fnSuccess );
            Objects.requireNonNull( fnFailure );
            return Objects.requireNonNull( fnSuccess.apply( value ) );
        }


        @Override
        public @NotNull Stream<T> stream() {
            return Stream.of( value );
        }

        @Override
        public @NotNull Try<T> filter(@NotNull Predicate<? super T> predicate,
                                      @NotNull ExFunction<? super T, ? extends Throwable> fnUnmatched) {

            requireNonNull( predicate );
            requireNonNull( fnUnmatched );

            if (predicate.test( value )) {
                return this;
            }

            try {
                return Failure.of( fnUnmatched.apply( value ) );
            } catch (Throwable t) {
                return Failure.of( t );
            }
        }

        @Override
        public @NotNull Try<T> exec(@NotNull Consumer<? super T> successConsumer) {
            requireNonNull( successConsumer );
            try {
                successConsumer.accept( value );
                return this;
            } catch (Throwable t) {
                return Failure.of( t );
            }
        }

        @Override
        public void consume(@NotNull Consumer<? super T> successConsumer) {
            requireNonNull( successConsumer );
            successConsumer.accept( value );
        }

        @Override
        public void consumeErr(@NotNull Consumer<? super Throwable> failureConsumer) {
            requireNonNull( failureConsumer );
            // do nothing
        }

        @Override
        public @NotNull <V2> Try<V2> map(@NotNull ExFunction<? super T, ? extends V2> fnSuccess) {
            requireNonNull( fnSuccess );
            return mapChecked(value, fnSuccess);
        }

        @Override
        public @NotNull <V2> Try<V2> flatMap(@NotNull ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess) {
            requireNonNull( fnSuccess );
            return Try.flatMapChecked( value, fnSuccess );
        }

        @Override
        public @NotNull Try<T> mapErr(@NotNull ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            requireNonNull( fnFailure );
            return this;
        }

        @Override
        public @NotNull Try<T> flatMapErr(@NotNull ExFunction<? super Throwable, ? extends Try<? extends T>> fnFailure) {
            requireNonNull( fnFailure );
            return this;
        }

        @Override
        public boolean ifPredicate(@NotNull Predicate<T> okPredicate) {
            requireNonNull( okPredicate );
            return okPredicate.test( value );
        }

        @Override
        public boolean contains(@Nullable T okValue) {
            return Objects.equals( value, okValue );
        }

        @Override
        public @NotNull T orElse(@NotNull T okAlternate) {
            requireNonNull( okAlternate );
            return value;
        }

        @Override
        public @NotNull T orElseGet(@NotNull Supplier<? extends T> okSupplier) {
            requireNonNull( okSupplier );
            return value;
        }

        @Override
        public @NotNull T recover(@NotNull Function<? super Throwable, ? extends T> fnFailureToSuccess) {
            requireNonNull( fnFailureToSuccess );
            return value;
        }


        @Override
        public @NotNull Throwable forfeit(@NotNull Function<? super T, ? extends Throwable> fnSuccessToFailure) {
            requireNonNull( fnSuccessToFailure );
            return requireNonNull( fnSuccessToFailure.apply( value ) );
        }


        @Override
        public @NotNull T expect() throws NoSuchElementException {
            return value;
        }

        @Override
        public <X extends Exception> @NotNull T getOrThrow(@NotNull Function<? super Throwable, X> exFn) throws X {
            requireNonNull( exFn );
            return value;
        }
    }



    record Failure<T>(@NotNull Throwable err) implements Try<T> {

        public Failure {
            Objects.requireNonNull( err );
            throwIfFatal( err );
        }

        public static <U> Failure<U> of(Throwable t) {
            return new Failure<>( t );
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }


        @Override
        public @NotNull <V2> Try<V2> biFlatMap(@NotNull ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess,
                                               @NotNull ExFunction<? super Throwable, ? extends Try<? extends V2>> fnFailure) {
            Objects.requireNonNull( fnSuccess );
            Objects.requireNonNull( fnFailure );
            return Try.flatMapChecked( err, fnFailure );
        }

        @Override
        public @NotNull <V2> Try<V2> biMap(@NotNull ExFunction<? super T, ? extends V2> fnSuccess, @NotNull ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            Objects.requireNonNull( fnSuccess );
            Objects.requireNonNull( fnFailure );
            return failMap(fnFailure);
        }

        // Failures can only be mapped to failures if the fail, and if the failure mapping fails,
        // it still fails. It's failure all the way down!
        private <V2> Try<V2> failMap(ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            try {
                return Failure.of( requireNonNull( fnFailure.apply( err ) ) );
            } catch (Throwable t) {
                return Failure.of( t );
            }
        }

        @Override
        public <U> @NotNull U fold(@NotNull Function<? super T, ? extends U> fnSuccess, @NotNull Function<? super Throwable, ? extends U> fnFailure) {
            Objects.requireNonNull( fnSuccess );
            Objects.requireNonNull( fnFailure );
            return Objects.requireNonNull( fnFailure.apply( err ) );
        }

        @Override
        public @NotNull Stream<T> stream() {
            return Stream.empty();
        }

        @Override
        public @NotNull Try<T> filter(@NotNull Predicate<? super T> predicate, @NotNull ExFunction<? super T, ? extends Throwable> fnUnmatched) {
            Objects.requireNonNull( predicate );
            Objects.requireNonNull( fnUnmatched );
            return this;
        }

        @Override
        public @NotNull Try<T> exec(@NotNull Consumer<? super T> successConsumer) {
            Objects.requireNonNull( successConsumer );
            return this;
        }

        @Override
        public void consume(@NotNull Consumer<? super T> successConsumer) {
            Objects.requireNonNull( successConsumer );
        }

        @Override
        public @NotNull <V2> Try<V2> map(@NotNull ExFunction<? super T, ? extends V2> fnSuccess) {
            Objects.requireNonNull( fnSuccess );
            return coerce();
        }

        @Override
        public @NotNull <V2> Try<V2> flatMap(@NotNull ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess) {
            Objects.requireNonNull( fnSuccess );
            return coerce();
        }

        @Override
        public @NotNull Try<T> mapErr(@NotNull ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            requireNonNull( fnFailure );
            return failMap( fnFailure );
        }

        @Override
        public @NotNull Try<T> flatMapErr(@NotNull ExFunction<? super Throwable, ? extends Try<? extends T>> fnFailure) {
            requireNonNull( fnFailure );
            return Try.flatMapChecked( err, fnFailure );
        }

        @Override
        public boolean ifPredicate(@NotNull Predicate<T> okPredicate) {
            requireNonNull( okPredicate );
            return false;
        }

        @Override
        public boolean contains(@Nullable T okValue) {
            requireNonNull( okValue );
            return false;
        }

        @Override
        public @NotNull T orElse(@NotNull T okAlternate) {
            requireNonNull( okAlternate );
            return okAlternate;
        }

        @Override
        public @NotNull T orElseGet(@NotNull Supplier<? extends T> okSupplier) {
            requireNonNull( okSupplier );
            return Objects.requireNonNull( okSupplier.get() );
        }

        @Override
        public @NotNull T recover(@NotNull Function<? super Throwable, ? extends T> fnFailureToSuccess) {
            requireNonNull( fnFailureToSuccess );
            return Objects.requireNonNull( fnFailureToSuccess.apply( err ) );
        }

        @Override
        public @NotNull Throwable forfeit(@NotNull Function<? super T, ? extends Throwable> fnSuccessToFailure) {
            requireNonNull( fnSuccessToFailure );
            return err;
        }


        @Override
        public void consumeErr(@NotNull Consumer<? super Throwable> failureConsumer) {
            requireNonNull( failureConsumer );
            failureConsumer.accept( err );
        }


        @Override
        public @NotNull T expect() throws NoSuchElementException {
            throw new NoSuchElementException(err);
        }

        @Override
        public <X extends Exception> @NotNull T getOrThrow(@NotNull Function<? super Throwable, X> exFn) throws X {
            requireNonNull( exFn );
            throw requireNonNull( exFn.apply( err ) );
        }



        @SuppressWarnings("unchecked")
        private <V2> Failure<V2> coerce() {
            return (Failure<V2>) this;
        }

        /**
         * Throwables which are truly fatal that we will not capture.
         */
        private static void throwIfFatal(final Throwable t) {
            // TODO: add MatchException when we target JDK > 20
            //           and restructure to a switch-case w/pattern
            if (t instanceof VirtualMachineError || t instanceof LinkageError) {
                // Errors are similar to unchecked exceptions
                throw (Error) t;
            } else if (t instanceof InterruptedException) {
                sneakyThrow( t );
            }
        }


    }


}
