package net.xyzsd.dichotomy.trying;


import net.xyzsd.dichotomy.None;
import net.xyzsd.dichotomy.trying.function.ExFunction;
import net.xyzsd.dichotomy.trying.function.ExSupplier;
import net.xyzsd.dichotomy.trying.function.SpecExSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/*
    todo: need common 'none' class with Result, and ofOK() without argument to create a null

    Try2 as exception
    NOTE: cannot have a swap() in this case

    TODO: ... consider renaming
        ? XResult? ExResult ? ResultX

 */
public sealed interface Try2<V, E extends Exception> {



    record OK<V, E extends Exception>(V value) implements Try2<V, E> {
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

        // coerce empty exception to new type
        @SuppressWarnings("unchecked")
        private <V, E2 extends Exception> OK<V, E2> coerceErr() {
            return (OK<V, E2>) this;
        }

    }

    record Err<V, E extends Exception>(E error) implements Try2<V, E> {
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

        // coerce empty OK value to new type
        @SuppressWarnings("unchecked")
        private <V2> Err<V2, E> coerceOK() {
            return (Err<V2, E>) this;
        }

    }


    /**
     * Create an {@link OK} (Success) {@link Try2} for the given non-null value.
     *
     * @param value value for success
     * @param <V>   Value type
     * @param <E>   Error Exception type
     * @return {@link OK}  {@link Try2} containing the given value.
     */
    @NotNull
    static <V, E extends Exception> Try2<V, E> ofOK(@NotNull V value) {
        return new OK<>( value );
    }

    /**
     * Create an empty {@link OK} (Success) {@link Try2}.
     * <p>
     * All empty values use the {@link None} type.
     * </p>
     *
     * @param <E> Error value
     * @return {@link OK} {@link Try2} containing the given value.
     */
    @NotNull
    static <E extends Exception> Try2<None, E> ofOK() {
        return new OK<None,E>( new None() );
    }

    /**
     * Create an {@link Err} (Failure) {@link Try2} for the given non-null value.
     *
     * @param error Error
     * @param <V> Value type
     * @param <E> Error Exception type
     * @return {@link Err} containing the given error Exception.
     */
    @NotNull
    static <V, E extends Exception> Try2<V, E> ofErr(@NotNull E error) {
        return new Err<>(  error );
    }



    // TODO: document and test all this


    @NotNull
    static <V,X extends Exception> Try2<V, X> of(@NotNull Supplier<V> supplier, @NotNull Class<X> exClass) {
        requireNonNull( supplier );

        try {
            return new OK<>( supplier.get() );
        } catch (Throwable t) {
            if (exClass.isInstance( t )) {
                return new Err<V,X>(exClass.cast( t ) );
            }
            throw t;
        }
    }


    // ANY runtime exception. standard Suppliers Can't throw checked exceptions (no throws clause defined), so, not included
    @NotNull
    static <V> Try2<V, RuntimeException> of(@NotNull Supplier<V> supplier) {
        requireNonNull( supplier );

        try {
            return new OK<>( supplier.get() );
        } catch (RuntimeException ex) {
            return new Err<>( ex );
        }
    }





    /**
     * Get a {@link Try2} from an {@link ExSupplier}.
     * <p>
     * The {@link ExSupplier} may throw a checked or unchecked {@link Exception}.
     * The result of the function, or {@link Exception} will be contained in the {@link Try2}.
     * </p>
     *
     * @param exSupplier Supplier to invoke
     * @param <V>        type of result supplied
     * @return A {@link Try2} containing the supplied value or an {@link Exception}
     * @see #from(ExSupplier)
     * @see #of(Supplier)
     * @see #from(Supplier)
     */
    @NotNull
    public static <V> Try2<V, Exception> of(@NotNull ExSupplier<V> exSupplier) {
        requireNonNull( exSupplier );

        try {
            return new Try2.OK<>( exSupplier.get() );
        } catch (Exception ex) {
            return new Try2.Err<>( ex );
        }
    }

    /**
     * Converts an {@link Supplier} which may throw {@link RuntimeException}s to a {@link Supplier} of {@link Try2}s.
     *
     * @param supplier Supplier to invoke
     * @param <V>      type of result supplied
     * @return A {@link Try2} containing the supplied value or a {@link RuntimeException}
     * @see #of(Supplier)
     * @see #of(ExSupplier)
     * @see #from(ExSupplier)
     */
    @NotNull
    public static <V> Supplier<Try2<V, RuntimeException>> from(@NotNull Supplier<V> supplier) {
        requireNonNull( supplier );
        return () -> of( supplier );
    }


