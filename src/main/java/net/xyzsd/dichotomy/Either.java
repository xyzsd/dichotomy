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
 * (todo: better explanation and examples)
 *
 * <p>
 * (todo: explanation of right-bias)
 *
 * @param <L> The left-hand value (by convention, this is the failure/unsuccessful value)
 * @param <R> The right-hand value (by convention, the success value)
 */
// opinionated : no null for L or R; cannot use null (Void types not allowed; use 'None' instead)
// sealed: can use in switch()
// biased: right (typically error value in left) HOWEVER we do have xxxErr methods to operate on the left side
// separable: everythign is defined in a single file.

public sealed interface Either<L, R> permits Either.Left, Either.Right {

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
    @NotNull
    default Optional<L> left() {
        return switch (this) {
            case Left(L l) -> Optional.of( l );
            case Right(var __) -> Optional.empty();
        };
    }

    /**
     * If the {@link Either} is {@link Right}, return the value as an {@link Optional}.
     * Otherwise, return an empty {@link Optional}.
     */
    @NotNull
    default Optional<R> right() {
        return switch (this) {
            case Right(R r) -> Optional.of( r );
            case Left(var __) -> Optional.empty();
        };
    }




    /**
     * Executes the action for the {@link Left} or {@link Right} depending upon
     * the value of this {@link Either}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #matchLeft(Consumer)
     */
    @NotNull
    default Either<L, R> biMatch(@NotNull Consumer<? super L> leftConsumer,
                                 @NotNull Consumer<? super R> rightConsumer) {
        requireNonNull( leftConsumer );
        requireNonNull( rightConsumer );

        switch (this) {
            case Left(L l) -> leftConsumer.accept( l );
            case Right(R r) -> rightConsumer.accept( r );
        }

        return this;
    }


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
    @NotNull
    default <L2, R2> Either<L2, R2> biMap(@NotNull Function<? super L, ? extends L2> fnLeft,
                                          @NotNull Function<? super R, ? extends R2> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );

