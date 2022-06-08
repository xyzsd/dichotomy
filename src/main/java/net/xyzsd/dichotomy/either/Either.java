package net.xyzsd.dichotomy.either;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.xyzsd.dichotomy.Box;
import net.xyzsd.dichotomy.result.Result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


// opinionated : no null for L or R
// sealed: can use in switch()
// biased: right (typically error value in left)
// note: cannot use null (Void types not allowed; use 'None' instead)
public sealed interface Either<L, R> extends Box<L, R> permits Left, Right {


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
     * Create an {@link Either} from a {@link Result}.
     */
    @NotNull
    static <L, R> Either<L, R> fromResult(@NotNull Result<R, L> result) {
        return result.fold( Either::ofRight, Either::ofLeft );
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
     * Return the value contained by this {@link Either}. This may be a {@link Left} value or
     * a {@link Right} value, but is never {@code null}.
     */
    @NotNull Object unwrap();


    /**
     * True if the {@link Either} is {@link Left}.
     */
    boolean isLeft();

    /**
     * True if the {@link Either} is {@link Right}.
     */
    boolean isRight();


    /**
     * Executes the action for the {@link Left} or {@link Right} depending upon
     * the value of this {@link Either}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #matchLeft(Consumer)
     */
    @NotNull Either<L, R> biMatch(@NotNull Consumer<? super L> leftConsumer,
                                  @NotNull Consumer<? super R> rightConsumer);


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
    @NotNull <L2, R2> Either<L2, R2> biMap(@NotNull Function<? super L, ? extends L2> fnLeft,
                                           @NotNull Function<? super R, ? extends R2> fnRight);


    /**
     * Returns an {@link Either}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced Either (which may be {@link Left} or {@link Right}) can have different types.
     * </p>
     *
     * @param fnLeft  the mapping function for {@link Left} values.
     * @param fnRight the mapping function for {@link Right} values.
     * @return the {@link Either} produced from {@code fnLeft} or {@code fnRight}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapLeft(Function)
     */
    @NotNull <L2, R2> Either<L2, R2> biFlatMap(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R2>> fnLeft,
                                               @NotNull Function<? super R, ? extends Either<? extends L2, ? extends R2>> fnRight);


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
     * @return the value produced from {@code fnLeft} or {@code fnRight}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #recover(Function)
     * @see #forfeit(Function)
     */
    @NotNull <T> T fold(@NotNull Function<? super L, ? extends T> fnLeft,
                        @NotNull Function<? super R, ? extends T> fnRight);


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
    @NotNull Either<L, R> filter(@NotNull Predicate<? super R> predicate,
                                 @NotNull Function<? super R, ? extends L> mapper);


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
     * @return A {@link Left} value (either the current {@link Left} if present, or the produced {@link Left} if not.
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
     * @return A {@link Right} value (either the current {@link Right} if present, or the produced {@link Right} if not.
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
     * @see #matches
     * @see #containsLeft
     * @see #matchesLeft
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
     * @see #matchesLeft
     * @see #matches
     * @see #contains
     */
    boolean containsLeft(@Nullable L lVal);

    /**
     * Determines if this {@link Right} {@link Either} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is a {@link Left} {@link Either}
     * </p>
     *
     * @param rp the {@link Predicate} to test
     * @return {@code true} iff this is a {@link Right} {@link Either} and the {@link Predicate} matches.
     * @see #contains
     * @see #matchesLeft
     * @see #containsLeft
     */
    boolean matches(@NotNull Predicate<R> rp);

    /**
     * Determines if this {@link Left} {@link Either} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is a {@link Right} {@link Either}
     * </p>
     *
     * @param lp the {@link Predicate} to test
     * @return {@code true} iff this is a {@link Left} {@link Either} and the {@link Predicate} matches.
     * @see #containsLeft
     * @see #contains
     * @see #matches
     */
    boolean matchesLeft(@NotNull Predicate<L> lp);


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
    // aka join; if left present, apply mapping function otherwise return this (right)
    @NotNull <L2> Either<L2, R> flatMapLeft(@NotNull Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper);


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
     * If {@code this} is {@link Left}, return it (without invoking the {@link Supplier}.
     * Otherwise, return the next {@link Either} supplied.
     * The next Either can have a different parameterized Right type.
     *
     * @param nextEitherSupplier The supplier of an {@link Either} to return; only called if {@code this} is {@link Right}.
     * @throws NullPointerException if the supplied {@link Either} is {@code null}.
     * @see #and(Either)
     * @see #or(Either)
     * @see #or(Supplier)
     */
    @NotNull
    default <R2> Either<L, R2> and(@NotNull Supplier<Either<L, R2>> nextEitherSupplier) {
        requireNonNull( nextEitherSupplier );
        return and( nextEitherSupplier.get() );
    }


    /**
     * If {@code this} is {@link Right}, return it.
     * Otherwise, return the next {@link Either} given.
     * The next Either can have a different parameterized Left type.
     *
     * @param nextEither The {@link Either} to return.
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
     * @throws NullPointerException if the supplier is called and returns {@code null}.
     * @see #or(Either)
     * @see #and(Either)
     * @see #and(Supplier)
     */
    @NotNull
    default <L2> Either<L2, R> or(@NotNull Supplier<Either<L2, R>> nextEitherSupplier) {
        requireNonNull( nextEitherSupplier );
        return or( nextEitherSupplier.get() );
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
    @NotNull <X extends Throwable> R getOrThrow(@NotNull Supplier<X> supplier) throws X;


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
    @NotNull <X extends Throwable> R getOrThrowWrapped(@NotNull Function<L, X> exFn) throws X;


    /**
     * Create a new {@link Either} with the left and right values transposed.
     * <p>
     * This can be used to effectively change the bias.
     * </p>
     * <p>
     * For example, the following are equivalent:
     * </p>
     * <pre>
     *         {@code myEither.flatMapLeft(MyFunction::doit) == myEither.swap().flatMap(MyFunction::doit);}
     *     </pre>
     *
     * @return A new {@link Either} with left and right values swapped.
     */
    @Override
    default @NotNull Either<R, L> swap() {
        return biFlatMap( Either::ofRight, Either::ofLeft );
    }

    /**
     * Create a new {@code Result} from the given {@link Either}.
     *
     * @return a new {@code Result}, equivalent to this {@link Either}.
     */
    default @NotNull Result<R, L> toResult() {
        return fold( Result::ofErr, Result::ofOK );
    }

}
