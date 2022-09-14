package net.xyzsd.dichotomy.either;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of a Right {@link Either}.
 *
 * @param value Right value. May not be {@code null}.
 * @param <L>   parameter type of Left values.
 * @param <R>   parameter type of Right values.
 */
public record Right<L, R>(@NotNull R value) implements Either<L, R> {

    /**
     * Create a Right value.
     */
    public Right {
        requireNonNull( value, "Right: value cannot be null!" );
    }

    /**
     * Get the contained value.
     */
    // equivalent to value()
    @NotNull
    public R get() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<L> left() {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<R> right() {
        return Optional.of( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeft() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRight() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Either<L, R> biMatch(@NotNull Consumer<? super L> leftConsumer, @NotNull Consumer<? super R> rightConsumer) {
        requireNonNull( leftConsumer );
        requireNonNull( rightConsumer );
        rightConsumer.accept( value );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <L2, R2> @NotNull Either<L2, R2> biMap(@NotNull Function<? super L, ? extends L2> fnLeft,
                                                  @NotNull Function<? super R, ? extends R2> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );
        return Either.ofRight( fnRight.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <L2, R2> @NotNull Either<L2, R2> biFlatMap(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R2>> fnLeft,
                                                      @NotNull Function<? super R, ? extends Either<? extends L2, ? extends R2>> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );
        return (Either<L2, R2>) requireNonNull( fnRight.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> @NotNull T fold(@NotNull Function<? super L, ? extends T> fnLeft,
                               @NotNull Function<? super R, ? extends T> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );
        return requireNonNull( fnRight.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Either<L, R> filter(@NotNull Predicate<? super R> predicate, @NotNull Function<? super R, ? extends L> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );
        return predicate.test( value ) ? this : Either.ofLeft( mapper.apply( value ) ); // implicit null check
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<R> stream() {
        return Stream.of( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<L> streamLeft() {
        return Stream.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable R rVal) {
        return value.equals( rVal );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLeft(@Nullable L lVal) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(@NotNull Predicate<R> rp) {
        return requireNonNull( rp ).test( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesLeft(@NotNull Predicate<L> lp) {
        requireNonNull( lp );
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <L2> @NotNull Either<L2, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper) {
        requireNonNull( leftMapper );
        return coerceLeft();
    }

    @SuppressWarnings("unchecked")
    /**
     * {@inheritDoc}
     */
    @Override
    public <R2> @NotNull Either<L, R2> flatMap(@NotNull Function<? super R, ? extends Either<? extends L, ? extends R2>> rightMapper) {
        requireNonNull( rightMapper );
        return (Either<L, R2>) requireNonNull( rightMapper.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull R orElse(@NotNull R rightAlternate) {
        requireNonNull( rightAlternate );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull L orElseLeft(@NotNull L leftAlternate) {
        requireNonNull( leftAlternate );
        return leftAlternate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull R orElseGet(@NotNull Supplier<? extends R> rightSupplier) {
        requireNonNull( rightSupplier );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull L orElseGetLeft(@NotNull Supplier<? extends L> leftSupplier) {
        requireNonNull( leftSupplier );
        return requireNonNull( leftSupplier.get() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R2> @NotNull Either<L, R2> and(@NotNull Either<L, R2> nextEither) {
        requireNonNull( nextEither );
        return nextEither;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <L2> @NotNull Either<L2, R> or(@NotNull Either<L2, R> nextEither) {
        requireNonNull( nextEither );
        return coerceLeft();
    }

    // more efficient than default implementation : does not invoke supplier

    /**
     * {@inheritDoc}
     */
    @Override
    public <L2> @NotNull Either<L2, R> or(@NotNull Supplier<Either<L2, R>> nextEitherSupplier) {
        requireNonNull( nextEitherSupplier );
        return coerceLeft();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <X extends Throwable> R getOrThrow(@NotNull Supplier<X> supplier) throws X {
        // supplier not invoked
        requireNonNull( supplier );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Throwable> @NotNull R getOrThrowWrapped(@NotNull Function<L, X> exFn) {
        // function not invoked
        requireNonNull( exFn );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Object unwrap() {
        return value;
    }


    // only 'coerce' the empty value. For Right values, that would be the Left side
    @SuppressWarnings("unchecked")
    @NotNull
    private <L2> Either<L2, R> coerceLeft() {
        return (Either<L2, R>) this;
    }
}
