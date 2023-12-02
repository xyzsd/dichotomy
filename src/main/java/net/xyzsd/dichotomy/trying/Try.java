package net.xyzsd.dichotomy.trying;


import net.xyzsd.dichotomy.Empty;
import net.xyzsd.dichotomy.Result;
import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import net.xyzsd.dichotomy.trying.function.SpecExSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Result}-like monad specialized to handle both successful and failed operations, but where
 * failed operations are always {@link Exception}s.
 *
 *
 * @param <V> The type of the value contained in the Try.
 * @param <E> The type of the exception that can occur in the Try.
 */
public sealed interface Try<V, E extends Exception> {


    /**
     * Create an {@link OK} (Success) {@link Try} for the given non-null value.
     *
     * @param value value for success
     * @param <V>   Value type
     * @param <E>   Error Exception type
     * @return {@link OK}  {@link Try} containing the given value.
     */
    @NotNull
    static <V, E extends Exception> Try<V, E> ofOK(@NotNull V value) {
        return new OK<>( value );
    }

    /**
     * Create an empty {@link OK} (Success) {@link Try}.
     * <p>
     * All empty values use the {@link Empty} type.
     * </p>
     *
     * @param <E> Error value
     * @return {@link OK} {@link Try} containing the given value.
     */
    @NotNull
    static <E extends Exception> Try<Empty, E> ofOK() {
        return new OK<>( Empty.getInstance() );
    }

    /**
     * Create an {@link Err} (Failure) {@link Try} for the given non-null value.
     *
     * @param error Error
     * @param <V>   Value type
     * @param <E>   Error Exception type
     * @return {@link Err} containing the given error Exception.
     */
    @NotNull
    static <V, E extends Exception> Try<V, E> ofErr(@NotNull E error) {
        return new Err<>( error );
    }

    /**
     * Creates a Try object by executing a supplied function and handling exceptions.
     *
     * @param supplier the supplier function that provides the result
     * @param exClass the class of exception to be handled
     * @param <V> the type of result
     * @param <X> the type of exception
     * @return a Try object representing the result or exception
     * @throws NullPointerException if supplier is null
     * @throws RuntimeException if an exception not assignable to exClass occurs
     */
    @NotNull
    static <V, X extends Exception> Try<V, X> of(@NotNull Supplier<V> supplier, @NotNull Class<X> exClass) {
        requireNonNull( supplier );

        try {
            return new OK<>( supplier.get() );
        } catch (Throwable t) {
            if (exClass.isInstance( t )) {
                return new Err<>( exClass.cast( t ) );
            }
            throw t;
        }
    }

    /**
     * Creates a Try object based on the result of the given supplier.
     *
     * @param supplier the supplier that provides the value
     * @param <V> the type of the value
     * @return a Try object representing the result of the supplier
     * @throws NullPointerException if the supplier is null
     */
    // ANY runtime exception. standard Suppliers Can't throw checked exceptions (no throws clause defined), so, not included
    @NotNull
    static <V> Try<V, RuntimeException> of(@NotNull Supplier<V> supplier) {
        requireNonNull( supplier );

        try {
            return new OK<>( supplier.get() );
        } catch (RuntimeException ex) {
            return new Err<>( ex );
        }
    }



    /**
     * Get a {@link Try} from an {@link ExSupplier}.
     * <p>
     * The {@link ExSupplier} may throw a checked or unchecked {@link Exception}.
     * The result of the function, or {@link Exception} will be contained in the {@link Try}.
     * </p>
     *
     * @param exSupplier Supplier to invoke
     * @param <V>        type of result supplied
     * @return A {@link Try} containing the supplied value or an {@link Exception}
     * @see #from(ExSupplier)
     * @see #of(Supplier)
     * @see #from(Supplier)
     */
    @NotNull
    static <V> Try<V, Exception> of(@NotNull ExSupplier<V> exSupplier) {
        requireNonNull( exSupplier );

        try {
            return new Try.OK<>( exSupplier.get() );
        } catch (Exception ex) {
            return new Try.Err<>( ex );
        }
    }

    /**
     * Converts an {@link Supplier} which may throw {@link RuntimeException}s to a {@link Supplier} of {@link Try}s.
     *
     * @param supplier Supplier to invoke
     * @param <V>      type of result supplied
     * @return A {@link Try} containing the supplied value or a {@link RuntimeException}
     * @see #of(Supplier)
     * @see #of(ExSupplier)
     * @see #from(ExSupplier)
     */
    @NotNull
    static <V> Supplier<Try<V, RuntimeException>> from(@NotNull Supplier<V> supplier) {
        requireNonNull( supplier );
        return () -> of( supplier );
    }

