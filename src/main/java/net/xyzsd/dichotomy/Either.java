package net.xyzsd.dichotomy;

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
 * A right-biased Either monad implementation.
 * <blockquote>
 * {@code An Either holds Left or Right values,}<br>
 * {@code but never both or neither,}<br>
 * {@code otherwise it'd be a tuple,}<br>
 * {@code rather than an Either.}
 * </blockquote>
 * <p>
 * Features:
 * <ul>
 *     <li>{@link Left} and {@link Right} values cannot hold {@code null}. The {@link Empty} type could be used instead.</li>
 *     <li>Right-biased; methods not ending in {@code Left} operate on {@link Right} values.
 *          However, the {@code xxxLeft} methods are provided so that a {@link #swap}() is not required. </li>
 *     <li>Sealed; can be used in {@code switch} statements and deconstruction patterns.</li>
 *     <li>Separable; this file only depends on standard JDK classes.</li>
 * </ul>
 * <p>
 * For example:
 * {@snippet :
 *      //** TODO ** include a succinct example
 *}
 *
 * <p>
 * NOTE: For methods which accept or return type parameters {@code <L2>} or {@code <R2>},
 * the {@code <L2>} or {@code <R2>} parameters can be completely different types from {@code <L>} or {@code <R>}.
 *
 * @param <L> The left-hand value (by convention, this is the failure/unsuccessful value)
 * @param <R> The right-hand value (by convention, the success value)
 */
public sealed interface Either<L, R> {

    /**
     * Create a Left {@link Either}.
     * <p>
     * By convention, the left-sided value is the abnormal / unexpected / error value.
     * </p>
     */
    @NotNull
    static <L, R> Either<L, R> ofLeft(@NotNull L value) {
        return new Left<>( value );
    }

    /**
     * Create a Right {@link Either}.
     * <p>
     * By convention, the right-sided value is the normal / expected value.
     * </p>
     */
    @NotNull
    static <L, R> Either<L, R> ofRight(@NotNull R value) {
        return new Right<>( value );
    }


    /**
     * If the {@link Either} is {@link Left}, return the value as an {@link Optional}.
     * Otherwise, return an empty {@link Optional}.
     */
    @NotNull Optional<L> left();

    /**
     * If the {@link Either} is {@link Right}, return the value as an {@link Optional}.
     * Otherwise, return an empty {@link Optional}.
     */
    @NotNull Optional<R> right();


    /**
     * Executes the action for the {@link Left} or {@link Right} depending upon
     * the value of this {@link Either}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #matchLeft(Consumer)
     */
    @NotNull Either<L, R> biMatch(@NotNull Consumer<? super L> leftConsumer, @NotNull Consumer<? super R> rightConsumer);


    /**
     * Returns a new {@link Either}, the value of which is determined by the appropriate mapping function.
     * <p>
     * The returned Either (which may be {@link Left} or {@link Right}) can have different types.
     * </p>
     *
     * @param fnLeft  the mapping function for {@link Left} values.
     * @param fnRight the mapping function for {@link Right} values.
     * @return the {@link Either} produced from {@code fnLeft} or {@code fnRight}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapLeft(Function)
     */
    @NotNull <L2, R2> Either<L2, R2> biMap(@NotNull Function<? super L, ? extends L2> fnLeft, @NotNull Function<? super R, ? extends R2> fnRight);


    /**
     * Returns an {@link Either}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced Either (which may be {@link Left} or {@link Right}) can have different types.
     * </p>
     *
     * @param fnLeft  the mapping function for {@link Left} values.
     * @param fnRight the mapping function for {@link Right} values.
     * @param <L2>    New {@link Left} value
     * @param <R2>    New {@link Right} value
     * @return the {@link Either} produced from {@code fnLeft} or {@code fnRight}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapLeft(Function)
     */
    @NotNull <L2, R2> Either<L2, R2> biFlatMap(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R2>> fnLeft, @NotNull Function<? super R, ? extends Either<? extends L2, ? extends R2>> fnRight);


    /**
     * Returns a value, produced from one of the appropriate mapping functions.
     * <p>
     * The produced value can have any type (except {@link Void}) but mapping functions for
     * both {@link Left} and {@link Right} types must produce the same value type.
     * </p>
     * <p>
     * If no value is to be returned, use {@link #biMatch(Consumer, Consumer)} instead.
     * </p>
     *
     * @param fnLeft  the mapping function for {@link Left} values.
     * @param fnRight the mapping function for {@link Right} values.
     * @param <T>     common type returned by mapping functions.
     * @return the value produced from {@code fnLeft} or {@code fnRight}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #recover(Function)
     * @see #forfeit(Function)
     */
    @NotNull <T> T fold(@NotNull Function<? super L, ? extends T> fnLeft, @NotNull Function<? super R, ? extends T> fnRight);


    /**
     * Filter an {@link Either}.
     * <p>
     * If this {@link Either} is {@link Left}, return {@link Left} ({@code this}).
     * The {@code Predicate} is not tested, and the mapper {@code Function} is not executed.
     * </p>
     * <p>
     * If this {@link Either} is {@link Right}, return {@link Right} ({@code this}) if the {@code Predicate} matches.
     * If the {@code Predicate} fails to match, return a {@link Left} {@link Either} produced by applying the
     * mapping function to the current {@link Either} ({@code this}).
     * </p>
     *
     * @param predicate the predicate used to test {@link Right} values.
     * @param mapper    the mapping function for {@link Right} values that do not match the predicate.
     * @return an {@link Either} based on the algorithm described above.
     * @throws NullPointerException if the called mapping function returns {@code null}.
     */
    @NotNull Either<L, R> filter(@NotNull Predicate<? super R> predicate, @NotNull Function<? super R, ? extends L> mapper);


    /**
     * Forfeit (ignore) the {@link Right} value if present, and apply the mapping function to get a {@link Left}.
     * <p>
     * If this is a {@link Left}, return it without applying the mapping function.
     * </p>
     * <p>
     * This method is equivalent in alternative implementations to {@code orElseMapLeft()}.
     * </p>
     *
     * @param fn {@link Function} that produces a {@link Left} value.
     * @return A {@link Left} value; the current {@link Left} if present, or the produced {@link Left} if not.
     * @throws NullPointerException if the result of the mapping function is {@code null}.
     * @see #recover(Function)
     */
    @NotNull L forfeit(@NotNull Function<? super R, ? extends L> fn);


    /**
     * Recover from an error; ignore the {@link Left} value if present,
     * and apply the mapping function to get a {@link Right}.
     * <p>
     * If this is a {@link Right}, return it without applying the mapping function.
     * </p>
     * <p>
     * This method is equivalent in alternative implementations to {@code orElseMap()}.
     * </p>
     *
     * @param fn {@link Function} that produces a {@link Right} value.
     * @return A {@link Right} value; the current {@link Right} if present, or the produced {@link Right} if not.
     * @throws NullPointerException if the result of the mapping function is {@code null}.
     * @see #forfeit(Function)
     */
    @NotNull R recover(@NotNull Function<? super L, ? extends R> fn);


    /**
     * Return a {@link Stream}, containing either a single {@link Right} value, or an empty {@link Stream}
     * if this is a {@link Left value}.
     *
     * @return Stream
     * @see #streamLeft()
     */
    @NotNull Stream<R> stream();

    /**
     * Return a {@link Stream}, containing either a single {@link Left} value, or an empty {@link Stream}
     * if this is a {@link Right value}.
     *
     * @see #stream()
     */
    @NotNull Stream<L> streamLeft();

    /**
     * Determines if this {@link Right} {@link Either} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param rVal value to compare
     * @return {@code true} iff this is a {@link Right} {@link Either} and the contained value equals {@code rVal}
     * @see #ifPredicate
     * @see #containsLeft
     * @see #ifPredicateLeft
     */
    boolean contains(@Nullable R rVal);

    /**
     * Determines if this {@link Left} {@link Either} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param lVal value to compare
     * @return {@code true} iff this is a {@link Left} {@link Either} and the contained value equals {@code lVal}
     * @see #ifPredicateLeft
     * @see #ifPredicate
     * @see #contains
     */
    boolean containsLeft(@Nullable L lVal);

    /**
     * Determines if this {@link Right} {@link Either} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not tested if this is a {@link Left} {@link Either}
     * </p>
     *
     * @param rp the {@link Predicate} to test
     * @return {@code true} iff this is a {@link Right} {@link Either} and the {@link Predicate} matches.
     * @see #contains
     * @see #ifPredicateLeft
     * @see #containsLeft
     */
    boolean ifPredicate(@NotNull Predicate<R> rp);

    /**
     * Determines if this {@link Left} {@link Either} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not tested if this is a {@link Right} {@link Either}
     * </p>
     *
     * @param lp the {@link Predicate} to test
     * @return {@code true} iff this is a {@link Left} {@link Either} and the {@link Predicate} matches.
     * @see #containsLeft
     * @see #contains
     * @see #ifPredicate
     */
    boolean ifPredicateLeft(@NotNull Predicate<L> lp);

    /**
     * If this is a {@link Right}, return a new {@link Right} value produced by the given mapping function.
     * Otherwise, return the {@link Left} value.
     * <p>
     * The type of the produced {@link Right} can be different. The mapping function is only invoked for
     * {@link Right} values.
     * </p>
     * <p>
     * This is equivalent to {@code biMap( Function.identity(), rightMapper )}.
     * </p>
     *
     * @param rightMapper the mapping function producing a new {@link Right} value.
     * @return a new {@link Right} produced by the mapping function if this is {@link Right};
     * otherwise, returns a {@link Left}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #mapLeft(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull <R2> Either<L, R2> map(@NotNull Function<? super R, ? extends R2> rightMapper);

    /**
     * If this is a {@link Right}, return the new {@link Either} supplied by the mapping function.
     * Note that while the {@link Left} type must remain the same, the {@link Right} type returned
     * can be different.
     * <p>
     * This is also known as {@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is a {@link Left}, and the mapping function is not invoked.
     * </p>
     *
     * @param rightMapper the mapping function that produces a new {@link Either}
     * @return a new {@link Right} produced by the mapping function if this is {@link Right};
     * otherwise, returns a {@link Left}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMapLeft(Function)
     */
    @NotNull <R2> Either<L, R2> flatMap(@NotNull Function<? super R, ? extends Either<? extends L, ? extends R2>> rightMapper);


    /**
     * If this is a {@link Left}, return a new {@link Left} value produced by the given mapping function.
     * Otherwise, return the {@link Right} value.
     * <p>
     * The type of the produced {@link Left} can be different. The mapping function is only invoked for
     * {@link Left} values.
     * </p>
     * <p>
     * This is equivalent to {@code map( leftMapper, Function.identity() )}.
     * </p>
     *
     * @param leftMapper the mapping function producing a new {@link Left} value.
     * @return a new {@link Left} produced by the mapping function if this is {@link Left};
     * otherwise, returns a {@link Right}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #map(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull <L2> Either<L2, R> mapLeft(@NotNull Function<? super L, ? extends L2> leftMapper);


    /**
     * If this is a {@link Left}, return the new {@link Either} supplied by the mapping function.
     * Note that while the {@link Right} type must remain the same, the {@link Left} type returned
     * can be different.
     * <p>
     * This is also known as a left-{@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is a {@link Right}, and the mapping function is not invoked.
     * </p>
     *
     * @param leftMapper the mapping function that produces a new {@link Either}
     * @return a new {@link Left} produced by the mapping function if this is {@link Left};
     * otherwise, returns a {@link Right}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMap(Function)
     */
    @NotNull <L2> Either<L2, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper);


    /**
     * Executes the action iff this is a {@link Left} {@link Either}.
     *
     * @return {@code this}
     * @param leftConsumer the consumer function to be executed
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull Either<L, R> matchLeft(@NotNull Consumer<? super L> leftConsumer);

    /**
     * Executes the action iff this is a {@link Right} {@link Either}.
     *
     * @return {@code this}
     * @param rightConsumer the consumer function to be executed
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull Either<L, R> match(@NotNull Consumer<? super R> rightConsumer);


    /**
     * Executes the given consumer if this is a {@link Right}. This is a terminal operation.
     *
     * @param rightConsumer the consumer function to be executed
     */
    default void consume(@NotNull Consumer<? super R> rightConsumer) {
        match(rightConsumer);
    }


    /**
     * If this {@link Either} is {@link Left}, return {@code rightAlternate}.
     * Otherwise, return {@code this} (a {@link Right} {@link Either}).
     *
     * @param rightAlternate alternate {@link Right} {@link Either}
     * @return {@code this}, or {@code rightAlternate} if {@code this} is {@link Left}
     * @see #orElseGet
     * @see #orElseLeft
     * @see #orElseGetLeft
     */
    @NotNull R orElse(@NotNull R rightAlternate);

    /**
     * If this {@link Either} is {@link Right}, return {@code leftAlternate}.
     * Otherwise, return {@code this} (a {@link Left} {@link Either}).
     *
     * @param leftAlternate alternate {@link Left} {@link Either}
     * @return {@code this}, or {@code leftAlternate} if {@code this} is {@link Right}
     * @see #orElseLeft
     * @see #orElse
     * @see #orElseGet
     */
    @NotNull L orElseLeft(@NotNull L leftAlternate);

    /**
     * If this {@link Either} is {@link Left}, return the supplied {@link Right} {@link Either}.
     * Otherwise, return {@code this} (a {@link Right} {@link Either}) without
     * invoking the {@link Supplier}.
     *
     * @param rightSupplier supplier of {@link Right} {@link Either}s
     * @return {@code this}, or the supplied {@link Right} {@link Either} if {@code this} is {@link Left}
     * @see #orElse
     * @see #orElseLeft
     * @see #orElseGetLeft
     */
    @NotNull R orElseGet(@NotNull Supplier<? extends R> rightSupplier);

    /**
     * If this {@link Either} is {@link Right}, return the supplied {@link Left}  {@link Either}.
     * Otherwise, return {@code this} (a {@link Left} {@link Either}) without
     * invoking the {@link Supplier}.
     *
     * @param leftSupplier supplier of {@link Left} {@link Either}s
     * @return {@code this}, or the supplied {@link Left} {@link Either} if {@code this} is {@link Right}
     * @see #orElseLeft
     * @see #orElse
     * @see #orElseGet
     */
    @NotNull L orElseGetLeft(@NotNull Supplier<? extends L> leftSupplier);


    /**
     * If {@code this} is {@link Left}, return it. Otherwise, return the next {@link Either} given.
     * The next Either can have a different parameterized Right type.
     *
     * @param nextEither The {@link Either} to return.
     * @see #and(Supplier)
     * @see #or(Either)
     * @see #or(Supplier)
     */
    @NotNull <R2> Either<L, R2> and(@NotNull Either<L, R2> nextEither);

    /**
     * If {@code this} is {@link Left}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Either} supplied.
     * The next Either can have a different parameterized Right type.
     *
     * @param nextEitherSupplier The supplier of an {@link Either} to return; only called if {@code this} is {@link Right}.
     * @param <R2>               New {@link Right} value
     * @throws NullPointerException if the supplied {@link Either} is {@code null}.
     * @see #and(Either)
     * @see #or(Either)
     * @see #or(Supplier)
     */
    @NotNull <R2> Either<L, R2> and(@NotNull Supplier<Either<L, R2>> nextEitherSupplier);


    /**
     * If {@code this} is {@link Right}, return it.
     * Otherwise, return the next {@link Either} given.
     * The next Either can have a different parameterized Left type.
     *
     * @param nextEither The {@link Either} to return.
     * @param <L2>       New {@link Left} value
     * @see #or(Supplier)
     * @see #and(Either)
     * @see #and(Supplier)
     */
    @NotNull <L2> Either<L2, R> or(@NotNull Either<L2, R> nextEither);


    /**
     * If {@code this} is {@link Right}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Either} supplied.
     * The next Either can have a different parameterized Left type.
     *
     * @param nextEitherSupplier The supplier of an {@link Either} to return; only called if {@code this} is {@link Left}.
     * @param <L2>               New {@link Left} value
     * @throws NullPointerException if the supplier is called and returns {@code null}.
     * @see #or(Either)
     * @see #and(Either)
     * @see #and(Supplier)
     */
    @NotNull <L2> Either<L2, R> or(@NotNull Supplier<Either<L2, R>> nextEitherSupplier);


    /**
     * Return the {@link Right} value if this is a {@link Right} {@link Either} without invoking the {@link Supplier}.
     * Otherwise, throw the supplied {@link Exception}.
     * <p>
     * If the supplied {@link Exception} extends {@link RuntimeException}, this method does not
     * need to be enclosed within a {@code try } block.
     * </p>
     *
     * @param supplier {@link Exception} {@link Supplier}
     * @param <X>      {@link Exception}
     * @return {@link Right} value, if present
     * @throws X                    the supplied {@link Exception}
     * @throws NullPointerException if the called Supplier returns {@code null}.
     */
    @NotNull <X extends Throwable> R expect(@NotNull Supplier<X> supplier) throws X;


    /**
     * Return the {@link Right} value if this is a {@link Right} {@link Either} without invoking the {@link Function}
     * supplying the exception.
     * <p>
     * Otherwise, invoke the {@link Function} to produce the {@link Exception}.
     * </p>
     * <p>
     * If the produced {@link Exception} extends {@link RuntimeException}, this method does not
     * need to be enclosed within a {@code try } block.
     * </p>
     * <p>
     * This method is particularly useful to convert Exception types, or to create Exceptions with
     * error messages dependent upon the {@link Left} value.
     * </p>
     * <p>
     * For example:
     * </p>
     * <pre>
     *     {@code
     *              Either<String,Integer> int anExampleMethod(int input) {
     *                  if(input > 100) {
     *                     return Either.ofRight(input * 10);
     *                  } else {
     *                     return Either.ofLeft("input too small");
     *                  }
     *              }
     *
     *              Either<String,Integer> myEitherGood = anExampleMethod(1000);
     *              int goodValue = myEitherGood.getOrThrowWrapped(ArithmeticException::new);
     *              // goodValue = 10000; no exception thrown
     *
     *              // regarding getOrThrowWrapped(ArithmeticException::new)
     *              //     this works because there is an ArithmeticException(String) constructor;
     *              //     otherwise a compile-time error would occur (no appropriate constructor to match)
     *
     *              Either<String,Integer> myEitherBad = anExampleMethod(-37234);
     *              int badValue = myEitherGood.getOrThrowWrapped(ArithmeticException::new);
     *              // throws a new ArithmeticException("input too small")
     *     }
     * </pre>
     *
     * @param exFn {@link Exception} producing {@link Function}
     * @param <X>  {@link Exception}
     * @return {@link Right} value, if present
     * @throws X                    the produced {@link Exception}
     * @throws NullPointerException if the called Function returns {@code null}.
     */
    @NotNull <X extends Throwable> R getOrThrow(@NotNull Function<L, X> exFn) throws X;


    /**
     * Create a new {@link Either} with the left and right values transposed.
     * <p>
     * This can be used to effectively change the bias.
     * </p>
     * <p>
     * For example, the following are equivalent:
     * </p>
     * {@snippet :
     *      myEither.flatMapLeft(MyFunction::doIt) == myEither.swap().flatMap(MyFunction::doIt);
     *}
     *
     * @return A new {@link Either} with left and right values swapped.
     */

    @NotNull Either<R, L> swap();


    /**
     * Implementation of a Left {@link Either}.
     *
     * @param value Left value. May not be {@code null}.
     * @param <L>   parameter type of Left values.
     * @param <R>   parameter type of Right values.
     */
    record Left<L, R>(@NotNull L value) implements Either<L, R> {


        /**
         * Create a Left value.
         */
        public Left {
            requireNonNull( value, "Left: value cannot be null!" );
        }


        /**
         * Get the value. Never null.
         */
        @NotNull
        public L get() {return value;}


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
        public <L2, R2> @NotNull Either<L2, R2> biMap(@NotNull Function<? super L, ? extends L2> fnLeft, @NotNull Function<? super R, ? extends R2> fnRight) {
            requireNonNull( fnLeft );
            requireNonNull( fnRight );
            return Either.ofLeft( fnLeft.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public <L2, R2> @NotNull Either<L2, R2> biFlatMap(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R2>> fnLeft, @NotNull Function<? super R, ? extends Either<? extends L2, ? extends R2>> fnRight) {
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
        public boolean ifPredicate(@NotNull Predicate<R> rp) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean ifPredicateLeft(@NotNull Predicate<L> lp) {
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
            return coerce();
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
        public @NotNull L forfeit(@NotNull Function<? super R, ? extends L> fn) {
            requireNonNull( fn );
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull R recover(@NotNull Function<? super L, ? extends R> fn) {
            requireNonNull( fn );
            return requireNonNull( fn.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull <R2> Either<L, R2> map(@NotNull Function<? super R, ? extends R2> rightMapper) {
            requireNonNull( rightMapper );
            return coerce();
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <L2> Either<L2, R> mapLeft(@NotNull Function<? super L, ? extends L2> leftMapper) {
            requireNonNull( leftMapper );
            return (Either<L2, R>) Either.ofLeft( leftMapper.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Either<L, R> matchLeft(@NotNull Consumer<? super L> leftConsumer) {
            requireNonNull( leftConsumer );
            leftConsumer.accept( value );
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Either<L, R> match(@NotNull Consumer<? super R> rightConsumer) {
            requireNonNull( rightConsumer );
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull <L2> Either<L2, R> or(@NotNull Supplier<Either<L2, R>> nextEitherSupplier) {
            requireNonNull( nextEitherSupplier );
            return requireNonNull( nextEitherSupplier.get() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Either<R, L> swap() {
            return Either.ofRight( value );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <R2> @NotNull Either<L, R2> and(@NotNull Either<L, R2> nextEither) {
            requireNonNull( nextEither );
            return coerce();
        }

        /**
         * {@inheritDoc}
         */
        // more efficient than  implementation : does not invoke supplier
        @Override
        public <R2> @NotNull Either<L, R2> and(@NotNull Supplier<Either<L, R2>> nextEitherSupplier) {
            requireNonNull( nextEitherSupplier );
            return coerce();
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
        public <X extends Throwable> @NotNull R expect(@NotNull Supplier<X> supplier) throws X {
            requireNonNull( supplier );
            throw requireNonNull( supplier.get() );
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public <X extends Throwable> @NotNull R getOrThrow(@NotNull Function<L, X> exFn) throws X {
            requireNonNull( exFn );
            throw requireNonNull( exFn.apply( value ) );
        }


        /**
         * Coerce the empty Right parameter to the new type.
         * <p>
         * Does not create a new object.
         * </p>
         *
         * @param <R2> new Right parameter
         * @return coerced Left
         */
        @SuppressWarnings("unchecked")
        @NotNull
        private <R2> Either<L, R2> coerce() {
            return (Either<L, R2>) this;
        }
    }

    /**
     * Implementation of a Right {@link Either}.
     *
     * @param value Right value. May not be {@code null}.
     * @param <L>   parameter type of Left values.
     * @param <R>   parameter type of Right values.
     */
    record Right<L, R>(@NotNull R value) implements Either<L, R> {

        /**
         * Create a Right value.
         */
        public Right {
            requireNonNull( value, "Right: value cannot be null!" );
        }


        /**
         * Get the value. Never null.
         */
        @NotNull
        public R get() {return value;}


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
        public <L2, R2> @NotNull Either<L2, R2> biMap(@NotNull Function<? super L, ? extends L2> fnLeft, @NotNull Function<? super R, ? extends R2> fnRight) {
            requireNonNull( fnLeft );
            requireNonNull( fnRight );
            return Either.ofRight( fnRight.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public <L2, R2> @NotNull Either<L2, R2> biFlatMap(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R2>> fnLeft, @NotNull Function<? super R, ? extends Either<? extends L2, ? extends R2>> fnRight) {
            requireNonNull( fnLeft );
            requireNonNull( fnRight );
            return (Either<L2, R2>) requireNonNull( fnRight.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> @NotNull T fold(@NotNull Function<? super L, ? extends T> fnLeft, @NotNull Function<? super R, ? extends T> fnRight) {
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
        public boolean ifPredicate(@NotNull Predicate<R> rp) {
            return requireNonNull( rp ).test( value );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean ifPredicateLeft(@NotNull Predicate<L> lp) {
            requireNonNull( lp );
            return false;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public <L2> @NotNull Either<L2, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper) {
            requireNonNull( leftMapper );
            return coerce();
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
            return coerce();
        }

        // more efficient than  implementation : does not invoke supplier

        /**
         * {@inheritDoc}
         */
        @Override
        public <L2> @NotNull Either<L2, R> or(@NotNull Supplier<Either<L2, R>> nextEitherSupplier) {
            requireNonNull( nextEitherSupplier );
            return coerce();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull L forfeit(@NotNull Function<? super R, ? extends L> fn) {
            requireNonNull( fn );
            return requireNonNull( fn.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull R recover(@NotNull Function<? super L, ? extends R> fn) {
            requireNonNull( fn );
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull <R2> Either<L, R2> map(@NotNull Function<? super R, ? extends R2> rightMapper) {
            requireNonNull( rightMapper );
            return Either.ofRight( rightMapper.apply( value ) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull <L2> Either<L2, R> mapLeft(@NotNull Function<? super L, ? extends L2> leftMapper) {
            requireNonNull( leftMapper );
            return coerce();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Either<L, R> matchLeft(@NotNull Consumer<? super L> leftConsumer) {
            requireNonNull( leftConsumer );
            // do nothing
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Either<L, R> match(@NotNull Consumer<? super R> rightConsumer) {
            requireNonNull( rightConsumer );
            rightConsumer.accept( value );
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull <R2> Either<L, R2> and(@NotNull Supplier<Either<L, R2>> nextEitherSupplier) {
            requireNonNull( nextEitherSupplier );
            return requireNonNull( nextEitherSupplier.get() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull Either<R, L> swap() {
            return ofLeft( value );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public <X extends Throwable> R expect(@NotNull Supplier<X> supplier) throws X {
            // supplier not invoked
            requireNonNull( supplier );
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <X extends Throwable> @NotNull R getOrThrow(@NotNull Function<L, X> exFn) {
            // function not invoked
            requireNonNull( exFn );
            return value;
        }

        /**
         * Coerce the empty Left parameter to the new type.
         * <p>
         * Does not create a new object.
         * </p>
         *
         * @param <L2> new Left parameter
         * @return coerced Right
         */
        @SuppressWarnings("unchecked")
        @NotNull
        private <L2> Either<L2, R> coerce() {
            return (Either<L2, R>) this;
        }
    }
}
