package net.xyzsd.dichotomy.trying;

import net.xyzsd.dichotomy.Empty;
import net.xyzsd.dichotomy.Result;
import net.xyzsd.dichotomy.trying.function.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


/**
 * {@code Try} is a specialized type of {@link Result} that handles code blocks that
 * can potentially throw exceptions.
 * <p>
 * The exception is untyped.
 * <p>
 * Similar to Scala, several 'fatal' exceptions (and subclasses) are not handled and will always be thrown:
 * <ul>
 *      <li>{@link VirtualMachineError}s</li>
 *      <li>{@link LinkageError}/li>
 *      <li>{@link InterruptedException}</li>
 * </ul>
 * <p>
 * In general, methods that return a {@code Try} will capture exceptions, and can return a failed {@code Try}
 * ({@link Failure}. The basic rule is:
 * <ul>
 *     <li>If a method returns a Try, we can capture exceptions (and thus a {@link Failure} may be returned)</li>
 *     <li>
 *         If a method returns a value (e.g., {@link #ifPredicate(Predicate)} or {@link #recover(Function)},
 *         exceptions will not be caught, and will be thrown.
 *     </li>
 * </ul>
 *
 * <p>
 * If a null argument is supplied to a method (unless explicitly marked @Nullable), an exception (NPE) will be thrown.
 * If a method which takes an {@link ExConsumer}, {@link ExSupplier}, or {@link ExFunction} returns {@code null},
 * the {@code NullPointerException} will be caught and returned as a {@link Failure}.
 *
 * @param <V> the Try type, held by Success values.
 */
@NullMarked
public sealed interface Try<V> permits Try.Failure, Try.Success {

    /**
     * Create a Successful Try.
     *
     * @param value contained by the {@link Success}
     * @param <T>   value type
     * @return {@link Success} containing the above value
     */
    
    static <T> Try<T> ofSuccess( T value) {
        return Success.of( value );
    }

    /**
     * Create an unsuccessful Try (Failure).
     *
     * @param failure contained by the {@link Failure}
     * @param <T>     Success value type (always empty in this case).
     * @return {@link Failure} containing the above value
     */
    
    static <T> Try<T> ofFailure( Throwable failure) {
        return Failure.of( failure );
    }



    /**
     * Invoke an {@link ExSupplier} to create a {@link Try}.
     * <p>
     * If the {@link ExSupplier} is successful, a {@link Success} will be returned.
     * If an exception is thrown by the {@link ExSupplier}, a {@link Failure} will be returned.
     * </p>
     * <p>
     * {@link ExSupplier}s can throw checked or unchecked Exceptions.
     * To use a {@link Supplier} (which can only throw RuntimeExceptions), use as follows:
     * {@snippet :
     *          Try<String> suppliedTry = Try.of( myStringSupplier::get );
     *          // or alternatively:
     *          Try<String> suppliedTry2 = Try.wrap( ExSupplier.from(myStringSupplier) );
     *}
     * </p>
     * <p>
     * If the {@code ExSupplier} returns {@code null}, this method
     * will return a {@link Failure} containing a {@link NullPointerException}.
     * </p>
     *
     * @param xSupplier a Supplier which could potentially throw an Exception.
     * @param <T>       non-exceptional type
     * @return supplied type or exception wrapped in a Try
     */
    