    /**
     * Converts an {@link ExSupplier} to a {@link Supplier} of {@link Try}s.
     * <p>
     * The {@link ExSupplier} may throw a checked or unchecked {@link Exception},
     * which will be wrapped in an {@link Err} {@link Try}.
     * </p>
     *
     * @param exSupplier Supplier to invoke
     * @param <V>        type of result supplied
     * @return A {@link Try} containing the supplied value or an {@link Exception}
     */
    @NotNull
    static <V> Supplier<Try<V, Exception>> from(@NotNull ExSupplier<V> exSupplier) {
        requireNonNull( exSupplier );
        return () -> of( exSupplier );
    }

    /**
     * Converts a {@link SpecExSupplier} to a {@link Supplier} of {@link Try}s.
     * <p>
     * The returned {@link Supplier} will only wrap {@link Exception}s of the given class
     * parameter {@code <X>} (and subclasses); all other {@link RuntimeException}s will be thrown.
     * </p>
     *
     * @param exSupplier {@link SpecExSupplier} to wrap
     * @param <V>        value wrapped by the given {@link Try}.
     * @param <X>        {@link RuntimeException} wrapped by the given {@link Try}.
     * @return A {@link Supplier} of {@link Try}s containing the supplied value or the specified
     * {@link RuntimeException}{@code <X>}
     * @see #of(SpecExSupplier, Class)
     * @see #from(ExSupplier)
     * @see #from(Supplier)
     */
    @NotNull
    static <V, X extends RuntimeException> Supplier<Try<V, X>> from(@NotNull SpecExSupplier<V, X> exSupplier, final Class<X> exClass) {
        requireNonNull( exSupplier );
        return () -> of( exSupplier, exClass );
    }

    /**
     * Execute a {@link SpecExSupplier} to a supply a {@link Try}.
     * <p>
     * The returned {@link Try} will contain the supplied value or the {@link RuntimeException} of the given class
     * parameter {@code <X>} (and subclasses); all other {@link RuntimeException}s will be thrown.
     * </p>
     *
     * @param exSupplier {@link SpecExSupplier} to wrap
     * @param <V>        supplied value wrapped by a {@link Try}.
     * @param <X>        {@link RuntimeException} type wrapped by a {@link Try}.
     * @return A {@link Try} containing the supplied value or the specified {@link RuntimeException}
     * @see #from(SpecExSupplier, Class)
     */
    @NotNull
    static <V, X extends RuntimeException> Try<V, X> of(@NotNull SpecExSupplier<V, X> exSupplier, final Class<X> exClass) {
        requireNonNull( exSupplier );
        requireNonNull( exClass );

        try {
            return new Try.OK<>( exSupplier.get() );
        } catch (RuntimeException ex) {
            if (exClass.isAssignableFrom( ex.getClass() )) {
                return new Try.Err<>( exClass.cast( ex ) );
            }
            throw ex;
        }
    }

    /**
     * Apply the given {@link ExFunction}, returning a {@link Try} which wraps either the return value
     * of the {@link ExFunction} or an {@link Exception}.
     *
     * @param in   value applied
     * @param exFn function, which may throw an exception
     * @param <T>  function input
     * @param <R>  function output
     * @return A {@link Try} wrapping either the output value or an {@link Exception}.
     * @see #from(ExFunction)
     */
    @NotNull
    static <T, R> Try<R, Exception> of(@Nullable T in, @NotNull final ExFunction<T, R> exFn) {
        requireNonNull( exFn );
        return fnToTry( in, exFn );
    }

    /**
     * Given an {@link Exception}-producing function ({@link ExFunction}), returns a {@link Function} which
     * returns a {@link Try} wrapping the function output or {@link Exception}. This is lazy; the function
     * is not evaluated.
     *
     * @param exFn {@link ExFunction} which may generate an exception
     * @param <T>  function input
     * @param <R>  function output
     * @return {@link Function} returning {@link Try}s which wrap the output or {@link Exception}
     * @see #of(Object, ExFunction)
     */
    @NotNull
    static <T, R> Function<T, Try<R, Exception>> from(@NotNull final ExFunction<T, R> exFn) {
        requireNonNull( exFn );
        return (in) -> fnToTry( in, exFn );
    }

    private static <T, R> Try<R, Exception> fnToTry(@Nullable T in, @NotNull final ExFunction<T, R> exFn) {
        try {
            return new Try.OK<>( exFn.apply( in ) );
        } catch (Exception ex) {
            return new Try.Err<>( ex );
        }
    }

