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

import static java.util.Objects.requireNonNull;

/**
 * An unsuccessful (failure) Result.
 * <p>
 * Note that because Err cannot be null, {@code Err<Void>} is not legitimate.
 * </p>
 *
 * @param <V>   Contained Success type
 * @param <E>   Contained Error (failure) type
 * @param error Value that this Result wraps
 */
public record Err<V, E>(@NotNull E error) implements Result<V, E> {

    /**
     * Create an OK with the given non-null value.
     *
     * @param error OK Value
     */
    public Err {
        requireNonNull( error, "Err: error is null!" );
    }


    /**
     * Get the Err E
     *
     * @return unwrapped Err E
     */
    public @NotNull E get() {
        return error;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOK() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErr() {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<V> ok() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<E> err() {
        return Optional.of( error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Object unwrap() {
        return error;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Result<V, E> biMatch(@NotNull Consumer<? super V> okConsumer,
                                       @NotNull Consumer<? super E> errConsumer) {
        requireNonNull( okConsumer );
        requireNonNull( errConsumer );
        errConsumer.accept( error );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Result<V, E> filter(@NotNull Predicate<? super V> predicate,
                                        @NotNull Function<? super V, ? extends E> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V2, E2> @NotNull Result<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper,
                                                @NotNull Function<? super E, ? extends E2> errMapper) {
        requireNonNull( okMapper );
        requireNonNull( errMapper );
        return new Err<>( errMapper.apply( error ) );   // implicit null check
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
        return (Result<V2, E2>) requireNonNull( fnErr.apply( error ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> @NotNull T fold(@NotNull Function<? super V, ? extends T> fnOK, @NotNull Function<? super E, ? extends T> fnErr) {
        requireNonNull( fnOK );
        requireNonNull( fnErr );
        return requireNonNull( fnErr.apply( error ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<V> stream() {
        return Stream.empty();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <V2> @NotNull Result<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
        requireNonNull( okMapper );
        return coerceValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V2> @NotNull Result<V2, E> flatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper) {
        requireNonNull( okMapper );
        return coerceValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(@NotNull Predicate<V> okPredicate) {
        requireNonNull( okPredicate );
        return false;   // always fails on error
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable V okValue) {
        return false;   // never matches okValue
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V orElse(@NotNull V okAlternate) {
        return requireNonNull( okAlternate );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V orElseGet(@NotNull Supplier<? extends V> okSupplier) {
        requireNonNull( okSupplier );
        return requireNonNull( okSupplier.get() );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<E> streamErr() {
        return Stream.of( error );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
        requireNonNull( errMapper );
        return new Err<>( errMapper.apply( error ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E2> @NotNull Result<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper) {
        requireNonNull( errMapper );
        return (Result<V, E2>) requireNonNull( errMapper.apply( error ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesErr(@NotNull Predicate<E> errPredicate) {
        requireNonNull( errPredicate );
        return errPredicate.test( error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsErr(@Nullable E errValue) {
        return Objects.equals( error, errValue );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull E orElseErr(@NotNull E errAlternate) {
        requireNonNull( errAlternate );
        return error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull E orElseGetErr(@NotNull Supplier<? extends E> errSupplier) {
        requireNonNull( errSupplier );
        return error;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <V2> @NotNull Result<V2, E> and(@NotNull Result<V2, E> nextResult) {
        requireNonNull( nextResult );
        return coerceValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull <V2> Result<V2, E> and(@NotNull Supplier<Result<V2, E>> nextResultSupplier) {
        requireNonNull( nextResultSupplier );
        return coerceValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> or(@NotNull Result<V, E2> nextResult) {
        return requireNonNull( nextResult );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <E2> @NotNull Result<V, E2> or(@NotNull Supplier<Result<V, E2>> nextResultSupplier) {
        requireNonNull( nextResultSupplier );
        return requireNonNull( nextResultSupplier.get() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Throwable> @NotNull V orThrowWrapped(@NotNull Function<E, X> exFn) throws X {
        requireNonNull( exFn );
        throw requireNonNull( exFn.apply( error ) );
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public <X extends Throwable> V orThrow(@NotNull Supplier<X> supplier) throws X {
        requireNonNull( supplier );
        return orThrowWrapped( (left) -> supplier.get() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V expect() {
        if (error instanceof RuntimeException e) {
            throw e;
        } else if (error instanceof Throwable t) {
            throw new ResultException( t );
        }

        throw new ResultException( String.valueOf( error ) );
        /*
        // using JDK 17 preview features:

        switch (error) {
            case RuntimeException e -> throw e;
            case Throwable t -> throw new ResultException( t );
            default -> throw new ResultException( String.valueOf( error ) );
        }

         */
    }


    // For types where the error type is unchanged and exists, but the generic type of the value differs
    // just cast and return. Types are erased so there is no need to create a new object.
    // The Error stays the same; only the empty value signature changes
    @SuppressWarnings("unchecked")
    @NotNull
    private <V2> Result<V2, E> coerceValue() {
        return (Result<V2, E>) this;
    }
}