    static <T> Try<T> wrap( ExSupplier<T> xSupplier) {
        requireNonNull( xSupplier );
        try {
            // we still need to wrap in a requireNonNull() because we could create
            // an ad-hoc ExSupplier (e.g., as a code block) which could return null
            return Success.of( requireNonNull( xSupplier.get() ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }


    /**
     * Invoke an {@link ExRunnable} to create a {@link Try}.
     * <p>
     * If the {@link ExRunnable} is successful, a {@link Success} will be returned.
     * If an exception is thrown by the {@link ExRunnable}, a {@link Failure} will be returned.
     * </p>
     * <p>
     * {@link ExRunnable}s can throw checked or unchecked Exceptions.
     * To use a {@link Runnable} (which can only throw RuntimeExceptions), use as follows:
     * {@snippet :
     *          Try<Empty> firstTry = Try.of( myRunnable::run );
     *          // or alternatively:
     *          Try<Empty> secondTry = Try.wrap( ExRunnable.from(myRunnable) );
     *}
     * </p>
     * <p>
     *     This can also be used to wrap blocks of code that could throw exceptions:
     *     {@snippet :
     *          Try<Empty> blockTry = Try.wrap( () -> {
     *              String in = Files.readString( Path.of("path-to-a-file") );
     *              if(in.startsWith( "begin:")) {
     *                    Files.writeString( Path.of("path-to-another-file") );
     *           }
     *          });
     *}
     * </p>
     * @param runnable a Runnable which could potentially throw an Exception
     * @return Empty Try or an exception wrapped in a Try
     */
    
    static Try<Empty> wrap( ExRunnable runnable) {
        requireNonNull( runnable );
        try {
            runnable.run();
            return Try.Success.of();
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }



    /**
     * Try-with-resources for a single AutoCloseable.
     * <p>
     * The ExSupplier will supply the AutoCloseable resource.
     * The ExFunction will take the supplied resource and return a result.
     * The supplied resource will be automatically closed (as per try-with-resource semantics).
     * If no exceptions occur, the function result will be wrapped in a {@link Success}.
     * </p>
     * <p>
     * A {@link Failure} will be returned, containing an Exception when:
     * <ul>
     *     <li>An Exception occurs in the supplier (if so, the function will not be invoked)</li>
     *     <li>An Exception occurs in the function (if so, the resource will still be closed)</li>
     *     <li>An Exception occurs when the resource is closed</li>
     * </ul>
     * If an Exception occurs in the function (but not the supplier) <b>and</b> during resource closing, the
     * resource-closing Exception will be added as a suppressed exception (unless disallowed) to the
     * Exception thrown during function execution.
     *
     * @param supplier AutoCloseable resource supplier
     * @param fn       ExFunction to process AutoCloseable resource
     * @param <T>      ExFunction result
     * @param <AC>     AutoCloseable resource and ExFunction input
     * @return ExFunction result wrapped in a Try
     */
    static <T, AC extends AutoCloseable> Try<T> withResources(ExSupplier<AC> supplier, ExFunction<AC, T> fn) {
        requireNonNull( supplier );
        requireNonNull( fn );

        try (AC ac = supplier.get()) {
            return Success.of( fn.apply( ac ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }

    /**
     * Two-supplier, Two-parameter try-with-resources.
     * See {@link #withResources(ExSupplier, ExFunction)}.
     *
     * @param supplier1 First AutoCloseable resource supplier
     * @param supplier2 Second AutoCloseable resource supplier
     * @param biFn      function which can use both AutoCloseable resources supplied
     * @param <T>       biFunction result
     * @param <AC1>     First AutoCloseable resource and ExBiFunction input
     * @param <AC2>     Second AutoCloseable resource and ExBiFunction input
     * @return biFn result
     */
    static <T, AC1 extends AutoCloseable, AC2 extends AutoCloseable> Try<T> withResources(ExSupplier<AC1> supplier1,
                                                                                          ExSupplier<AC2> supplier2,
                                                                                          ExBiFunction<AC1, AC2, T> biFn) {
        requireNonNull( supplier1 );
        requireNonNull( supplier2 );
        requireNonNull( biFn );

        try (AC1 ac1 = supplier1.get(); AC2 ac2 = supplier2.get()) {
            return Success.of( biFn.apply( ac1, ac2 ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }


    /**
     * Determines if this is a Failure.
     *
     * @return {@code true} if this is a {@link Failure}.
     */
    boolean isFailure();

    /**
     * Determines if this is a Success.
     *
     * @return {@code true} if this is a {@link Success}.
     */
    boolean isSuccess();


    /**
     * Returns a {@link Try}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced {@link Try} (which may be {@link Failure} or {@link Success}) can have different types.
     * </p>
     *
     * @param fnSuccess the mapping function for {@link Success} values.
     * @param fnFailure the mapping function for {@link Failure} values.
     * @param <V2>      returned type, which can be different from the original type
     * @return the {@link Try} produced from {@code fnSuccess} or {@code fnFailure}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(ExFunction)
     */
     <V2> Try<V2> biFlatMap( ExFunction<? super V, ? extends Try<? extends V2>> fnSuccess,
                                     ExFunction<? super Throwable, ? extends Try<? extends V2>> fnFailure);


    /**
     * Returns a value, produced from one of the appropriate mapping functions.
     * <p>
     * The produced value can have any type (except {@link Void}) but mapping functions for
     * both {@link Failure} and {@link Success} types must produce the same value type.
     * </p>
     *
     * @param fnSuccess the mapping function for {@link Failure} values.
     * @param fnFailure the mapping function for {@link Success} values.
     * @param <T>       returned type, which must be the same for both Failure and Success values.
     * @return the value produced from {@code fnSuccess} or {@code fnFailure}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #recover(Function)
     * @see #forfeit(Function)
     */
     <T> T fold( Function<? super V, ? extends T> fnSuccess,
                         Function<? super Throwable, ? extends T> fnFailure);

    /**
     * Return a {@link Stream}, containing either a single {@link Success} value, or an empty {@link Stream}
     * if this is an {@link Failure} value.
     *
     * @return Stream
     */
     Stream<V> stream();

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
     * @param predicate   the predicate used to test {@link Success} values.
     * @param fnUnmatched the mapping function for {@link Success} values that do not match the predicate.
     * @return a {@link Try} based on the algorithm described above.
     * @throws NullPointerException if the called mapping function returns {@code null}.
     */
     Try<V> filter( Predicate<? super V> predicate,  ExFunction<? super V, ? extends Throwable> fnUnmatched);

    /**
     * Executes the action iff this is an {@link Success} {@link Try}.
     *
     * @param successConsumer Consumer of Success values
     * @return {@code this} if successful, otherwise returns a {@link Failure} containing the Exception.
     * @throws NullPointerException if successConsumer is {@code null}.
     * @see #consume(Consumer)
     * @see #consumeErr(Consumer)
     */
     Try<V> exec( ExConsumer<? super V> successConsumer);


    /**
     * Executes the given consumer if this is a {@link Success}. This is a terminal operation.
     * Unlike {@link #exec(ExConsumer)}, this method does <b>NOT</b> handle Exceptions.
     *
     * @param successConsumer the consumer function to be executed
     * @throws NullPointerException if successConsumer is {@code null}.
     */
    void consume( Consumer<? super V> successConsumer);


    /**
     * Executes the given consumer if this is a {@link Failure}; otherwise do nothing. This is a terminal operation.
     * This method does <b>NOT</b> handle Exceptions.
     *
     * @param failureConsumer the consumer function to be executed
     */
    void consumeErr( Consumer<? super Throwable> failureConsumer);


    /**
     * If this is an {@link Success}, return a new {@link Success} value produced by the given mapping function.
     * Otherwise, return the {@link Failure} value.
     * <p>
     * The type of the produced {@link Success} can be different. The mapping function is only invoked for
     * {@link Success} values.
     * </p>
     * <p>
     * Any non-fatal exception thrown by the mapping function will be caught, and a {@link Failure} will be returned
     * containing that Exception.
     * </p>
     *
     * @param fnSuccess the mapping function producing a new {@link Success} value.
     * @param <V2>      returned type, which can be different from the original type
     * @return a new {@link Success} produced by the mapping function, if applied.
     */
     <V2> Try<V2> map( ExFunction<? super V, ? extends V2> fnSuccess);

    /**
     * If this is an {@link Failure}, return a new {@link Failure} value produced by the given mapping function.
     * <p>
     * Error mapping can only result in a {@link Failure}, but can result in a different type of failure.
     * </p>
     * <p>
     * Any non-fatal Exception thrown by the mapping function will be caught, and a {@link Failure} will be returned
     * containing that Exception.
     * </p>
     *
     * @param fnFailure the mapping function producing a new {@link Failure} value.
     * @return a new {@link Try} produced by the mapping function, if applied
     */
     Try<V> mapErr( ExFunction<? super Throwable, ? extends Throwable> fnFailure);


    /**
     * Returns a new {@link Try}, the value of which is determined by the appropriate mapping function.
     * <p>
     * The returned {@link Try} (which may be {@link Success} or {@link Failure}) can have different types.
     * </p>
     * <p>
     * Any non-fatal exception thrown by the executed mapping function will be caught, and a {@link Failure} will be returned
     * containing that Exception.
     * </p>
     *
     * @param fnSuccess the mapping function for {@link Success} values.
     * @param fnFailure the mapping function for {@link Failure} values.
     * @param <V2>      returned type, which can be different from the original type
     * @return the {@link Try} produced from {@code okMapper} or {@code errMapper}, or a {@link Failure} if an Exception is caught.
     * @see #map(ExFunction)
     * @see #mapErr(ExFunction)
     */
     <V2> Try<V2> biMap( ExFunction<? super V, ? extends V2> fnSuccess,
                                 ExFunction<? super Throwable, ? extends Throwable> fnFailure);


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
     * <p>
     * Any non-fatal exception thrown by the mapping function will be caught, and a {@link Failure} will be returned.
     * </p>
     *
     * @param fnSuccess the mapping function that produces a new {@link Try}
     * @param <V2>      returned type, which can be different from the original type
     * @return a new {@link Try} produced by the mapping function, if applied
     * @see #flatMapErr(ExFunction)
     * @see #biFlatMap(ExFunction, ExFunction)
     */
     <V2> Try<V2> flatMap( ExFunction<? super V, ? extends Try<? extends V2>> fnSuccess);


    /**
     * If this is an {@link Failure}, return the new {@link Try} supplied by the mapping function.
     * Note that the {@link Success} type must remain the same.
     * <p>
     * This is also known as {@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link Success}, and the mapping function is not invoked.
     * </p>
     * <p>
     * Any non-fatal exception thrown by the mapping function will be caught, and a {@link Failure} will be returned.
     * </p>
     *
     * @param fnFailure the mapping function that produces a new {@link Try}
     * @return a new {@link Try} produced by the mapping function, if applied
     * otherwise, returns an {@link Failure}.
     * @see #flatMapErr(ExFunction)
     * @see #biFlatMap(ExFunction, ExFunction)
     */
     Try<V> flatMapErr( ExFunction<? super Throwable, ? extends Try<? extends V>> fnFailure);


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
    boolean ifPredicate( Predicate<V> okPredicate);

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
     V orElse( V okAlternate);

    /**
     * If this {@link Try} is {@link Failure}, return the supplied {@link Success} {@link Try}.
     * Otherwise, return {@code this} (an {@link Success} {@link Try}) without
     * invoking the {@link Supplier}.
     *
     * @param okSupplier supplier of {@link Success} {@link Try}s
     * @return {@code this}, or the supplied {@link Success} {@link Try} if {@code this} is {@link Failure}
     * @see #orElse(Object)
     */
     V orElseGet( Supplier<? extends V> okSupplier);


    /**
     * If {@code this} is {@link Failure}, return it. Otherwise, return the next {@link Try} given.
     * The next {@link Try} can have a different parameterized type.
     *
     * @param nextTry The {@link Try} to return.
     * @param <V2>    type of the value, which can be different from the original type
     * @return this or the given Try
     * @see #and(ExSupplier)
     * @see #or(Try)
     * @see #or(ExSupplier)
     */
     <V2> Try<V2> and( Try<V2> nextTry);


    /**
     * If {@code this} is {@link Failure}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Try} supplied.
     * The next {@link Try} can have a different parameterized type.
     *
     * @param nextTrySupplier The supplier of a {@link Try} to return; only called if {@code this} is a {@link Success}.
     * @param <V2>            type of the value, which can be different from the original type
     * @return this or the supplied Try
     * @throws NullPointerException if the supplied {@link Try} is {@code null}.
     * @see #and(Try)
     * @see #or(Try)
     * @see #or(ExSupplier)
     */
    default  <V2> Try<V2> and( final ExSupplier<Try<V2>> nextTrySupplier) {
        requireNonNull( nextTrySupplier );
        return flatMap( v -> nextTrySupplier.get() );
    }


    /**
     * If {@code this} is {@link Success}, return it.
     * Otherwise, return the next {@link Try} given.
     *
     * @param nextTry The {@link Try} to return.
     * @return this or the given Try
     * @see #or(ExSupplier)
     * @see #and(Try)
     * @see #and(ExSupplier)
     */
     Try<V> or( Try<V> nextTry);

    /**
     * If {@code this} is {@link Success}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Try} supplied.
     *
     * @param nextTrySupplier The supplier of a {@link Try} to return; only called if {@code this} is a {@link Failure}.
     * @return this or the supplied Try
     * @throws NullPointerException if the supplier is called and returns {@code null}.
     * @see #or(Try)
     * @see #and(Try)
     * @see #and(ExSupplier)
     */
    default  Try<V> or( ExSupplier<Try<V>> nextTrySupplier) {
        requireNonNull( nextTrySupplier );
        return flatMapErr( t -> nextTrySupplier.get() );
    }


    /**
     * Recover from an value; ignore the {@link Failure} value if present,
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
     V recover( Function<? super Throwable, ? extends V> fnFailureToSuccess);


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
     Throwable forfeit( Function<? super V, ? extends Throwable> fnSuccessToFailure);


    /**
     * Returns a value or throws a {@link NoSuchElementException}.
     *
     * @return V
     * @throws NoSuchElementException if this is a {@link Failure}, with the contained Throwable set as its cause.
     */
     V expect() throws NoSuchElementException;

    /**
     * Returns a value, or throw the given Throwable.
     * <p>
     * This method can wrap a throwable easily if the desired Exception can be created
     * with a Throwable as a cause:
     * {@snippet :
     *      getOrThrow(IOException::new);   // equivalent to new IOException(Throwable)
     *}
     * If the exception is a checked exception, it must be caught or
     * declared within a methods {@code throws} clause.
     * <p>
     * If setting the throwable as a cause is desirable, this form could be used:
     * {@snippet :
     *      getOrThrow( (x) -> new IOException("This is my exception message"));
     *}
     *
     * @param exFn Exception (Throwable) mapping function
     * @param <X>  Exception to throw; if not a (subclass of) RuntimeException, it must be rethrown or caught
     * @return V
     * @throws X Exception to throw
     */
    <X extends Exception>  V getOrThrow( Function<? super Throwable, X> exFn) throws X;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Internal helper methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static <IN, OUT> Try<OUT> mapChecked(IN in, ExFunction<? super IN, ? extends OUT> fn) {
        try {
            return Success.of( fn.apply( in ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }


    @SuppressWarnings("unchecked")
    private static <IN, OUT> Try<OUT> flatMapChecked(IN in, ExFunction<? super IN, ? extends Try<? extends OUT>> fn) {
        try {
            return (Try<OUT>) requireNonNull( fn.apply( in ) );
        } catch (Throwable t) {
            return Failure.of( t );
        }
    }


    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * A Successful Try.
     *
     * @param value Successful value
     * @param <T>   value type parameter
     */
    record Success<T>( T value) implements Try<T> {
        /**
         * A Successful Try.
         *
         * @param value successful value.
         */
        public Success {
            requireNonNull( value );
        }

        /**
         * Create a Successful empty Try.
         *
         * @return Successful Try with the {@link Empty} type.
         */
        public static Success<Empty> of() {
            return new Success<>( Empty.getInstance() );
        }

        /**
         * Create a Successful Try.
         *
         * @param value successful value.
         * @param <U>   type of value
         * @return Success containing the given value
         */
        public static <U> Success<U> of( final U value) {
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
        public  <V2> Try<V2> biFlatMap( ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess,
                                                ExFunction<? super Throwable, ? extends Try<? extends V2>> fnFailure) {
            requireNonNull( fnFailure );
            return flatMap( fnSuccess );
        }

        @Override
        public  <V2> Try<V2> biMap( ExFunction<? super T, ? extends V2> fnSuccess,  ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            requireNonNull( fnSuccess );
            requireNonNull( fnFailure );
            return Try.mapChecked( value, fnSuccess );
        }

        @Override
        public <T1>  T1 fold( Function<? super T, ? extends T1> fnSuccess,
                                      Function<? super Throwable, ? extends T1> fnFailure) {
            requireNonNull( fnSuccess );
            requireNonNull( fnFailure );
            return requireNonNull( fnSuccess.apply( value ) );
        }


        @Override
        public  Stream<T> stream() {
            return Stream.of( value );
        }

        @Override
        public  Try<T> filter( Predicate<? super T> predicate,
                                       ExFunction<? super T, ? extends Throwable> fnUnmatched) {
            requireNonNull( predicate );
            requireNonNull( fnUnmatched );

            try {
                if (predicate.test( value )) {
                    return this;
                }
                return Failure.of( fnUnmatched.apply( value ) );
            } catch (Throwable t) {
                // this includes predicate testing failures
                return Failure.of( t );
            }
        }

        @Override
        public  Try<T> exec( ExConsumer<? super T> successConsumer) {
            requireNonNull( successConsumer );
            try {
                successConsumer.accept( value );
                return this;
            } catch (Throwable t) {
                return Failure.of( t );
            }
        }

        @Override
        public void consume( Consumer<? super T> successConsumer) {
            requireNonNull( successConsumer );
            successConsumer.accept( value );
        }

        @Override
        public void consumeErr( Consumer<? super Throwable> failureConsumer) {
            requireNonNull( failureConsumer );
            // do nothing
        }

        @Override
        public  <V2> Try<V2> map( ExFunction<? super T, ? extends V2> fnSuccess) {
            requireNonNull( fnSuccess );
            return mapChecked( value, fnSuccess );
        }

        @Override
        public  <V2> Try<V2> flatMap( ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess) {
            requireNonNull( fnSuccess );
            return Try.flatMapChecked( value, fnSuccess );
        }

        @Override
        public  Try<T> mapErr( ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            requireNonNull( fnFailure );
            return this;
        }

        @Override
        public  Try<T> flatMapErr( ExFunction<? super Throwable, ? extends Try<? extends T>> fnFailure) {
            requireNonNull( fnFailure );
            return this;
        }

        @Override
        public boolean ifPredicate( Predicate<T> okPredicate) {
            requireNonNull( okPredicate );
            return okPredicate.test( value );
        }

        @Override
        public boolean contains(@Nullable T okValue) {
            return Objects.equals( value, okValue );
        }

        @Override
        public  T orElse( T okAlternate) {
            requireNonNull( okAlternate );
            return value;
        }

        @Override
        public  T orElseGet( Supplier<? extends T> okSupplier) {
            requireNonNull( okSupplier );
            return value;
        }


        @Override
        public  <V2> Try<V2> and( Try<V2> nextTry) {
            requireNonNull( nextTry );
            return nextTry;
        }

        @Override
        public  Try<T> or( Try<T> nextTry) {
            requireNonNull( nextTry );
            return this;
        }


        @Override
        public  T recover( Function<? super Throwable, ? extends T> fnFailureToSuccess) {
            requireNonNull( fnFailureToSuccess );
            return value;
        }


        @Override
        public  Throwable forfeit( Function<? super T, ? extends Throwable> fnSuccessToFailure) {
            requireNonNull( fnSuccessToFailure );
            return requireNonNull( fnSuccessToFailure.apply( value ) );
        }


        @Override
        public  T expect() throws NoSuchElementException {
            return value;
        }

        @Override
        public <X extends Exception>  T getOrThrow( Function<? super Throwable, X> exFn) throws X {
            requireNonNull( exFn );
            return value;
        }
    }


    /**
     * A Failed Try, which holds a single Throwable.
     *
     * @param err value value
     * @param <T> success value type (success value is always empty for Failures)
     */
    record Failure<T>( Throwable err) implements Try<T> {

        /**
         * An Unsuccessful Try. This will never permit 'fatal' exceptions (see documentation for {@link Try}).
         *
         * @param err Throwable
         */
        public Failure {
            requireNonNull( err );
            throwIfFatal( err );
        }

        /**
         * Create an Unsuccessful Try. This will never permit 'fatal' exceptions (see documentation for {@link Try}).
         *
         * @param t   value value
         * @param <U> Success value type (always empty)
         * @return {@code Try.Failure} containing the value value.
         */
        public static <U> Failure<U> of( Throwable t) {
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
        public  <V2> Try<V2> biFlatMap( ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess,
                                                ExFunction<? super Throwable, ? extends Try<? extends V2>> fnFailure) {
            requireNonNull( fnSuccess );
            requireNonNull( fnFailure );
            return Try.flatMapChecked( err, fnFailure );
        }

        @Override
        public  <V2> Try<V2> biMap( ExFunction<? super T, ? extends V2> fnSuccess,  ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            requireNonNull( fnSuccess );
            requireNonNull( fnFailure );
            return failMap( fnFailure );
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
        public <U>  U fold( Function<? super T, ? extends U> fnSuccess,  Function<? super Throwable, ? extends U> fnFailure) {
            requireNonNull( fnSuccess );
            requireNonNull( fnFailure );
            return requireNonNull( fnFailure.apply( err ) );
        }

        @Override
        public  Stream<T> stream() {
            return Stream.empty();
        }

        @Override
        public  Try<T> filter( Predicate<? super T> predicate,
                                       ExFunction<? super T, ? extends Throwable> fnUnmatched) {
            requireNonNull( predicate );
            requireNonNull( fnUnmatched );
            return this;
        }

        @Override
        public  Try<T> exec( ExConsumer<? super T> successConsumer) {
            requireNonNull( successConsumer );
            return this;
        }

        @Override
        public void consume( Consumer<? super T> successConsumer) {
            requireNonNull( successConsumer );
        }

        @Override
        public  <V2> Try<V2> map( ExFunction<? super T, ? extends V2> fnSuccess) {
            requireNonNull( fnSuccess );
            return coerce();
        }

        @Override
        public  <V2> Try<V2> flatMap( ExFunction<? super T, ? extends Try<? extends V2>> fnSuccess) {
            requireNonNull( fnSuccess );
            return coerce();
        }

        @Override
        public  Try<T> mapErr( ExFunction<? super Throwable, ? extends Throwable> fnFailure) {
            requireNonNull( fnFailure );
            return failMap( fnFailure );
        }

        @Override
        public  Try<T> flatMapErr( ExFunction<? super Throwable, ? extends Try<? extends T>> fnFailure) {
            requireNonNull( fnFailure );
            return Try.flatMapChecked( err, fnFailure );
        }

        @Override
        public boolean ifPredicate( Predicate<T> okPredicate) {
            requireNonNull( okPredicate );
            return false;
        }

        @Override
        public boolean contains(@Nullable T okValue) {
            return false;
        }

        @Override
        public  T orElse( T okAlternate) {
            requireNonNull( okAlternate );
            return okAlternate;
        }

        @Override
        public  T orElseGet( Supplier<? extends T> okSupplier) {
            requireNonNull( okSupplier );
            return requireNonNull( okSupplier.get() );
        }

        @Override
        public  <V2> Try<V2> and( Try<V2> nextTry) {
            requireNonNull( nextTry );
            return coerce();
        }

        @Override
        public  Try<T> or( Try<T> nextTry) {
            requireNonNull( nextTry );
            return nextTry;
        }

        @Override
        public  T recover( Function<? super Throwable, ? extends T> fnFailureToSuccess) {
            requireNonNull( fnFailureToSuccess );
            return requireNonNull( fnFailureToSuccess.apply( err ) );
        }

        @Override
        public  Throwable forfeit( Function<? super T, ? extends Throwable> fnSuccessToFailure) {
            requireNonNull( fnSuccessToFailure );
            return err;
        }


        @Override
        public void consumeErr( Consumer<? super Throwable> failureConsumer) {
            requireNonNull( failureConsumer );
            failureConsumer.accept( err );
        }


        @Override
        public  T expect() throws NoSuchElementException {
            throw new NoSuchElementException( err );
        }

        @Override
        public <X extends Exception>  T getOrThrow( Function<? super Throwable, X> exFn) throws X {
            requireNonNull( exFn );
            throw requireNonNull( exFn.apply( err ) );
        }


        @SuppressWarnings("unchecked")
        private <V2> Failure<V2> coerce() {
            return (Failure<V2>) this;
        }

        /**
         * For Throwables which are truly fatal, we will not capture.
         */
        private static void throwIfFatal(final Throwable t) {
            if (t instanceof VirtualMachineError || t instanceof LinkageError) {
                // Errors are similar to unchecked (Runtime) exceptions
                throw (Error) t;
            } else if (t instanceof MatchException me) {
                throw me;   // a RuntimeException
            } else if (t instanceof InterruptedException) {
                sneakyThrow( t );
            }
        }


    }


}