    /**
     * Get the {@link Result.OK} value V as an {@link Optional}.
     *
     * @return return the {@link Result.OK} value if present; otherwise, return an empty {@link Optional}.
     */
    @NotNull Optional<V> ok();

    /**
     * Get the {@link Result.Err} value E as an {@link Optional}.
     *
     * @return return {@link Result.Err} value if present; otherwise, return an empty {@link Optional}.
     */
    @NotNull Optional<E> err();


    /////////////////////////////////////////////////
    // Interface methods
    /////////////////////////////////////////////////

    /**
     * Executes the action for the {@link OK} or {@link Err} depending upon
     * the value of this {@link Try}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #matchErr(Consumer)
     */
    @NotNull Try<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer);

    /**
     * Returns a new {@link Try}, the value of which is determined by the appropriate mapping function.
     * <p>
     * The returned {@link Try} (which may be {@link OK} or {@link Err}) can have different types.
     * </p>
     *
     * @param okMapper  the mapping function for {@link OK} values.
     * @param errMapper the mapping function for {@link Err} values.
     * @return the {@link Try} produced from {@code okMapper} or {@code errMapper}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapErr(Function)
     */
    @NotNull <V2, E2 extends Exception> Try<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper, @NotNull Function<? super E, ? extends E2> errMapper);

    /**
     * Returns a {@link Try}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced {@link Try} (which may be {@link Err} or {@link OK}) can have different types.
     * </p>
     *
     * @param fnOK  the mapping function for {@link OK} values.
     * @param fnErr the mapping function for {@link Err} values.
     * @return the {@link Try} produced from {@code fnOK} or {@code fnErr}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapErr(Function) (Function)
     */
    @NotNull <V2, E2 extends Exception> Try<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Try<? extends V2, ? extends E2>> fnOK, @NotNull Function<? super E, ? extends Try<? extends V2, ? extends E2>> fnErr);

    /**
     * Returns a value, produced from one of the appropriate mapping functions.
     * <p>
     * The produced value can have any type (except {@link Void}) but mapping functions for
     * both {@link Err} and {@link OK} types must produce the same value type.
     * </p>
     * <p>
     * If no value is to be returned, use {@link #biMatch(Consumer, Consumer)} instead.
     * </p>
     *
     * @param fnOK  the mapping function for {@link Err} values.
     * @param fnErr the mapping function for {@link OK} values.
     * @return the value produced from {@code fnOK} or {@code fnErr}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #recover(Function)
     * @see #forfeit(Function)
     */
    @NotNull <T> T fold(@NotNull Function<? super V, ? extends T> fnOK, @NotNull Function<? super E, ? extends T> fnErr);

    /**
     * Return a {@link Stream}, containing either a single {@link OK} value, or an empty {@link Stream}
     * if this is an {@link Err} value.
     *
     * @see #streamErr()
     */
    @NotNull Stream<V> stream();

    /**
     * Filter a {@link Try}.
     * <p>
     * If this {@link Try} is {@link Err}, return {@link Err} ({@code this}).
     * The {@code Predicate} is not tested, and the mapper {@code Function} is not executed.
     * </p>
     * <p>
     * If this {@link Try} is {@link OK}, return {@link OK} ({@code this}) if the {@code Predicate} matches.
     * If the {@code Predicate} fails to match, return an {@link Err} {@link Try} produced by applying the
     * mapping function to the current {@link Try} ({@code this}).
     * </p>
     *
     * @param predicate the predicate used to test {@link OK} values.
     * @param mapper    the mapping function for {@link OK} values that do not match the predicate.
     * @return a {@link Try} based on the algorithm described above.
     * @throws NullPointerException if the called mapping function returns {@code null}.
     */
    @NotNull Try<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper);

    /**
     * Executes the action iff this is an {@link OK} {@link Try}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #matchErr(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull Try<V, E> match(@NotNull Consumer<? super V> okConsumer);


    /**
     * Executes the given consumer if this is a {@link OK}. This is a terminal operation.
     *
     * @param okConsumer the consumer function to be executed
     */
    default void consume(@NotNull Consumer<? super V> okConsumer) {
        match(okConsumer);
    }

    /**
     * If this is an {@link OK}, return a new {@link OK} value produced by the given mapping function.
     * Otherwise, return the {@link Err} value.
     * <p>
     * The type of the produced {@link OK} can be different. The mapping function is only invoked for
     * {@link OK} values.
     * </p>
     * <p>
     * This is equivalent to {@code map( Function.identity(), rightMapper )}.
     * </p>
     *
     * @param okMapper the mapping function producing a new {@link OK} value.
     * @return a new {@link OK} produced by the mapping function if this is {@link OK};
     * otherwise, returns an {@link Err}.
     * @throws NullPointerException if the Try of the mapping function is {@code null}
     * @see #mapErr(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull <V2> Try<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper);

    /**
     * If this is an {@link OK}, return the new {@link Try} supplied by the mapping function.
     * Note that while the {@link Err} type must remain the same, the {@link OK} type returned
     * can be different.
     * <p>
     * This is also known as {@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link Err}, and the mapping function is not invoked.
     * </p>
     *
     * @param okMapper the mapping function that produces a new {@link Try}
     * @return a new {@link OK} produced by the mapping function if this is {@link OK};
     * otherwise, returns an {@link Err}.
     * @throws NullPointerException if the Try of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMapErr(Function)
     */
    @NotNull <V2> Try<V2, E> flatMap(@NotNull Function<? super V, ? extends Try<? extends V2, ? extends E>> okMapper);

    /**
     * Determines if this {@link OK} {@link Try} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link Err} {@link Try}
     * </p>
     *
     * @param okPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link OK} {@link Try} and the {@link Predicate} matches.
     * @see #contains(Object)
     * @see #ifPredicateErr(Predicate)
     * @see #containsErr(Exception)
     */
    boolean ifPredicate(@NotNull Predicate<V> okPredicate);

    /**
     * Determines if this {@link OK} {@link Try} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param okValue value to compare
     * @return {@code true} iff this is an {@link OK} {@link Try} and the contained value equals {@code okValue}
     * @see #ifPredicate(Predicate)
     * @see #containsErr(Exception)
     * @see #ifPredicateErr(Predicate)
     */
    boolean contains(@Nullable V okValue);

    /**
     * If this {@link Try} is {@link Err}, return {@code rightAlternate}.
     * Otherwise, return {@code this} (an {@link OK} {@link Try}).
     *
     * @param okAlternate alternate {@link OK} {@link Try}
     * @return {@code this}, or {@code okAlternate} if {@code this} is {@link Err}
     * @see #orElse(Supplier)
     * @see #orElseErr(Exception)
     * @see #orElseErr(Supplier)
     */
    @NotNull V orElse(@NotNull V okAlternate);

    /**
     * If this {@link Try} is {@link Err}, return the supplied {@link OK} {@link Try}.
     * Otherwise, return {@code this} (an {@link OK} {@link Try}) without
     * invoking the {@link Supplier}.
     *
     * @param okSupplier supplier of {@link OK} {@link Try}s
     * @return {@code this}, or the supplied {@link OK} {@link Try} if {@code this} is {@link Err}
     * @see #orElse(Object)
     * @see #orElseErr(Exception)
     * @see #orElseErr(Supplier)
     */
    @NotNull V orElse(@NotNull Supplier<? extends V> okSupplier);

    /**
     * Recover from an error; ignore the {@link Err} value if present,
     * and apply the mapping function to get an {@link OK}.
     * <p>
     * If this is an {@link OK}, return it without applying the mapping function.
     * </p>
     * <p>
     * This method is equivalent in alternative implementations to {@code orElseMap()}.
     * </p>
     *
     * @param fnE2V {@link Function} that produces an {@link OK} value.
     * @return A {@link OK} value, either the current {@link OK} if present, or the produced {@link OK} if not.
     * @throws NullPointerException if the Try of the mapping function is {@code null}.
     * @see #forfeit(Function)
     */
    @NotNull V recover(@NotNull Function<? super E, ? extends V> fnE2V);

    /**
     * Return a {@link Stream}, containing either a single {@link Err} value, or an empty {@link Stream}
     * if this is an {@link OK} value.
     *
     * @see #stream()
     */
    @NotNull Stream<E> streamErr();

    /**
     * Executes the action iff this is an {@link Err} {@link Try}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull Try<V, E> matchErr(@NotNull Consumer<? super E> errConsumer);

    /**
     * If this is an {@link Err}, return a new {@link Err} value produced by the given mapping function.
     * Otherwise, return the {@link OK} value.
     * <p>
     * The type of the produced {@link Err} can be different. The mapping function is only invoked for
     * {@link Err} values.
     * </p>
     * <p>
     * This is equivalent to {@code map( leftMapper, Function.identity() )}.
     * </p>
     *
     * @param errMapper the mapping function producing a new {@link Err} value.
     * @return a new {@link Err} produced by the mapping function if this is {@link Err};
     * otherwise, returns an {@link OK}.
     * @throws NullPointerException if the Try of the mapping function is {@code null}
     * @see #map(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull <E2 extends Exception> Try<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper);

    /**
     * If this is an {@link Err}, return the new {@link Try} supplied by the mapping function.
     * Note that while the {@link OK} type must remain the same, the {@link Err} type returned
     * can be different.
     * <p>
     * This is also known as a left-{@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link OK}, and the mapping function is not invoked.
     * </p>
     *
     * @param errMapper the mapping function that produces a new {@link Try}
     * @return a new {@link Err} produced by the mapping function if this is {@link Err};
     * otherwise, returns an {@link OK}.
     * @throws NullPointerException if the Try of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMap(Function)
     */
    @NotNull <E2 extends Exception> Try<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Try<? extends V, ? extends E2>> errMapper);

      /**
     * Determines if this {@link Err} {@link Try} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link OK} {@link Try}
     * </p>
     *
     * @param errPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link Err} {@link Try} and the {@link Predicate} matches.
     * @see #containsErr(Exception) (Object)
     * @see #contains(Object)
     * @see #ifPredicate(Predicate)
     */
    boolean ifPredicateErr(@NotNull Predicate<E> errPredicate);

    /**
     * Determines if this {@link Err} {@link Try} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param errValue value to compare
     * @return {@code true} iff this is an {@link Err} {@link Try} and the contained value equals {@code errValue}
     * @see #ifPredicateErr
     * @see #ifPredicate
     * @see #contains
     */
    boolean containsErr(@NotNull E errValue);

    /**
     * If this {@link Try} is an {@link OK}, return {@code errAlternate}.
     * Otherwise, return {@code this} (an {@link Err} {@link Try}).
     *
     * @param errAlternate alternate {@link Err} {@link Try}
     * @return {@code this}, or {@code leftAlternate} if {@code this} is an {@link OK}
     * @see #orElse(Object)
     * @see #orElse(Supplier)
     */
    @NotNull E orElseErr(@NotNull E errAlternate);

    /**
     * If this {@link Try} is an {@link OK}, return the supplied {@link Err}  {@link Try}.
     * Otherwise, return {@code this} (an {@link Err} {@link Try}) without
     * invoking the {@link Supplier}.
     *
     * @param errSupplier supplier of {@link Err} {@link Try}s
     * @return {@code this}, or the supplied {@link Err} {@link Try} if {@code this} is {@link OK}
     * @see #orElse(Object)
     * @see #orElseErr(Supplier)
     */
    @NotNull E orElseErr(@NotNull Supplier<? extends E> errSupplier);

    /**
     * Forfeit (ignore) the {@link OK} value if present, and apply the mapping function to get an {@link Err}.
     * <p>
     * If this is an {@link Err}, return it without applying the mapping function.
     * </p>
     * <p>
     * This method is equivalent in alternative implementations to {@code orElseMapErr()}.
     * </p>
     *
     * @param fnV2E {@link Function} that produces an {@link Err} value.
     * @return A {@link Err} value, either the current {@link Err} if present, or the produced {@link Err} if not.
     * @throws NullPointerException if the Try of the mapping function is {@code null}.
     * @see #recover(Function)
     */
    @NotNull E forfeit(@NotNull Function<? super V, ? extends E> fnV2E);

    /**
     * If {@code this} is {@link Err}, return it. Otherwise, return the next {@link Try} given.
     * The next {@link Try} can have a different parameterized {@link OK} type.
     *
     * @param nextTry The {@link Try} to return.
     * @see #and(Supplier)
     * @see #or(Try)
     * @see #or(Supplier)
     */
    @NotNull <V2> Try<V2, E> and(@NotNull Try<V2, E> nextTry);

    /**
     * If {@code this} is {@link Err}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Try} supplied.
     * The next {@link Try} can have a different parameterized {@link OK} type.
     *
     * @param nextTrySupplier The supplier of a {@link Try} to return; only called if {@code this} is {@link OK}.
     * @throws NullPointerException if the supplied {@link Try} is {@code null}.
     * @see #and(Try)
     * @see #or(Try)
     * @see #or(Supplier)
     */
    @NotNull <V2> Try<V2, E> and(@NotNull Supplier<Try<V2, E>> nextTrySupplier);

    /**
     * If {@code this} is {@link OK}, return it.
     * Otherwise, return the next {@link Try} given.
     * The next {@link Try}  can have a different parameterized {@link Err} type.
     *
     * @param nextTry The {@link Try} to return.
     * @see #or(Supplier)
     * @see #and(Try)
     * @see #and(Supplier)
     */
    @NotNull <E2 extends Exception> Try<V, E2> or(@NotNull Try<V, E2> nextTry);

    /**
     * If {@code this} is {@link OK}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Try} supplied.
     * The next {@link Try} can have a different parameterized {@link Err} type.
     *
     * @param nextTrySupplier The supplier of a {@link Try} to return; only called if {@code this} is {@link Err}.
     * @throws NullPointerException if the supplier is called and returns {@code null}.
     * @see #or(Try)
     * @see #and(Try)
     * @see #and(Supplier)
     */
    @NotNull <E2 extends Exception> Try<V, E2> or(@NotNull Supplier<Try<V, E2>> nextTrySupplier);

    /**
     * Expect success (an {@link OK} value), otherwise throw a <strong>runtime</strong> Exception.
     * <p>
     * This always will throw for {@link Err}. If {@link Err} is a checked exception, this will
     * throw a checked Exception.
     * <p>
     * This is similar to {@code getOrThrow()}; a value is expected or an exception is thrown.
     *
     * @return Value (if {@link OK}))
     * @throws E ({@link Exception} held by {@link Err}).
     * @see #getOrThrow(Function)
     */
    @NotNull V expect() throws E;

    /**
     * Throw the given supplied {@link Exception} (or more precisely, {@link Throwable}).
     * <p>
     * This method can wrap an {@link Err} type if that {@link Err} type is allowed by the constructor.
     * This can also convert Exceptions; e.g., using an {@link java.io.UncheckedIOException} to wrap
     * an {@link java.io.IOException} via {@code orThrow(UncheckedIOException::new)}.
     * </p>
     *
     * @param exFn Exception producing function
     * @param <X>  Throwable (Exception) created by {@code exFn}
     * @return Value V
     * @throws X Throwable
     * @see #expect()
     */
    @NotNull <X extends Exception> V getOrThrow(@NotNull Function<E, X> exFn) throws X;

    /**
     * Represents a successful result with a value of type V.
     * Implements the Try interface.
     *
     * @param <V> the type of the value in the result
     * @param <E> the type of the exception that may occur
     */
    record OK<V, E extends Exception>(V value) implements Try<V, E> {
        /**
         * Checks if the given value is non-null.
         *
         * @param value the value to be checked
         * @throws NullPointerException if the value is null
         */
        public OK {
            requireNonNull( value, "OK: cannot be null!" );
        }

        /**
         * Get the OK value V
         *
         * @return V
         */
        public @NotNull V get() {
            return value;
        }


        @Override
        public @NotNull Optional<V> ok() {
            return Optional.of( value );
        }

        @Override
        public @NotNull Optional<E> err() {
            return Optional.empty();
        }

        @Override
        public @NotNull Try<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer) {
            requireNonNull( okConsumer );
            requireNonNull( errConsumer );
            okConsumer.accept( value );
            return this;
        }

        @Override
        public @NotNull <V2, E2 extends Exception> Try<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper, @NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return new OK<>( okMapper.apply( value ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <V2, E2 extends Exception> Try<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Try<? extends V2, ? extends E2>> okMapper, @NotNull Function<? super E, ? extends Try<? extends V2, ? extends E2>> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return (Try<V2, E2>)  requireNonNull( okMapper.apply( value ));
        }

        @Override
        public <T> @NotNull T fold(@NotNull Function<? super V, ? extends T> fnOK, @NotNull Function<? super E, ? extends T> fnErr) {
            requireNonNull( fnOK );
            requireNonNull( fnErr );
            return requireNonNull( fnOK.apply( value ) );
        }

        @Override
        public @NotNull Stream<V> stream() {
            return Stream.of( value );
        }


        @Override
        public @NotNull Try<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper) {
            requireNonNull( predicate );
            requireNonNull( mapper );
            if (predicate.test( value )) {
                return this;
            }
            return new Err<>( mapper.apply( value ) );
        }

        @Override
        public @NotNull Try<V, E> match(@NotNull Consumer<? super V> okConsumer) {
            requireNonNull( okConsumer );
            okConsumer.accept( value );
            return this;
        }

        @Override
        public @NotNull <V2> Try<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
            requireNonNull( okMapper );
            return new OK<>( okMapper.apply( value ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <V2> Try<V2, E> flatMap(@NotNull Function<? super V, ? extends Try<? extends V2, ? extends E>> okMapper) {
            requireNonNull( okMapper );
            return (Try<V2, E>) requireNonNull( okMapper.apply( value ) );
        }

        @Override
        public boolean ifPredicate(@NotNull Predicate<V> okPredicate) {
            requireNonNull( okPredicate );
            return okPredicate.test( value );
        }

        @Override
        public boolean contains(@Nullable V okValue) {
            return Objects.equals( value, okValue );
        }


        @Override
        public @NotNull V orElse(@NotNull V okAlternate) {
            requireNonNull( okAlternate );
            return value;
        }

        @Override
        public @NotNull V orElse(@NotNull Supplier<? extends V> okSupplier) {
            requireNonNull( okSupplier );
            return value;
        }

        @Override
        public @NotNull V recover(@NotNull Function<? super E, ? extends V> fnE2V) {
            requireNonNull( fnE2V );
            return value;
        }

        @Override
        public @NotNull Stream<E> streamErr() {
            return Stream.empty();
        }


        @Override
        public @NotNull Try<V, E> matchErr(@NotNull Consumer<? super E> errConsumer) {
            requireNonNull( errConsumer );
            return this;
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( errMapper );
            return coerce();
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Try<? extends V, ? extends E2>> errMapper) {
            requireNonNull( errMapper );
            return coerce();
        }

        @Override
        public boolean ifPredicateErr(@NotNull Predicate<E> errPredicate) {
            requireNonNull( errPredicate );
            return false;
        }

        @Override
        public boolean containsErr(@NotNull E errValue) {
            requireNonNull( errValue );
            return false;
        }

        @Override
        public @NotNull E orElseErr(@NotNull E errAlternate) {
            requireNonNull( errAlternate );
            return errAlternate;
        }

        @Override
        public @NotNull E orElseErr(@NotNull Supplier<? extends E> errSupplier) {
            requireNonNull( errSupplier );
            return requireNonNull( errSupplier.get() );
        }

        @Override
        public @NotNull E forfeit(@NotNull Function<? super V, ? extends E> fnV2E) {
            requireNonNull( fnV2E );
            return requireNonNull( fnV2E.apply( value ) );
        }

        @Override
        public @NotNull <V2> Try<V2, E> and(@NotNull Try<V2, E> nextTry) {
            requireNonNull( nextTry );
            return nextTry;
        }

        @Override
        public @NotNull <V2> Try<V2, E> and(@NotNull Supplier<Try<V2, E>> nextTrySupplier) {
            requireNonNull( nextTrySupplier );
            return requireNonNull( nextTrySupplier.get() );
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> or(@NotNull Try<V, E2> nextTry) {
            requireNonNull( nextTry );
            return coerce();
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> or(@NotNull Supplier<Try<V, E2>> nextTrySupplier) {
            requireNonNull( nextTrySupplier );
            return coerce();
        }

        @Override
        public @NotNull V expect() throws E {
            return value;
        }

        @Override
        public <X extends Exception> @NotNull V getOrThrow(@NotNull Function<E, X> exFn) throws X {
            return value;
        }

        // coerce empty exception to new type
        @SuppressWarnings("unchecked")
        private <E2 extends Exception> OK<V, E2> coerce() {
            return (OK<V, E2>) this;
        }

    }


    /**
     * The Err class represents the error result of a Try.
     * It holds an exception object.
     *
     * @param <V> the type of the success value
     * @param <E> the type of the exception
     */
    record Err<V, E extends Exception>(E error) implements Try<V, E> {
        /**
         * Checks if the specified error is not null.
         *
         * @param error the error object to check
         * @throws NullPointerException if the error is null
         */
        public Err {
            requireNonNull( error, "Err: cannot be null!" );
        }

        /**
         * Get the Err value E
         *
         * @return E
         */
        public @NotNull E get() {
            return error;
        }


        @Override
        public @NotNull Optional<V> ok() {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<E> err() {
            return Optional.of( error );
        }


        @Override
        public @NotNull Try<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer) {
            requireNonNull( okConsumer );
            requireNonNull( errConsumer );
            errConsumer.accept( error );
            return this;
        }

        @Override
        public @NotNull <V2, E2 extends Exception> Try<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper, @NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return new Err<>( errMapper.apply( error ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <V2, E2 extends Exception> Try<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Try<? extends V2, ? extends E2>> okMapper, @NotNull Function<? super E, ? extends Try<? extends V2, ? extends E2>> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return (Try<V2, E2>) requireNonNull( errMapper.apply( error ) );
        }

        @Override
        public <T> @NotNull T fold(@NotNull Function<? super V, ? extends T> fnOK, @NotNull Function<? super E, ? extends T> fnErr) {
            requireNonNull( fnOK );
            requireNonNull( fnErr );
            return requireNonNull( fnErr.apply( error ) );
        }

        @Override
        public @NotNull Stream<V> stream() {
            return Stream.empty();
        }

        @Override
        public @NotNull Try<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper) {
            requireNonNull( predicate );
            requireNonNull( mapper );
            return this;
        }

        @Override
        public @NotNull Try<V, E> match(@NotNull Consumer<? super V> okConsumer) {
            requireNonNull( okConsumer );
            return this;
        }

        @Override
        public @NotNull <V2> Try<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
            requireNonNull( okMapper );
            return coerce();
        }

        @Override
        public @NotNull <V2> Try<V2, E> flatMap(@NotNull Function<? super V, ? extends Try<? extends V2, ? extends E>> okMapper) {
            requireNonNull( okMapper );
            return coerce();
        }

        @Override
        public boolean ifPredicate(@NotNull Predicate<V> okPredicate) {
            requireNonNull( okPredicate );
            return false;
        }

        @Override
        public boolean contains(@Nullable V okValue) {
            requireNonNull( okValue );
            return false;
        }

        @Override
        public @NotNull V orElse(@NotNull V okAlternate) {
            requireNonNull( okAlternate );
            return okAlternate;
        }

        @Override
        public @NotNull V orElse(@NotNull Supplier<? extends V> okSupplier) {
            requireNonNull( okSupplier );
            return requireNonNull( okSupplier.get() );
        }

        @Override
        public @NotNull V recover(@NotNull Function<? super E, ? extends V> fnE2V) {
            requireNonNull( fnE2V );
            return requireNonNull( fnE2V.apply( error ) );
        }

        @Override
        public @NotNull Stream<E> streamErr() {
            return Stream.of( error );
        }

        @Override
        public @NotNull Try<V, E> matchErr(@NotNull Consumer<? super E> errConsumer) {
            requireNonNull( errConsumer );
            errConsumer.accept( error );
            return this;
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( errMapper );
            return new Err<>( errMapper.apply( error ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Try<? extends V, ? extends E2>> errMapper) {
            requireNonNull( errMapper );
            return (Try<V, E2>) requireNonNull( errMapper.apply( error ) );
        }

        @Override
        public boolean ifPredicateErr(@NotNull Predicate<E> errPredicate) {
            requireNonNull( errPredicate );
            return errPredicate.test( error );
        }

        @Override
        public boolean containsErr(@Nullable E errValue) {
            requireNonNull( errValue );
            return Objects.equals( error, errValue );
        }

        @Override
        public @NotNull E orElseErr(@NotNull E errAlternate) {
            requireNonNull( errAlternate );
            return error;
        }

        @Override
        public @NotNull E orElseErr(@NotNull Supplier<? extends E> errSupplier) {
            requireNonNull( errSupplier );
            return error;
        }

        @Override
        public @NotNull E forfeit(@NotNull Function<? super V, ? extends E> fnV2E) {
            requireNonNull( fnV2E );
            return error;
        }

        @Override
        public @NotNull <V2> Try<V2, E> and(@NotNull Try<V2, E> nextResult) {
            requireNonNull( nextResult );
            return coerce();
        }

        @Override
        public @NotNull <V2> Try<V2, E> and(@NotNull Supplier<Try<V2, E>> nextResultSupplier) {
            requireNonNull( nextResultSupplier );
            return coerce();
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> or(@NotNull Try<V, E2> nextResult) {
            requireNonNull( nextResult );
            return nextResult;
        }

        @Override
        public @NotNull <E2 extends Exception> Try<V, E2> or(@NotNull Supplier<Try<V, E2>> nextResultSupplier) {
            requireNonNull( nextResultSupplier );
            return requireNonNull( nextResultSupplier.get() );
        }


        @Override
        public @NotNull V expect() throws E {
            throw error;
        }

        @Override
        public <X extends Exception> @NotNull V getOrThrow(@NotNull Function<E, X> exFn) throws X {
            requireNonNull( exFn );
            throw requireNonNull( exFn.apply( error ) );
        }

        // coerce empty value to new type
        @SuppressWarnings("unchecked")
        private <V2> Err<V2, E> coerce() {
            return (Err<V2, E>) this;
        }
    }


}