    /**
     * Converts an {@link ExSupplier} to a {@link Supplier} of {@link Try2}s.
     * <p>
     * The {@link ExSupplier} may throw a checked or unchecked {@link Exception},
     * which will be wrapped in an {@link Err} {@link Try2}.
     * </p>
     *
     * @param exSupplier Supplier to invoke
     * @param <V>        type of result supplied
     * @return A {@link Try2} containing the supplied value or an {@link Exception}
     */
    @NotNull
    public static <V> Supplier<Try2<V, Exception>> from(@NotNull ExSupplier<V> exSupplier) {
        requireNonNull( exSupplier );
        return () -> of( exSupplier );
    }


    /**
     * Converts a {@link SpecExSupplier} to a {@link Supplier} of {@link Try2}s.
     * <p>
     * The returned {@link Supplier} will only wrap {@link Exception}s of the given class
     * parameter {@code <X>} (and subclasses); all other {@link RuntimeException}s will be thrown.
     * </p>
     *
     * @param exSupplier {@link SpecExSupplier} to wrap
     * @param <V>        value wrapped by the given {@link Try2}.
     * @param <X>        {@link RuntimeException} wrapped by the given {@link Try2}.
     * @return A {@link Supplier} of {@link Try2}s containing the supplied value or the specified
     * {@link RuntimeException}{@code <X>}
     * @see #of(SpecExSupplier, Class)
     * @see #from(ExSupplier)
     * @see #from(Supplier)
     */
    @NotNull
    public static <V, X extends RuntimeException> Supplier<Try2<V, X>> from(@NotNull SpecExSupplier<V, X> exSupplier, final Class<X> exClass) {
        requireNonNull( exSupplier );
        return () -> of( exSupplier, exClass );
    }


    /**
     * Execute a {@link SpecExSupplier} to a supply a {@link Try2}.
     * <p>
     * The returned {@link Try2} will contain the supplied value or the {@link RuntimeException} of the given class
     * parameter {@code <X>} (and subclasses); all other {@link RuntimeException}s will be thrown.
     * </p>
     *
     * @param exSupplier {@link SpecExSupplier} to wrap
     * @param <V>        supplied value wrapped by a {@link Try2}.
     * @param <X>        {@link RuntimeException} type wrapped by a {@link Try2}.
     * @return A {@link Try2} containing the supplied value or the specified {@link RuntimeException}
     * @see #from(SpecExSupplier, Class)
     */
    @NotNull
    public static <V, X extends RuntimeException> Try2<V, X> of(@NotNull SpecExSupplier<V, X> exSupplier, final Class<X> exClass) {
        requireNonNull( exSupplier );
        requireNonNull( exClass );

        try {
            return new Try2.OK<>( exSupplier.get() );
        } catch (RuntimeException ex) {
            if (exClass.isAssignableFrom( ex.getClass() )) {
                return new Try2.Err<>( exClass.cast( ex ) );
            }
            throw ex;
        }
    }


    /**
     * Apply the given {@link ExFunction}, returning a {@link Try2} which wraps either the return value
     * of the {@link ExFunction} or an {@link Exception}.
     *
     * @param in value applied
     * @param exFn function, which may throw an exception
     * @return A {@link Try2} wrapping either the output value or an {@link Exception}.
     * @param <T> function input
     * @param <R> function output
     *
     * @see #from(ExFunction)
     */
    @NotNull
    public static <T, R> Try2<R, Exception> of(@Nullable T in, @NotNull final ExFunction<T, R> exFn) {
        requireNonNull( exFn );
        return fnToResult( in, exFn );
    }

    /**
     * Given an {@link Exception}-producing function ({@link ExFunction}), returns a {@link Function} which
     * returns a {@link Try2} wrapping the function output or {@link Exception}. This is lazy; the function
     * is not evaluated.
     *
     * @param exFn {@link ExFunction} which may generate an exception
     * @return {@link Function} returning {@link Try2}s which wrap the output or {@link Exception}
     * @param <T> function input
     * @param <R> function output
     *
     * @see #of(Object, ExFunction)
     */
    @NotNull
    public static <T, R> Function<T, Try2<R, Exception>> from(@NotNull final ExFunction<T, R> exFn) {
        requireNonNull( exFn );
        return (in) -> fnToResult( in, exFn );
    }



