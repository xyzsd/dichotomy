package net.xyzsd.dichotomy;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


/**
 * A type which holds a value {@link Some}, or no value {@link None}.
 * <p>
 * This is analogous to the standard {@link java.util.Optional} class, but this is a sealed
 * type and can be used in {@code switch} statements and supports pattern-matching.
 *
 * @param <T> Value type.
 */
@NullMarked
public sealed interface Maybe<T> {


    /**
     * Create a Maybe with the given non-null value.
     *
     * @param value value of this Maybe
     * @param <T>   value type
     * @return Maybe holding given value.
     */
    static <T> Maybe<T> of(T value) {
        return new Some<>( value );
    }

    /**
     * Create an empty {@link Maybe}.
     *
     * @param <T> value type
     * @return empty Maybe
     */
    static <T> Maybe<T> ofNone() {
        return None.empty();
    }

    /**
     * Given a potentially nullable value, create a {@link Maybe} holding the value
     * or an empty {@link Maybe}  if the value is null.
     *
     * @param value value of this Maybe
     * @param <T>   value type
     * @return Maybe
     */
    static <T> Maybe<T> ofNullable(@Nullable T value) {
        return (value == null) ? Maybe.ofNone() : Maybe.of( value );
    }


    /**
     * Perform an action depending on whether the {@link Maybe} is a {@link Some} or a {@link None}.
     * <p>
     * This is analogous to {@link java.util.Optional#ifPresentOrElse(Consumer, Runnable)}
     *
     * @param someConsumer action performed if {@link Some}
     * @param noneRunner   action performed if {@link None}
     * @return this
     */
    Maybe<T> biMatch(Consumer<? super T> someConsumer, Runnable noneRunner);

    /**
     * Filter based on the given {@link Predicate}. If the Predicate is false, or this is a {@link None},
     * a {@link None} value is returned.
     *
     * @param predicate {@link Predicate} to test.
     * @return this or {@link None}
     */
    Maybe<T> filter(Predicate<? super T> predicate);

    /**
     * Map a {@link Some} or {@link None} value to a given type.
     *
     * @param fnSome  mapping function for Some values
     * @param supNone supplier for None values
     * @param <U>     return type of both functions
     * @return result of one of the above mapping function or supplier.
     */
    <U> U fold(Function<? super T, ? extends U> fnSome, Supplier<? extends U> supNone);

    /**
     * Stream the value contained. The stream will consist of at most a single element, and
     * will contain no elements if it is a {@link None}.
     *
     * @return Stream
     */
    Stream<T> stream();

    /**
     * Map {@link Some} types. No mapping is performed if the type is {@link None}.
     *
     * @param fnSome mapping function for {@link Some} values
     * @param <U>    return type of function
     * @return the result of the mapping function or a {@link None}
     */
    // only maps if present
    <U> Maybe<U> map(Function<? super T, ? extends U> fnSome);

    /**
     * If the {@link Maybe} is a {@link Some}, call the provided {@link Consumer}. Otherwise, do nothing.
     *
     * @param someConsumer act upon the given {@link Some} value
     * @return this
     */
    Maybe<T> match(Consumer<? super T> someConsumer);

    /**
     * If the {@link Maybe} is a {@link Some}, call the provided {@link Consumer}. Otherwise, do nothing.
     * <p>
     * This is functionally equivalent to {@link #match(Consumer)} but does not return {@code this},
     * clearly marking it as a terminal operation.
     *
     * @param someConsumer act upon the given {@link Some} value
     */
    void consume(Consumer<? super T> someConsumer);

    /**
     * If a value is present ({@link Some}), return the result of applying the given mapping function to the value,
     * otherwise returns a {@link None}.
     * <p>
     * This does not wrap the returned function value in a new {@link Some} value, unlike {@link #map(Function)}.
     *
     * @param mapper mapper for {@link Some} values.
     * @param <U>    type of {@link Maybe} returned.
     * @return result of mapping, or {@link None}
     */
    <U> Maybe<U> flatMap(Function<? super T, ? extends Maybe<? extends U>> mapper);

    /**
     * Terminal operation which tests a {@link Some} value against the given {@link Predicate}.
     * <p>
     * This is functionally equivalent to:
     * {@snippet :
     *  filter(Predicate).hasSome()
     *}
     *
     * @param predicate {@link Predicate} to test
     * @return result of predicate testing
     */
    boolean matches(Predicate<T> predicate);

    /**
     * Returns {@code true} if {@code value} equals the value contained in a {@link Some}.
     * If {@code value} is {@code null}, returns {@code true} if {@link None}.
     * <p>
     * This is analogous to {@link java.util.Optional#equals(Object)}.
     *
     * @param value value to evaluate
     * @return true if provided value equals the contained value.
     */
    boolean contains(@Nullable final T value);