        return switch (this) {
            case Left(L l) -> ofLeft( fnLeft.apply( l ) );
            case Right(R r) -> ofRight( fnRight.apply( r ) );
        };
    }


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
    @NotNull
    @SuppressWarnings("unchecked")
    default <L2, R2> Either<L2, R2> biFlatMap(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R2>> fnLeft,
                                              @NotNull Function<? super R, ? extends Either<? extends L2, ? extends R2>> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );

        return switch (this) {
            case Left(L l) -> (Either<L2, R2>) requireNonNull( fnLeft.apply( l ) );
            case Right(R r) -> (Either<L2, R2>) requireNonNull( fnRight.apply( r ) );
        };
    }


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
    @NotNull
    default <T> T fold(@NotNull Function<? super L, ? extends T> fnLeft,
                       @NotNull Function<? super R, ? extends T> fnRight) {
        requireNonNull( fnLeft );
        requireNonNull( fnRight );

        return switch (this) {
            case Left(L l) -> requireNonNull( fnLeft.apply( l ) );
            case Right(R r) -> requireNonNull( fnRight.apply( r ) );

        };
    }


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
    @NotNull
    default Either<L, R> filter(@NotNull Predicate<? super R> predicate,
                                @NotNull Function<? super R, ? extends L> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );

        return switch (this) {
            case Left(L l) -> this;
            case Right(R r) -> predicate.test( r )
                    ? this
                    : Either.ofLeft( mapper.apply( r ) ); // implicit null check
        };
    }


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
    @NotNull
    default L forfeit(@NotNull Function<? super R, ? extends L> fn) {
        return fold( Function.identity(), fn );
    }


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
    @NotNull
    default R recover(@NotNull Function<? super L, ? extends R> fn) {
        return fold( fn, Function.identity() );
    }


    /**
     * Return a {@link Stream}, containing either a single {@link Right} value, or an empty {@link Stream}
     * if this is a {@link Left value}.
     *
     * @return Stream
     * @see #streamLeft()
     */
    @NotNull
    default Stream<R> stream() {
        return switch (this) {
            case Left(L l) -> Stream.empty();
            case Right(R r) -> Stream.of( r );
        };
    }

    /**
     * Return a {@link Stream}, containing either a single {@link Left} value, or an empty {@link Stream}
     * if this is a {@link Right value}.
     *
     * @see #stream()
     */
    @NotNull
    default Stream<L> streamLeft() {
        return switch (this) {
            case Left(L l) -> Stream.of( l );
            case Right(R r) -> Stream.empty();
        };
    }

    /**
     * Determines if this {@link Right} {@link Either} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param rVal value to compare
     * @return {@code true} iff this is a {@link Right} {@link Either} and the contained value equals {@code rVal}
     * @see #matches
     * @see #containsLeft
     * @see #matchesLeft
     */
    default boolean contains(@Nullable R rVal) {
        return switch (this) {
            case Left(L l) -> false;
            case Right(R r) -> r.equals( rVal );
        };
    }

    /**
     * Determines if this {@link Left} {@link Either} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param lVal value to compare
     * @return {@code true} iff this is a {@link Left} {@link Either} and the contained value equals {@code lVal}
     * @see #matchesLeft
     * @see #matches
     * @see #contains
     */
    default boolean containsLeft(@Nullable L lVal) {
        return switch (this) {
            case Left(L l) -> l.equals( lVal );
            case Right(R r) -> false;
        };
    }

    /**
     * Determines if this {@link Right} {@link Either} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not tested if this is a {@link Left} {@link Either}
     * </p>
     *
     * @param rp the {@link Predicate} to test
     * @return {@code true} iff this is a {@link Right} {@link Either} and the {@link Predicate} matches.
     * @see #contains
     * @see #matchesLeft
     * @see #containsLeft
     */
    default boolean matches(@NotNull Predicate<R> rp) {
        requireNonNull( rp );

        return switch (this) {
            case Left(L l) -> false;
            case Right(R r) -> rp.test( r );
        };
    }

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
     * @see #matches
     */
    default boolean matchesLeft(@NotNull Predicate<L> lp) {
        requireNonNull( lp );

        return switch (this) {
            case Left(L l) -> lp.test( l );
            case Right(R r) -> false;
        };
    }

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
    @NotNull
    default <R2> Either<L, R2> map(@NotNull Function<? super R, ? extends R2> rightMapper) {
        return biMap( Function.identity(), rightMapper );
    }

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
    @SuppressWarnings("unchecked")
    @NotNull
    default <R2> Either<L, R2> flatMap(@NotNull Function<? super R, ? extends
            Either<? extends L, ? extends R2>> rightMapper) {
        requireNonNull( rightMapper );

        return switch (this) {
            case Left(L l) -> (Left<L, R2>) this;    // coerce right
            case Right(R r) -> (Either<L, R2>) requireNonNull( rightMapper.apply( r ) );
        };
    }


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
    @NotNull
    default <L2> Either<L2, R> mapLeft(@NotNull Function<? super L, ? extends L2> leftMapper) {
        return biMap( leftMapper, Function.identity() );
    }

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
    @SuppressWarnings("unchecked")
    @NotNull
    default <L2> Either<L2, R> flatMapLeft(@NotNull Function<? super L, ? extends
            Either<? extends L2, ? extends R>> leftMapper) {
        requireNonNull( leftMapper );

        return switch (this) {
            case Left(L l) -> (Either<L2, R>) requireNonNull( leftMapper.apply( l ) );
            case Right(R r) -> (Right<L2, R>) this;    // coerce left
        };
    }


    /**
     * Executes the action iff this is a {@link Left} {@link Either}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull
    default Either<L, R> matchLeft(@NotNull Consumer<? super L> leftConsumer) {
        return biMatch( leftConsumer, (x) -> {} );
    }

    /**
     * Executes the action iff this is a {@link Right} {@link Either}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull
    default Either<L, R> match(@NotNull Consumer<? super R> rightConsumer) {
        return biMatch( (x) -> {}, rightConsumer );
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
    @NotNull
    default R orElse(@NotNull R rightAlternate) {
        requireNonNull( rightAlternate );

        return switch (this) {
            case Left(L l) -> rightAlternate;
            case Right(R r) -> r;
        };
    }

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
    @NotNull
    default L orElseLeft(@NotNull L leftAlternate) {
        requireNonNull( leftAlternate );
        return switch (this) {
            case Left(L l) -> l;
            case Right(R r) -> leftAlternate;
        };
    }

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
    @NotNull
    default R orElseGet(@NotNull Supplier<? extends R> rightSupplier) {
        requireNonNull( rightSupplier );

        return switch (this) {
            case Left(L l) -> rightSupplier.get();
            case Right(R r) -> r;
        };
    }

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
    @NotNull
    default L orElseGetLeft(@NotNull Supplier<? extends L> leftSupplier) {
        requireNonNull( leftSupplier );

        return switch (this) {
            case Left(L l) -> l;
            case Right(R r) -> leftSupplier.get();
        };
    }


    /**
     * If {@code this} is {@link Left}, return it. Otherwise, return the next {@link Either} given.
     * The next Either can have a different parameterized Right type.
     *
     * @param nextEither The {@link Either} to return.
     * @see #and(Supplier)
     * @see #or(Either)
     * @see #or(Supplier)
     */
    @NotNull
    default <R2> Either<L, R2> and(@NotNull Either<L, R2> nextEither) {
        requireNonNull( nextEither );

        return switch (this) {
            case Left(L l) -> ((Left<L, R>) this).coerce();
            case Right(R r) -> nextEither;
        };
    }

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
    @NotNull
    default <R2> Either<L, R2> and(@NotNull Supplier<Either<L, R2>> nextEitherSupplier) {
        requireNonNull( nextEitherSupplier );

        return switch (this) {
            case Left(L l) -> ((Left<L, R>) this).coerce();
            case Right(R r) -> requireNonNull( nextEitherSupplier.get() );
        };
    }


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
    @NotNull
    default <L2> Either<L2, R> or(@NotNull Either<L2, R> nextEither) {
        requireNonNull( nextEither );

        return switch (this) {
            case Left(L l) -> nextEither;
            case Right(R r) -> ((Right<L, R>) this).coerce();
        };
    }


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
    @NotNull
    default <L2> Either<L2, R> or(@NotNull Supplier<Either<L2, R>> nextEitherSupplier) {
        requireNonNull( nextEitherSupplier );

        return switch (this) {
            case Left(L l) -> requireNonNull( nextEitherSupplier.get() );
            case Right(R r) -> ((Right<L, R>) this).coerce();
        };
    }


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
    @NotNull
    default <X extends Throwable> R getOrThrow(@NotNull Supplier<X> supplier) throws X {
        requireNonNull( supplier );

        return switch (this) {
            case Left(L l) -> throw supplier.get();
            case Right(R r) -> r;
        };
    }


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
    @NotNull
    default <X extends Throwable> R getOrThrowWrapped(@NotNull Function<L, X> exFn) throws X {
        requireNonNull( exFn );

        return switch (this) {
            case Left(L l) -> throw exFn.apply( l );
            case Right(R r) -> r;
        };
    }


    /**
     * Create a new {@link Either} with the left and right values transposed.
     * <p>
     * This can be used to effectively change the bias.
     * </p>
     * <p>
     * For example, the following are equivalent:
     * </p>
     * {@snippet :
     *      myEither.flatMapLeft(MyFunction::doit) == myEither.swap().flatMap(MyFunction::doit);
     *}
     *
     * @return A new {@link Either} with left and right values swapped.
     */
    default
    @NotNull Either<R, L> swap() {
        // optimized; could use biFlatMap( Either::ofRight, Either::ofLeft );
        return switch (this) {
            case Left(L l) -> Either.ofRight( l );
            case Right(R r) -> Either.ofLeft( r );
        };
    }


    /**
     * Implementation of a Left {@link Either}.
     *
     * @param value Left value. May not be {@code null}.
     * @param <L> parameter type of Left values.
     * @param <R> parameter type of Right values.
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
        public L get() { return value; }

        /**
         * Coerce the empty Right parameter to the new type.
         * <p>
         *     Does not create a new object.
         * </p>
         *
         * @return coerced Left
         * @param <R2> new Right parameter
         */    @SuppressWarnings("unchecked")
        @NotNull
        public <R2> Either<L, R2> coerce() {
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
        public R get() { return value; }


        /**
         * Coerce the empty Left parameter to the new type.
         * <p>
         *     Does not create a new object.
         * </p>
         *
         * @return coerced Right
         * @param <L2> new Left parameter
         */
        @SuppressWarnings("unchecked")
        @NotNull
        public <L2> Either<L2, R> coerce() {
            return (Either<L2, R>) this;
        }
    }
}