    private static <T, R> Try2<R, Exception> fnToResult(@Nullable T in, @NotNull final ExFunction<T, R> exFn) {
        try {
            return new Try2.OK<>( exFn.apply( in ) );
        } catch (Exception ex) {
            return new Try2.Err<>( ex );
        }
    }













    /**
     * Executes the action for the {@link OK} or {@link Err} depending upon
     * the value of this {@link Try2}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #matchErr(Consumer)
     */
    @NotNull
    default Try2<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer) {
        requireNonNull( okConsumer );
        requireNonNull( errConsumer );

        switch (this) {
            case OK(V v) -> okConsumer.accept( v );
            case Err(E e) -> errConsumer.accept( e );
        }

        return this;
    }


    /**
     * Returns a new {@link Try2}, the value of which is determined by the appropriate mapping function.
     * <p>
     * The returned {@link Try2} (which may be {@link OK} or {@link Err}) can have different types.
     * </p>
     *
     * @param okMapper  the mapping function for {@link OK} values.
     * @param errMapper the mapping function for {@link Err} values.
     * @return the {@link Try2} produced from {@code okMapper} or {@code errMapper}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapErr(Function)
     */
    @NotNull
    default <V2, E2 extends Exception> Try2<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper,
                                                          @NotNull Function<? super E, ? extends E2> errMapper) {
        requireNonNull( okMapper );
        requireNonNull( errMapper );

        return switch (this) {
            case OK(V v) -> new OK<>( okMapper.apply( v ) );
            case Err(E e) -> new Err<>( errMapper.apply( e ) );
        };
    }


    /**
     * Returns a {@link Try2}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced {@link Try2} (which may be {@link Err} or {@link OK}) can have different types.
     * </p>
     *
     * @param fnOK  the mapping function for {@link OK} values.
     * @param fnErr the mapping function for {@link Err} values.
     * @return the {@link Try2} produced from {@code fnOK} or {@code fnErr}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapErr(Function) (Function)
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default <V2, E2 extends Exception> Try2<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Try2<? extends V2, ? extends E2>> fnOK,
                                                              @NotNull Function<? super E, ? extends Try2<? extends V2, ? extends E2>> fnErr) {
        requireNonNull( fnOK );
        requireNonNull( fnErr );

        return (Try2<V2, E2>) switch (this) {
            case OK(V v) -> requireNonNull( fnOK.apply( v ) );
            case Err(E e) -> requireNonNull( fnErr.apply( e ) );
        };
    }


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
    @NotNull
    default <T> T fold(@NotNull Function<? super V, ? extends T> fnOK, @NotNull Function<? super E, ? extends T> fnErr) {
        requireNonNull( fnOK );
        requireNonNull( fnErr );

        return switch (this) {
            case OK(V v) -> requireNonNull( fnOK.apply( v ) );
            case Err(E e) -> requireNonNull( fnErr.apply( e ) );
        };
    }


    /**
     * Return a {@link Stream}, containing either a single {@link OK} value, or an empty {@link Stream}
     * if this is an {@link Err} value.
     *
     * @see #streamErr()
     */
    @NotNull
    default Stream<V> stream() {
        return switch (this) {
            case OK(V v) -> Stream.of( v );
            case Err(E e) -> Stream.empty();
        };
    }

    /**
     * Filter a {@link Try2}.
     * <p>
     * If this {@link Try2} is {@link Err}, return {@link Err} ({@code this}).
     * The {@code Predicate} is not tested, and the mapper {@code Function} is not executed.
     * </p>
     * <p>
     * If this {@link Try2} is {@link OK}, return {@link OK} ({@code this}) if the {@code Predicate} matches.
     * If the {@code Predicate} fails to match, return an {@link Err} {@link Try2} produced by applying the
     * mapping function to the current {@link Try2} ({@code this}).
     * </p>
     *
     * @param predicate the predicate used to test {@link OK} values.
     * @param mapper    the mapping function for {@link OK} values that do not match the predicate.
     * @return a {@link Try2} based on the algorithm described above.
     * @throws NullPointerException if the called mapping function returns {@code null}.
     */
    @NotNull
    default Try2<V, E> filter(@NotNull Predicate<? super V> predicate,
                              @NotNull Function<? super V, ? extends E> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );
        return switch (this) {
            case OK<V,E> ok -> {
                if(predicate.test( ok.get() )) {
                    yield ok;
                } else {
                    yield new Err<>( mapper.apply( ok.get() ) );
                }
            }
            case Err<V,E> __ -> this;
        };
    }


    /**
     * Executes the action iff this is an {@link OK} {@link Try2}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #matchErr(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull
    default Try2<V, E> match(@NotNull Consumer<? super V> okConsumer) {
        requireNonNull( okConsumer );
        if (this instanceof OK(V v)) {
            okConsumer.accept( v );
        }
        return this;
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
     * @throws NullPointerException if the Try2 of the mapping function is {@code null}
     * @see #mapErr(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull
    default <V2> Try2<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
        requireNonNull( okMapper );

        return switch (this) {
            case OK(V v) -> new OK<>( okMapper.apply( v ) );
            case Err<V, E> err -> err.coerceOK();
        };
    }

    /**
     * If this is an {@link OK}, return the new {@link Try2} supplied by the mapping function.
     * Note that while the {@link Err} type must remain the same, the {@link OK} type returned
     * can be different.
     * <p>
     * This is also known as {@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link Err}, and the mapping function is not invoked.
     * </p>
     *
     * @param okMapper the mapping function that produces a new {@link Try2}
     * @return a new {@link OK} produced by the mapping function if this is {@link OK};
     * otherwise, returns an {@link Err}.
     * @throws NullPointerException if the Try2 of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMapErr(Function)
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default <V2> Try2<V2, E> flatMap(@NotNull Function<? super V, ? extends Try2<? extends V2, ? extends E>> okMapper) {
        requireNonNull( okMapper );

        return (Try2<V2, E>) switch (this) {
            case OK(V v) -> requireNonNull( okMapper.apply( v ) );
            case Err<V, E> err -> err.coerceOK();
        };
    }

    /**
     * Determines if this {@link OK} {@link Try2} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link Err} {@link Try2}
     * </p>
     *
     * @param okPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link OK} {@link Try2} and the {@link Predicate} matches.
     * @see #contains(Object)
     * @see #matchesErr(Predicate)
     * @see #containsErr(Exception)
     */
    default boolean matches(@NotNull Predicate<V> okPredicate) {
        requireNonNull( okPredicate );
        return switch (this) {
            case OK(V v) -> okPredicate.test( v );
            case Err<V, E> __ -> false;
        };
    }

    /**
     * Determines if this {@link OK} {@link Try2} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param okValue value to compare
     * @return {@code true} iff this is an {@link OK} {@link Try2} and the contained value equals {@code okValue}
     * @see #matches(Predicate)
     * @see #containsErr(Exception)
     * @see #matchesErr(Predicate)
     */
    default boolean contains(@Nullable V okValue) {
        requireNonNull( okValue );
        return switch (this) {
            case OK(V v) -> Objects.equals( v, okValue );
            case Err<V, E> __ -> false;
        };
    }

    /**
     * If this {@link Try2} is {@link Err}, return {@code rightAlternate}.
     * Otherwise, return {@code this} (an {@link OK} {@link Try2}).
     *
     * @param okAlternate alternate {@link OK} {@link Try2}
     * @return {@code this}, or {@code okAlternate} if {@code this} is {@link Err}
     * @see #orElseGet(Supplier)
     * @see #orElseErr(Exception)
     * @see #orElseGetErr(Supplier)
     */
    @NotNull
    default V orElse(@NotNull V okAlternate) {
        requireNonNull( okAlternate );
        return switch (this) {
            case OK(V v) -> v;
            case Err<V, E> __ -> okAlternate;
        };
    }

    /**
     * If this {@link Try2} is {@link Err}, return the supplied {@link OK} {@link Try2}.
     * Otherwise, return {@code this} (an {@link OK} {@link Try2}) without
     * invoking the {@link Supplier}.
     *
     * @param okSupplier supplier of {@link OK} {@link Try2}s
     * @return {@code this}, or the supplied {@link OK} {@link Try2} if {@code this} is {@link Err}
     * @see #orElse(Object)
     * @see #orElseErr(Exception)
     * @see #orElseGetErr(Supplier)
     */
    @NotNull
    default V orElseGet(@NotNull Supplier<? extends V> okSupplier) {
        requireNonNull( okSupplier );
        return switch (this) {
            case OK(V v) -> v;
            case Err<V, E> __ -> requireNonNull( okSupplier.get() );
        };
    }

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
     * @throws NullPointerException if the Try2 of the mapping function is {@code null}.
     * @see #forfeit(Function)
     */
    @NotNull
    default V recover(@NotNull Function<? super E, ? extends V> fnE2V) {
        requireNonNull( fnE2V );

        return switch (this) {
            case OK(V v) -> v;
            case Err(E e) -> requireNonNull( fnE2V.apply( e ) );
        };
    }


    /**
     * Return a {@link Stream}, containing either a single {@link Err} value, or an empty {@link Stream}
     * if this is an {@link OK} value.
     *
     * @see #stream()
     */
    @NotNull
    default Stream<E> streamErr() {
        return switch (this) {
            case OK(V v) -> Stream.empty();
            case Err(E e) -> Stream.of( e );
        };
    }

    /**
     * Executes the action iff this is an {@link Err} {@link Try2}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull
    default Try2<V, E> matchErr(@NotNull Consumer<? super E> errConsumer) {
        requireNonNull( errConsumer );
        if (this instanceof Err(E e)) {
            errConsumer.accept( e );
        }
        return this;
    }


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
     * @throws NullPointerException if the Try2 of the mapping function is {@code null}
     * @see #map(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull
    default <E2 extends Exception> Try2<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
        requireNonNull( errMapper );
        return switch (this) {
            case OK<V, E> ok -> ok.coerceErr();
            case Err(E e) -> new Err<>( errMapper.apply( e ) );
        };
    }

    /**
     * If this is an {@link Err}, return the new {@link Try2} supplied by the mapping function.
     * Note that while the {@link OK} type must remain the same, the {@link Err} type returned
     * can be different.
     * <p>
     * This is also known as a left-{@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link OK}, and the mapping function is not invoked.
     * </p>
     *
     * @param errMapper the mapping function that produces a new {@link Try2}
     * @return a new {@link Err} produced by the mapping function if this is {@link Err};
     * otherwise, returns an {@link OK}.
     * @throws NullPointerException if the Try2 of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMap(Function)
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default <E2 extends Exception> Try2<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Try2<? extends V, ? extends E2>> errMapper) {
        requireNonNull( errMapper );
        return switch (this) {
            case OK<V, E> ok -> ok.coerceErr();
            case Err(E e) -> (Try2<V, E2>) requireNonNull( errMapper.apply( e ) );
        };
    }


    /**
     * Determines if this {@link Err} {@link Try2} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link OK} {@link Try2}
     * </p>
     *
     * @param errPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link Err} {@link Try2} and the {@link Predicate} matches.
     * @see #containsErr(Exception) (Object)
     * @see #contains(Object)
     * @see #matches(Predicate)
     */
    default boolean matchesErr(@NotNull Predicate<E> errPredicate) {
        requireNonNull( errPredicate );
        return switch (this) {
            case OK<V, E> __ -> false;
            case Err(E e) -> errPredicate.test( e );
        };
    }

    /**
     * Determines if this {@link Err} {@link Try2} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param errValue value to compare
     * @return {@code true} iff this is an {@link Err} {@link Try2} and the contained value equals {@code errValue}
     * @see #matchesErr
     * @see #matches
     * @see #contains
     */
    default boolean containsErr(@NotNull E errValue) {
        requireNonNull( errValue );
        return switch (this) {
            case OK<V, E> __ -> false;
            case Err(E e) -> Objects.equals( e, errValue );
        };
    }

    /**
     * If this {@link Try2} is an {@link OK}, return {@code errAlternate}.
     * Otherwise, return {@code this} (an {@link Err} {@link Try2}).
     *
     * @param errAlternate alternate {@link Err} {@link Try2}
     * @return {@code this}, or {@code leftAlternate} if {@code this} is an {@link OK}
     * @see #orElse(Object)
     * @see #orElseGet(Supplier)
     */
    @NotNull
    default E orElseErr(@NotNull E errAlternate) {
        requireNonNull( errAlternate );
        return switch (this) {
            case OK<V, E> __ -> errAlternate;
            case Err(E e) -> e;
        };
    }

    /**
     * If this {@link Try2} is an {@link OK}, return the supplied {@link Err}  {@link Try2}.
     * Otherwise, return {@code this} (an {@link Err} {@link Try2}) without
     * invoking the {@link Supplier}.
     *
     * @param errSupplier supplier of {@link Err} {@link Try2}s
     * @return {@code this}, or the supplied {@link Err} {@link Try2} if {@code this} is {@link OK}
     * @see #orElse(Object)
     * @see #orElseGetErr(Supplier)
     */
    @NotNull
    default E orElseGetErr(@NotNull Supplier<? extends E> errSupplier) {
        requireNonNull( errSupplier );
        return switch (this) {
            case OK<V, E> __ -> requireNonNull( errSupplier.get() );
            case Err(E e) -> e;
        };
    }

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
     * @throws NullPointerException if the Try2 of the mapping function is {@code null}.
     * @see #recover(Function)
     */
    @NotNull
    default E forfeit(@NotNull Function<? super V, ? extends E> fnV2E) {
        requireNonNull( fnV2E );
        return switch (this) {
            case OK(V v) -> requireNonNull( fnV2E.apply( v ) );
            case Err(E e) -> e;
        };
    }


    /**
     * If {@code this} is {@link Err}, return it. Otherwise, return the next {@link Try2} given.
     * The next {@link Try2} can have a different parameterized {@link OK} type.
     *
     * @param nextTry2 The {@link Try2} to return.
     * @see #and(Supplier)
     * @see #or(Try2)
     * @see #or(Supplier)
     */
    @NotNull
    default <V2> Try2<V2, E> and(@NotNull Try2<V2, E> nextTry2) {
        requireNonNull( nextTry2 );
        return switch (this) {
            case OK<V, E> __ -> nextTry2;
            case Err<V, E> err -> err.coerceOK();
        };
    }

    /**
     * If {@code this} is {@link Err}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Try2} supplied.
     * The next {@link Try2} can have a different parameterized {@link OK} type.
     *
     * @param nextTry2Supplier The supplier of a {@link Try2} to return; only called if {@code this} is {@link OK}.
     * @throws NullPointerException if the supplied {@link Try2} is {@code null}.
     * @see #and(Try2)
     * @see #or(Try2)
     * @see #or(Supplier)
     */
    @NotNull
    default <V2> Try2<V2, E> and(@NotNull Supplier<Try2<V2, E>> nextTry2Supplier) {
        requireNonNull( nextTry2Supplier );
        return switch (this) {
            case OK<V, E> __ -> requireNonNull( nextTry2Supplier.get() );
            case Err<V, E> err -> err.coerceOK();
        };
    }

    /**
     * If {@code this} is {@link OK}, return it.
     * Otherwise, return the next {@link Try2} given.
     * The next {@link Try2}  can have a different parameterized {@link Err} type.
     *
     * @param nextTry2 The {@link Try2} to return.
     * @see #or(Supplier)
     * @see #and(Try2)
     * @see #and(Supplier)
     */
    @NotNull
    default <E2 extends Exception> Try2<V, E2> or(@NotNull Try2<V, E2> nextTry2) {
        requireNonNull( nextTry2 );
        return switch (this) {
            case OK<V, E> ok -> ok.coerceErr();
            case Err<V, E> __ -> nextTry2;
        };
    }

    /**
     * If {@code this} is {@link OK}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Try2} supplied.
     * The next {@link Try2} can have a different parameterized {@link Err} type.
     *
     * @param nextTry2Supplier The supplier of a {@link Try2} to return; only called if {@code this} is {@link Err}.
     * @throws NullPointerException if the supplier is called and returns {@code null}.
     * @see #or(Try2)
     * @see #and(Try2)
     * @see #and(Supplier)
     */
    @NotNull
    default <E2 extends Exception> Try2<V, E2> or(@NotNull Supplier<Try2<V, E2>> nextTry2Supplier) {
        requireNonNull( nextTry2Supplier );
        return switch (this) {
            case OK<V, E> ok -> ok.coerceErr();
            case Err<V, E> __ -> requireNonNull( nextTry2Supplier.get() );
        };
    }


    /**
     * Expect success (an {@link OK} value), otherwise throw a <strong>runtime</strong> Exception.
     * <p>
     * This always will throw for {@link Err}. If {@link Err} is a checked exception, this will
     * throw a checked Exception.
     * <p>
     * This is equivalent to {@code expect()}; a value is expected or an exception is thrown.
     *
     * @return Value (if {@link OK}))
     * @throws E ({@link Exception} held by {@link Err}).
     * @see #orThrow(Function)
     */
    @NotNull
    default V orThrow() throws E {
        // this method is NOT called 'expect()' because the error argument is always an exception, so it can be thrown
        return switch (this) {
            case OK(V v) -> v;
            case Err(E e) -> throw e;
        };
    }


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
     * @see #orThrow()
     */
    @NotNull
    default <X extends Exception> V orThrow(@NotNull Function<E, X> exFn) throws X {
        requireNonNull( exFn );
        return switch (this) {
            case OK(V v) -> v;
            case Err(E e) -> throw requireNonNull( exFn.apply( e ) );
        };
    }



}