    /**
     * True if this is a {@link Some} (contains <i>some</i>thing)
     *
     * @return {@code true} if this is a {@link Some} type.
     */
    boolean hasSome();


    /**
     * True if this is a {@link None}. (contains nothing).
     *
     * @return {@code true} if this is a {@link None} type.
     */
    boolean isNone();

    /**
     * If this is a {@link Some}, return it. Otherwise, use the provided alternate value.
     *
     * @param alternate used if this is {@link None}
     * @return this or the given Maybe
     */
    T orElse(T alternate);

    /**
     * If this is a {@link Some}, return it. Otherwise, use the provided alternate value.
     * The Supplier is only invoked if this is a {@link None}.
     *
     * @param supplier, used if this is {@link None}
     * @return this or the supplied Maybe
     */
    T orElse(Supplier<? extends T> supplier);

    /**
     * Get the next {@link Maybe}, but only if the current {@link Maybe} is a {@link Some}.
     * Otherwise, do nothing.
     *
     * @param nextMaybe if this is not a {@link None}
     * @param <U>       type of Maybe returned
     * @return this or the given Maybe
     */
    <U> Maybe<U> and(Maybe<? extends U> nextMaybe);

    /**
     * Get the next {@link Maybe}, but only if the current {@link Maybe} is a {@link Some}.
     * Otherwise, do nothing.
     *
     * @param nextMaybeSupplier invoked this is not a {@link None}
     * @param <U>               type of Maybe returned
     * @return this or the supplied Maybe
     */
    <U> Maybe<U> and(Supplier<Maybe<? extends U>> nextMaybeSupplier);

    /**
     * If this is a {@link Some}, return it. Otherwise, return {@code nextMaybe}.
     *
     * @param nextMaybe the next Maybe
     * @return nextMaybe (returned if this is a {@link None}
     */
    Maybe<T> or(Maybe<T> nextMaybe);

    /**
     * If this is a {@link Some}, return it. Otherwise, return the maybe via the provided {@link Supplier}.
     *
     * @param nextMaybeSupplier supplier of the next Maybe
     * @return nextMaybeSupplier (invoked if this is a {@link None}
     */
    Maybe<T> or(Supplier<Maybe<T>> nextMaybeSupplier);

    /**
     * Get the value, if this is a {@link Some}; otherwise, throw an exception.
     *
     * @return value
     * @throws NoSuchElementException if this is a {@link None}
     */
    T expect();

    /**
     * Get the value, if this is a {@link Some}; otherwise, throw an exception via the provided {@link Supplier}.
     *
     * @param <X>      Exception to throw; if not a (subclass of) RuntimeException, it must be rethrown or caught
     * @param supplier Supplier of exceptions to throw
     * @return value
     * @throws X Exception to throw
     */
    <X extends Throwable> T getOrThrow(Supplier<X> supplier) throws X;

    /**
     * The non-empty {@link Maybe} which holds a non-null value.
     *
     * @param value the value
     * @param <T>   value type
     */
    record Some<T>(T value) implements Maybe<T> {


        /**
         * Represents a Maybe that contains a non-null value.
         */
        public Some {
            requireNonNull( value );
        }


        @Override
        public Maybe<T> biMatch(Consumer<? super T> someConsumer, Runnable noneRunner) {
            requireNonNull( someConsumer );
            requireNonNull( noneRunner );
            someConsumer.accept( value );
            return this;
        }

        @Override
        public Maybe<T> filter(Predicate<? super T> predicate) {
            requireNonNull( predicate );
            return predicate.test( value ) ? this : None.empty();
        }

        @Override
        public <U> U fold(Function<? super T, ? extends U> fnSome, Supplier<? extends U> supNone) {
            requireNonNull( fnSome );
            requireNonNull( supNone );
            return requireNonNull( fnSome.apply( value ) );
        }

        @Override
        public Stream<T> stream() {
            return Stream.of( value );
        }

        @Override
        public <U> Maybe<U> map(Function<? super T, ? extends U> fnSome) {
            requireNonNull( fnSome );
            return new Some<>( fnSome.apply( value ) );
        }

        @Override
        public Maybe<T> match(Consumer<? super T> someConsumer) {
            requireNonNull( someConsumer );
            someConsumer.accept( value );
            return this;
        }

