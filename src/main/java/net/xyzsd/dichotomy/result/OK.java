package net.xyzsd.dichotomy.result;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.xyzsd.dichotomy.None;

import static java.util.Objects.requireNonNull;

// Void will not work as a type due to Void === Null; use None type instead

/**
 * A successful Result.
 * <p>
 * Note that because OK cannot be null, {@code OK<Void>} is not legitimate.
 * Instead, use an empty type such as {@link None}.
 * </p>
 *
 * @param <V>   Contained Success type
 * @param <E>   Contained Error (failure) type
 * @param value Value that this Result wraps
 */
public record OK<V, E>(@NotNull V value) implements Result<V, E> {


    /**
     * Create an OK with the given non-null value.
     *
     * @param value OK Value
     */
    public OK {
        requireNonNull(  value, "OK: value is null!" );
    }


    /**
     * Get the value V
     *
     * @return unwrapped value V
     */
    public @NotNull V get() {
        return value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOK() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErr() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<V> ok() {
        return Optional.of( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<E> err() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Object unwrap() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Result<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer) {
        requireNonNull( okConsumer );
        requireNonNull( errConsumer );
        okConsumer.accept( value );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Result<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );
        return predicate.test( value ) ? this : new Err<>( mapper.apply( value ) );    // implicit null check
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V2, E2> @NotNull Result<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper,
                                                @NotNull Function<? super E, ? extends E2> errMapper) {
        requireNonNull( okMapper );
        requireNonNull( errMapper );
        return new OK<>( okMapper.apply( value ) );  // implicit null check
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V2, E2> @NotNull Result<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E2>> fnOK,
                                                      @NotNull Function<? super E, ? extends Result<? extends V2, ? extends E2>> fnErr) {
        requireNonNull( fnOK );
        requireNonNull( fnErr );
        return (Result<V2, E2>) requireNonNull( fnOK.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> @NotNull T fold(@NotNull Function<? super V, ? extends T> fnOK, @NotNull Function<? super E, ? extends T> fnErr) {
        requireNonNull( fnOK );
        requireNonNull( fnErr );
        return requireNonNull( fnOK.apply( value ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<V> stream() {
        return Stream.of( value );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <V2> @NotNull Result<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
        requireNonNull( okMapper );
        return new OK<>( okMapper.apply( value ) );   // implicit null check
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V2> @NotNull Result<V2, E> flatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper) {
        requireNonNull( okMapper );
        return (Result<V2, E>) requireNonNull( okMapper.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(@NotNull Predicate<V> okPredicate) {
        return requireNonNull( okPredicate ).test( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable V okValue) {
        return Objects.equals( value, okValue );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V orElse(@NotNull V okAlternate) {
        requireNonNull( okAlternate );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V orElseGet(@NotNull Supplier<? extends V> okSupplier) {
        requireNonNull( okSupplier );
        return value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<E> streamErr() {
        return Stream.empty();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
        requireNonNull( errMapper );
        return coerceErr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper) {
        requireNonNull( errMapper );
        return coerceErr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesErr(@NotNull Predicate<E> errPredicate) {
        requireNonNull( errPredicate );
        return false;   // OK never matches Err
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsErr(@NotNull E errValue) {
        requireNonNull( errValue );
        return false; // OK never contains Err
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull E orElseErr(@NotNull E errAlternate) {
        return requireNonNull( errAlternate );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull E orElseGetErr(@NotNull Supplier<? extends E> errSupplier) {
        return requireNonNull( requireNonNull( errSupplier ).get() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <V2> @NotNull Result<V2, E> and(@NotNull Result<V2, E> nextResult) {
        requireNonNull( nextResult );
        return nextResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V2> @NotNull Result<V2, E> and(@NotNull Supplier<Result<V2, E>> nextResultSupplier) {
        requireNonNull( nextResultSupplier );
        return requireNonNull( nextResultSupplier.get() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> or(@NotNull Result<V, E2> nextResult) {
        requireNonNull( nextResult );
        return coerceErr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> or(@NotNull Supplier<Result<V, E2>> nextResultSupplier) {
        requireNonNull( nextResultSupplier );
        return coerceErr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V expect() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Throwable> @NotNull V orThrowWrapped(@NotNull Function<E, X> exFn) {
        requireNonNull( exFn );
        return value;  // never throws
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <X extends Throwable> V orThrow(@NotNull Supplier<X> supplier) throws X {
        requireNonNull( supplier );
        return value;  // never throws
    }



    // For types where the error type is unchanged and exists, but the generic type of the value differs
    // just cast and return. Types are erased so there is no need to create a new object.
    // The value stays the same, only the empty error signature changes
    @SuppressWarnings("unchecked")
    @NotNull
    private <E2> Result<V, E2> coerceErr() {
        return (Result<V, E2>) this;
    }
}
