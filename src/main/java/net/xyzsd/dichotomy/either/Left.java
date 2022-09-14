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
 * Implementation of a Left {@link Either}.
 *
 * @param value Left value. May not be {@code null}.
 * @param <L> parameter type of Left values.
 * @param <R> parameter type of Right values.
 */
public record Left<L, R>(@NotNull L value) implements Either<L, R> {


    /**
     * Create a Left value.
     */
    public Left {
        requireNonNull( value, "Left: value cannot be null!" );
    }

    /**
     * Get the contained value.
     */
    // equivalent to value()
    @NotNull
    public L get() {
        return value;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<L> left() {
        return Optional.of( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<R> right() {
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
    public boolean isLeft() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRight() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Either<L, R> biMatch(@NotNull Consumer<? super L> leftConsumer, @NotNull Consumer<? super R> rightConsumer) {
        requireNonNull( leftConsumer );
        requireNonNull( rightConsumer );
        leftConsumer.accept( value );
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
        return Either.ofLeft( fnLeft.apply( value ) );
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
        return (Either<L2, R2>) requireNonNull( fnLeft.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> @NotNull T fold(@NotNull Function<? super L, ? extends T> fnLeft, @NotNull Function<? super R, ? extends T> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );
        return requireNonNull( fnLeft.apply( value ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Either<L, R> filter(@NotNull Predicate<? super R> predicate, @NotNull Function<? super R, ? extends L> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<R> stream() {
        return Stream.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Stream<L> streamLeft() {
        return Stream.of( value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable R rVal) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLeft(@Nullable L lVal) {
        return value.equals( lVal );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(@NotNull Predicate<R> rp) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesLeft(@NotNull Predicate<L> lp) {
        return requireNonNull( lp ).test( value );
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <L2> @NotNull Either<L2, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper) {
        requireNonNull( leftMapper );
        return (Either<L2, R>) requireNonNull( leftMapper.apply( value ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R2> @NotNull Either<L, R2> flatMap(@NotNull Function<? super R, ? extends Either<? extends L, ? extends R2>> rightMapper) {
        requireNonNull( rightMapper );
        return coerceRight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull R orElse(@NotNull R rightAlternate) {
        requireNonNull( rightAlternate );
        return rightAlternate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull L orElseLeft(@NotNull L leftAlternate) {
        requireNonNull( leftAlternate );
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull R orElseGet(@NotNull Supplier<? extends R> rightSupplier) {
        requireNonNull( rightSupplier );
        return requireNonNull( rightSupplier.get() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull L orElseGetLeft(@NotNull Supplier<? extends L> leftSupplier) {
        requireNonNull( leftSupplier );
        return value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <R2> @NotNull Either<L, R2> and(@NotNull Either<L, R2> nextEither) {
        requireNonNull( nextEither );
        return coerceRight();
    }

    /**
     * {@inheritDoc}
     */
    // more efficient than default implementation : does not invoke supplier
    @Override
    public <R2> @NotNull Either<L, R2> and(@NotNull Supplier<Either<L, R2>> nextEitherSupplier) {
        requireNonNull( nextEitherSupplier );
        return coerceRight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <L2> @NotNull Either<L2, R> or(@NotNull Either<L2, R> nextEither) {
        requireNonNull( nextEither );
        return nextEither;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Throwable> @NotNull R getOrThrow(@NotNull Supplier<X> supplier) throws X {
        requireNonNull( supplier );
        throw requireNonNull( supplier.get( ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Throwable> @NotNull R getOrThrowWrapped(@NotNull Function<L, X> exFn) throws X {
        requireNonNull( exFn );
        throw requireNonNull( exFn.apply( value ) );
    }


    // only 'coerce' the empty value. For Left values, that would be the Right side
    @SuppressWarnings("unchecked")
    @NotNull
    private <R2> Either<L, R2> coerceRight() {
        return (Either<L, R2>) this;
    }
}