        @Override
        public void consume(Consumer<? super T> someConsumer) {
            requireNonNull( someConsumer );
            someConsumer.accept( value );
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Maybe<U> flatMap(Function<? super T, ? extends Maybe<? extends U>> mapper) {
            requireNonNull( mapper );
            return (Maybe<U>) requireNonNull( mapper.apply( value ) );
        }

        @Override
        public boolean matches(Predicate<T> predicate) {
            requireNonNull( predicate );
            return predicate.test( value );
        }

        @Override
        public boolean contains(@Nullable T v) {
            return Objects.equals( value, v );
        }

        @Override
        public boolean hasSome() {
            return true;
        }

        @Override
        public boolean isNone() {
            return false;
        }

        @Override
        public T orElse(T alternate) {
            requireNonNull( value );
            return value;
        }

        @Override
        public T orElse(Supplier<? extends T> supplier) {
            requireNonNull( supplier );
            return value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Maybe<U> and(Maybe<? extends U> nextMaybe) {
            requireNonNull( nextMaybe );
            return (Maybe<U>) nextMaybe;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Maybe<U> and(Supplier<Maybe<? extends U>> nextMaybeSupplier) {
            requireNonNull( nextMaybeSupplier );
            return requireNonNull( (Maybe<U>) nextMaybeSupplier.get() );
        }

        @Override
        public Maybe<T> or(Maybe<T> nextMaybe) {
            requireNonNull( nextMaybe );
            return this;
        }

        @Override
        public Maybe<T> or(Supplier<Maybe<T>> nextMaybeSupplier) {
            requireNonNull( nextMaybeSupplier );
            return this;
        }

        @Override
        public T expect() {
            return value;
        }

        @Override
        public <X extends Throwable> T getOrThrow(Supplier<X> supplier) {
            return value;
        }
    }

    /**
     * The empty {@link Maybe}.
     * <p>
     * This None has a type which s always coerced to the needed type.
     *
     * @param <T> value type
     */
    record None<T>() implements Maybe<T> {
        private static final None<Object> _NONE_INSTANCE = new None<>();

        @SuppressWarnings("unchecked")
        private static <U> None<U> empty() {
            return (None<U>) _NONE_INSTANCE;
        }


        @Override
        public Maybe<T> biMatch(Consumer<? super T> someConsumer, Runnable noneRunner) {
            requireNonNull( someConsumer );
            requireNonNull( noneRunner );
            noneRunner.run();
            return this;
        }

        @Override
        public Maybe<T> filter(Predicate<? super T> predicate) {
            requireNonNull( predicate );
            return None.empty();
        }

        @Override
        public <U> U fold(Function<? super T, ? extends U> fnSome, Supplier<? extends U> supNone) {
            requireNonNull( fnSome );
            requireNonNull( supNone );
            return requireNonNull( supNone.get() );
        }

        @Override
        public Stream<T> stream() {
            return Stream.empty();
        }

        @Override
        public <U> Maybe<U> map(Function<? super T, ? extends U> fnSome) {
            requireNonNull( fnSome );
            return empty();
        }

        @Override
        public Maybe<T> match(Consumer<? super T> someConsumer) {
            requireNonNull( someConsumer );
            return this;    // do nothing
        }

        @Override
        public void consume(Consumer<? super T> someConsumer) {
            requireNonNull( someConsumer );
            // do nothing
        }

        @Override
        public <U> Maybe<U> flatMap(Function<? super T, ? extends Maybe<? extends U>> someConsumer) {
            requireNonNull( someConsumer );
            return empty();
        }

        @Override
        public boolean matches(Predicate<T> predicate) {
            requireNonNull( predicate );
            return false;
        }

        @Override
        public boolean contains(@Nullable T value) {
            return (value == null);
        }

        @Override
        public boolean hasSome() {
            return false;
        }

        @Override
        public boolean isNone() {
            return true;
        }

        @Override
        public T orElse(T alternate) {
            requireNonNull( alternate );
            return alternate;
        }

        @Override
        public T orElse(Supplier<? extends T> supplier) {
            requireNonNull( supplier );
            return requireNonNull( supplier.get() );
        }

        @Override
        public <U> Maybe<U> and(Maybe<? extends U> nextMaybe) {
            requireNonNull( nextMaybe );
            return empty();
        }

        @Override
        public <U> Maybe<U> and(Supplier<Maybe<? extends U>> nextMaybeSupplier) {
            requireNonNull( nextMaybeSupplier );
            return empty();
        }

        @Override
        public Maybe<T> or(Maybe<T> nextMaybe) {
            requireNonNull( nextMaybe );
            return nextMaybe;
        }

        @Override
        public Maybe<T> or(Supplier<Maybe<T>> nextMaybeSupplier) {
            requireNonNull( nextMaybeSupplier );
            return requireNonNull( nextMaybeSupplier.get() );
        }

        @Override
        public T expect() throws NoSuchElementException {
            throw new NoSuchElementException();
        }

        @Override
        public <X extends Throwable> T getOrThrow(Supplier<X> supplier) throws X {
            throw requireNonNull( supplier.get() );
        }
    }

}
