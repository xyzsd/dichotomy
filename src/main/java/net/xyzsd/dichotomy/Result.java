package net.xyzsd.dichotomy;

import net.xyzsd.dichotomy.trying.Try;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


/**
 * A {@code Result} type.
 * <p>
 * A {@code Result} is typically the return value of a method, and can be only one of two values,
 * each of which can have its own non-null type.
 * <ul>
 *     <li>OK: Success</li>
 *     <li>Err: Failure (not required to be an {@link Exception})</li>
 * </ul>
 * If there is no associated type, use the {@link Empty} type rather than {@link Void}.
 * <p>
 * For example:
 * {@snippet :
 *
 *      Result<String,Integer> myMethod(String input) {
 *           if ("hello".equals(input) ) {
 *              return Result.ofOK("Hello!");
 *           }
 *           return Result.ofErr(666);
 *      }
 *
 *      ...
 *
 *      Result<String,Integer> result = myMethod("hello");
 *
 *      switch(result) {
 *          case OK success -> System.out.println(success.get());
 *          case Err err -> throw new IllegalArgumentException("ERROR CODE: "+err.get());
 *      }
 *}
 * Or with patterns, using the above {@code Result}:
 * {@snippet :
 *
 *     switch(result) {
 *         case OK(String s) -> System.out.printf("Success! %s\n", s);
 *         case Err(int i) -> System.err.printf("ERROR: %d",i);
 *     }
 *}
 * <p>
 * NOTE: For methods which accept or return type parameters {@code <V2>} or {@code <E2>},
 * {@code <V2>} or {@code <E2>} parameters can be completely different types from {@code <V>} or {@code <E>}.
 * <p>
 * While {@link Result}s can be used with switch() statements (particularly the pattern {@code switch} syntax),
 * they can be used in functional code by mapping, flatMapping (joining), folds, etc.
 * <p>
 * Unlike {@link Either}s, {@link Result}s have a right-bias. Therefore, methods not ending in {@code Err} will
 * operate on the {@link OK} value.
 * <p>
 * If the {@link Err} type is an {@link Exception}, then consider using a {@link Try}, which is a specialized
 * {@link Result}, and can wrap methods producing checked or unchecked {@link Exception}s.
 *
 * @param <V> The Success type.
 * @param <E> THe Failure (error) type. Does not have to be an Exception.
 */
public sealed interface Result<V, E> {


    /**
     * Create an OK (Success) Result for the given non-null value.
     *
     * @param value value
     * @param <V>   Value
     * @param <E>   Error value
     * @return OK result containing the given value.
     */
    @NotNull
    static <V, E> Result<V, E> ofOK(@NotNull V value) {
        return new OK<>( value );
    }

    /**
     * Create an OK (Success) Result for the given empty value.
     * <p>
     * All empty values use the {@link Empty} type.
     * </p>
     *
     * @param <E> Error value
     * @return OK result containing the given value.
     */
    @NotNull
    static <E> Result<Empty, E> ofOK() {
        return new OK<>( Empty.getInstance() );
    }

    /**
     * Convert a possibly-null value into a {@link Result}.
     * <p>
     * For a given value {@code <V>}:
     *     <ul>
     *     <li>{@code null} is considered failure, and becomes {@code Err<None>}.</li>
     *     <li>Non-null values are considered successful and becomes {@code OK<V>}.</li>
     *     </ul>
     * <p>
     *     If the opposite behavior is desired ({@code null} is successful, and
     *     non-{@code null} values are failure) use {@link #swap()}, as follows:
     *     <pre>
     *         {@code
     *              // Lets say RuntimeException is the error type
     *              Result<None, RuntimeException> result = Result.ofNullable(myValue).swap();
     *         }
     *     </pre>
     *
     * @param <V> Value, which may be null
     * @return A result containing {@code OK<None>} or {@code Err<E>}.
     */
    @NotNull
    static <V> Result<V, Empty> ofNullable(@Nullable V value) {
        return (value == null) ? ofErr( Empty.getInstance() ) : ofOK( value );
    }

    /**
     * Convert an {@link Optional} to a {@link Result}.
     * <p>
     * For a given {@code Optional<V>}:
     *     <ul>
     *     <li>{@link Optional#empty()} is considered failure and becomes {@code Err<None>}.</li>
     *     <li>{@code <V>} is considered successful, and becomes {@code OK<V>}.</li>
     *     </ul>
     * <p>
     *     If the opposite behavior is desired ({@code null} is successful, and
     *     non-{@code null} values are failure) use {@link #swap()}, as follows:
     *     <pre>
     *         {@code
     *              // Lets say RuntimeException is the error type
     *              Optional<RuntimeException> myValue = someFunction();
     *              Result<None, RuntimeException> result = Result.ofOK(myValue).swap();
     *         }
     *     </pre>
     *
     * @param <V> Value type
     * @return A result containing {@code OK<V>} or {@code Err<None>}.
     * @see #ofNullable(Object)
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    static <V> Result<V, Empty> from(@NotNull Optional<V> opt) {
        return opt.<Result<V, Empty>>map( Result::ofOK ).orElseGet( () -> Result.ofErr( Empty.getInstance() ) );
    }

    /**
     * Create an {@link Err} (Error/Failure) Result for the given non-null value.
     *
     * @param error Error
     * @param <V>   Success type
     * @param <E>   Error type
     * @return Result containing the given Error
     */
    @NotNull
    static <V, E> Result<V, E> ofErr(@NotNull E error) {
        return new Err<>( error );
    }

    /**
     * Get the {@link OK} value V as an {@link Optional}.
     *
     * @return return the {@link OK} value if present; otherwise, return an empty {@link Optional}.
     */
    @NotNull Optional<V> ok();

    /**
     * Get the {@link Err} value E as an {@link Optional}.
     *
     * @return return {@link Err} value if present; otherwise, return an empty {@link Optional}.
     */
    @NotNull Optional<E> err();

    /**
     * Create a new {@link Result} with values transposed.
     * <p>
     * This can be used to effectively change the bias.
     *
     * @return A new {@link Result} with {@link OK} and {@link Err} values swapped.
     */
    @NotNull Result<E, V> swap();

    /**
     * Executes the action for the {@link OK} or {@link Err} depending upon
     * the value of this {@link Result}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #matchErr(Consumer)
     */
    @NotNull Result<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer);

    /**
     * Returns a new {@link Result}, the value of which is determined by the appropriate mapping function.
     * <p>
     * The returned {@link Result} (which may be {@link OK} or {@link Err}) can have different types.
     * </p>
     *
     * @param okMapper  the mapping function for {@link OK} values.
     * @param errMapper the mapping function for {@link Err} values.
     * @return the {@link Result} produced from {@code okMapper} or {@code errMapper}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapErr(Function)
     */
    @NotNull <V2, E2> Result<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper, @NotNull Function<? super E, ? extends E2> errMapper);

    /**
     * Returns a {@link Result}, produced from one of the appropriate mapping functions.
     * <p>
     * The produced {@link Result} (which may be {@link Err} or {@link OK}) can have different types.
     * </p>
     *
     * @param fnOK  the mapping function for {@link Err} values.
     * @param fnErr the mapping function for {@link OK} values.
     * @return the {@link Result} produced from {@code fnOK} or {@code fnErr}
     * @throws NullPointerException if the called function returns {@code null}.
     * @see #map(Function)
     * @see #mapErr(Function) (Function)
     */
    @NotNull <V2, E2> Result<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E2>> fnOK, @NotNull Function<? super E, ? extends Result<? extends V2, ? extends E2>> fnErr);

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
     * Filter a {@link Result}.
     * <p>
     * If this {@link Result} is {@link Err}, return {@link Err} ({@code this}).
     * The {@code Predicate} is not tested, and the mapper {@code Function} is not executed.
     * </p>
     * <p>
     * If this {@link Result} is {@link OK}, return {@link OK} ({@code this}) if the {@code Predicate} matches.
     * If the {@code Predicate} fails to match, return an {@link Err} {@link Result} produced by applying the
     * mapping function to the current {@link Result} ({@code this}).
     * </p>
     *
     * @param predicate the predicate used to test {@link OK} values.
     * @param mapper    the mapping function for {@link OK} values that do not match the predicate.
     * @return a {@link Result} based on the algorithm described above.
     * @throws NullPointerException if the called mapping function returns {@code null}.
     */
    @NotNull Result<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper);

    /**
     * Executes the action iff this is an {@link OK} {@link Result}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull Result<V, E> match(@NotNull Consumer<? super V> okConsumer);


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
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #mapErr(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull <V2> Result<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper);

    /**
     * If this is an {@link OK}, return the new {@link Result} supplied by the mapping function.
     * Note that while the {@link Err} type must remain the same, the {@link OK} type returned
     * can be different.
     * <p>
     * This is also known as {@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link Err}, and the mapping function is not invoked.
     * </p>
     *
     * @param okMapper the mapping function that produces a new {@link Result}
     * @return a new {@link OK} produced by the mapping function if this is {@link OK};
     * otherwise, returns an {@link Err}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMapErr(Function)
     */
    @NotNull <V2> Result<V2, E> flatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper);

    /**
     * Determines if this {@link OK} {@link Result} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link Err} {@link Result}
     * </p>
     *
     * @param okPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link OK} {@link Result} and the {@link Predicate} matches.
     * @see #contains(Object)
     * @see #ifPredicateErr(Predicate)
     * @see #containsErr(Object)
     */
    boolean ifPredicate(@NotNull Predicate<V> okPredicate);

    /**
     * Determines if this {@link OK} {@link Result} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param okValue value to compare
     * @return {@code true} iff this is an {@link OK} {@link Result} and the contained value equals {@code okValue}
     * @see #ifPredicate(Predicate)
     * @see #containsErr(Object)
     * @see #ifPredicateErr(Predicate)
     */
    boolean contains(@Nullable V okValue);

    /**
     * If this {@link Result} is {@link Err}, return {@code rightAlternate}.
     * Otherwise, return {@code this} (an {@link OK} {@link Result}).
     *
     * @param okAlternate alternate {@link OK} {@link Result}
     * @return {@code this}, or {@code okAlternate} if {@code this} is {@link Err}
     * @see #orElse(Supplier)
     * @see #orElseErr(Object)
     * @see #orElseErr(Supplier)
     */
    @NotNull V orElse(@NotNull V okAlternate);

    /**
     * If this {@link Result} is {@link Err}, return the supplied {@link OK} {@link Result}.
     * Otherwise, return {@code this} (an {@link OK} {@link Result}) without
     * invoking the {@link Supplier}.
     *
     * @param okSupplier supplier of {@link OK} {@link Result}s
     * @return {@code this}, or the supplied {@link OK} {@link Result} if {@code this} is {@link Err}
     * @see #orElse(Object)
     * @see #orElseErr(Object)
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
     * @throws NullPointerException if the result of the mapping function is {@code null}.
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
     * Executes the action iff this is an {@link Err} {@link Result}.
     *
     * @return {@code this}
     * @throws NullPointerException if the called action returns {@code null}.
     * @see #match(Consumer)
     * @see #biMatch(Consumer, Consumer)
     */
    @NotNull Result<V, E> matchErr(@NotNull Consumer<? super E> errConsumer);

    /**
     * If this is an {@link Err}, return a new {@link Err} value produced by the given mapping function.
     * Otherwise, return the {@link OK} value.
     * <p>
     * The type of the produced {@link Err} can be different. The mapping function is only invoked for
     * {@link Err} values.
     * </p>
     * <p>
     * This is equivalent to:
     * {@snippet :
     *      // given:
     *      Result<String,Integer> result = Result<>.of(5);
     *      Function<Integer,Double> mapper = (i) -> 10.0d * i;
     *
     *      // newResult : 50.0d
     *      Result<String,Double> newResult = result.mapErr(mapper);
     *
     *      // equivalent
     *      newResult = either.map(mapper, Function.identity());
     *
     *      // also equivalent:
     *      newResult = result.swap()     // Result&lt;Integer,String&gt;
     *            .map(mapper)            // Result&lt;Double,String&gt;
     *            .swap();                // Result&lt;String,Double&gt;
     *}
     *
     * @param errMapper the mapping function producing a new {@link Err} value.
     * @return a new {@link Err} produced by the mapping function if this is {@link Err};
     * otherwise, returns an {@link OK}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #map(Function)
     * @see #biMap(Function, Function)
     */
    @NotNull <E2> Result<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper);

    /**
     * If this is an {@link Err}, return the new {@link Result} supplied by the mapping function.
     * Note that while the {@link OK} type must remain the same, the {@link Err} type returned
     * can be different.
     * <p>
     * This is also known as a left-{@code join()} in other implementations.
     * </p>
     * <p>
     * No mapping is performed if this is an {@link OK}, and the mapping function is not invoked.
     * </p>
     *
     * @param errMapper the mapping function that produces a new {@link Result}
     * @return a new {@link Err} produced by the mapping function if this is {@link Err};
     * otherwise, returns an {@link OK}.
     * @throws NullPointerException if the result of the mapping function is {@code null}
     * @see #biFlatMap(Function, Function)
     * @see #flatMap(Function)
     */
    @NotNull <E2> Result<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper);

    /**
     * Determines if this {@link Err} {@link Result} matches the given {@link Predicate}.
     * <p>
     * The {@link Predicate} is not invoked if this is an {@link OK} {@link Result}
     * </p>
     *
     * @param errPredicate the {@link Predicate} to test
     * @return {@code true} iff this is an {@link Err} {@link Result} and the {@link Predicate} matches.
     * @see #containsErr(Object)
     * @see #contains(Object)
     * @see #ifPredicate(Predicate)
     */
    boolean ifPredicateErr(@NotNull Predicate<E> errPredicate);

    /**
     * Determines if this {@link Err} {@link Result} contains the given value.
     * <p>
     * This will always return {@code false} for {@code null} values.
     * </p>
     *
     * @param errValue value to compare
     * @return {@code true} iff this is an {@link Err} {@link Result} and the contained value equals {@code errValue}
     * @see #ifPredicateErr
     * @see #ifPredicate
     * @see #contains
     */
    boolean containsErr(@Nullable E errValue);

    /**
     * If this {@link Result} is an {@link OK}, return {@code errAlternate}.
     * Otherwise, return {@code this} (an {@link Err} {@link Result}).
     *
     * @param errAlternate alternate {@link Err} {@link Result}
     * @return {@code this}, or {@code leftAlternate} if {@code this} is an {@link OK}
     * @see #orElseErr(Object)
     * @see #orElse(Object)
     * @see #orElse(Supplier)
     */
    @NotNull E orElseErr(@NotNull E errAlternate);

    /**
     * If this {@link Result} is an {@link OK}, return the supplied {@link Err}  {@link Result}.
     * Otherwise, return {@code this} (an {@link Err} {@link Result}) without
     * invoking the {@link Supplier}.
     *
     * @param errSupplier supplier of {@link Err} {@link Result}s
     * @return {@code this}, or the supplied {@link Err} {@link Result} if {@code this} is {@link OK}
     * @see #orElse(Object)
     * @see #orElseErr(Object)
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
     * @throws NullPointerException if the result of the mapping function is {@code null}.
     * @see #recover(Function)
     */
    @NotNull E forfeit(@NotNull Function<? super V, ? extends E> fnV2E);

    /**
     * If {@code this} is {@link Err}, return it. Otherwise, return the next {@link Result} given.
     * The next {@link Result} can have a different parameterized {@link OK} type.
     *
     * @param nextResult The {@link Result} to return.
     * @see #and(Supplier)
     * @see #or(Result)
     * @see #or(Supplier)
     */
    @NotNull <V2> Result<V2, E> and(@NotNull Result<V2, E> nextResult);

    /**
     * If {@code this} is {@link Err}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Result} supplied.
     * The next {@link Result} can have a different parameterized {@link OK} type.
     *
     * @param nextResultSupplier The supplier of a {@link Result} to return; only called if {@code this} is {@link OK}.
     * @throws NullPointerException if the supplied {@link Result} is {@code null}.
     * @see #and(Result)
     * @see #or(Result)
     * @see #or(Supplier)
     */
    @NotNull <V2> Result<V2, E> and(@NotNull Supplier<Result<V2, E>> nextResultSupplier);

    /**
     * If {@code this} is {@link OK}, return it.
     * Otherwise, return the next {@link Result} given.
     * The next {@link Result}  can have a different parameterized {@link Err} type.
     *
     * @param nextResult The {@link Result} to return.
     * @see #or(Supplier)
     * @see #and(Result)
     * @see #and(Supplier)
     */
    @NotNull <E2> Result<V, E2> or(@NotNull Result<V, E2> nextResult);

    /**
     * If {@code this} is {@link OK}, return it (without invoking the {@link Supplier}).
     * Otherwise, return the next {@link Result} supplied.
     * The next {@link Result} can have a different parameterized {@link Err} type.
     *
     * @param nextResultSupplier The supplier of a {@link Result} to return; only called if {@code this} is {@link Err}.
     * @throws NullPointerException if the supplier is called and returns {@code null}.
     * @see #or(Result)
     * @see #and(Result)
     * @see #and(Supplier)
     */
    @NotNull <E2> Result<V, E2> or(@NotNull Supplier<Result<V, E2>> nextResultSupplier);

    /**
     * Expect success (an {@link OK} value), otherwise throw a <strong>runtime</strong> Exception.
     * <p>
     * This always will throw for {@link Err}. What is thrown depends
     * upon the {@link Err} type:
     * <ul>
     *     <li>If {@link Err} extends {@link RuntimeException},
     *         the RuntimeException is thrown as-is, without wrapping into a {@link NoSuchElementException}</li>
     *     <li>If {@link Err} extends {@link Exception} (but not a subclass of {@link RuntimeException})
     *         it is wrapped in a {@link NoSuchElementException} and then thrown</li>
     *     <li>If {@link Err} is any other type,
     *         it is converted to a {@link String} using {@code String.valueOf()},
     *         then wrapped in a {@link NoSuchElementException} and thrown</li>
     * </ul>
     *
     * @return Value (if {@link OK}) or throws a {@link RuntimeException} (if {@link Err})
     * @throws RuntimeException as detailed above.
     */
    @NotNull V expect() throws RuntimeException;

    /**
     * Throw the given supplied {@link Exception}
     * <p>
     * This method can wrap an {@link Err} type if that {@link Err} type is allowed by the constructor.
     * For example, an {@code IOException::new} could wrap an {@code Exception} or {@code String} type.
     * </p>
     * <pre>
     * {@code
     *      Result<File,String> fileResult = findFile(...);
     *      // fileResult = Err<String> "cannot find the file"
     *
     *      fileResult.expect();        // throws a ResultException("cannot find the file")
     *
     *      // throws a RuntimeException("cannot find the file"); does not have to be caught
     *      fileResult.orThrowWrapped(RuntimeException::new);
     *
     *      // throws a FileNotFoundException("cannot find the file"), which *must* be caught
     *      fileResult.orThrowWrapped(FileNotFoundException::new);
     *
     *      // the following line will not compile, since there is no AnnotationTypeMismatchException(String) constructor
     *      fileResult.orThrowWrapped(AnnotationTypeMismatchException::new);
     *
     *      // to contrast with orThrow():
     *      // this will throw FileNotFoundException() without setting a message.
     *      fileResult.orThrow(FileNotFoundException::new);
     *
     *
     *      try {
     *          // throws FileNotFoundException("cannot find the file")
     *          fileResult.orThrowWrapped(FileNotFoundException::new);
     *      } catch(IOException e) {
     *          assert "cannot find the file".equals(e.getMessage();
     *          ...
     *      }
     * }
     * </pre>
     * <p>
     * If the exception type does not wrap a given error type:
     * {@code
     * fileResult.orThrow((err) -> new MyException());
     * }
     * </p>
     *
     * @param exFn Exception producing function
     * @param <X>  Exception created by {@code exFn}
     * @return Value V
     * @throws X Exception
     * @see #getOrThrow(Supplier)
     * @see #expect()
     */
    @NotNull <X extends Exception> V getOrThrowMapped(@NotNull Function<E, X> exFn) throws X;

    /**
     * Throw the given supplied {@link Exception}
     * <p>
     * This method takes a {@link Supplier}. In many cases, the exception message or fields can be customized
     * more easily with {@link #getOrThrowMapped(Function)}.
     * </p>
     *
     * @param supplier Exception supplier
     * @param <X>      Exception created by the supplier
     * @return Value V
     * @throws X Exception
     * @see #getOrThrowMapped(Function)
     * @see #expect()
     */
    @NotNull <X extends Exception> V getOrThrow(@NotNull Supplier<X> supplier) throws X;

    /**
     * Success Result.
     *
     * @param value OK value
     * @param <V>   OK type
     * @param <E>   Error type
     */
    record OK<V, E>(@NotNull V value) implements Result<V, E> {


        /**
         * Create an OK with the given non-null value.
         *
         * @param value OK Value
         */
        public OK {
            requireNonNull( value, "OK: value cannot be null!" );
        }

        /**
         * Get the value V
         *
         * @return unwrapped value V
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
        public @NotNull Result<E, V> swap() {
            return new Err<>( value );
        }

        @Override
        public @NotNull Result<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer) {
            requireNonNull( okConsumer );
            requireNonNull( errConsumer );
            okConsumer.accept( value );
            return this;
        }

        @Override
        public @NotNull <V2, E2> Result<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper, @NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return new OK<>( okMapper.apply( value ) );
        }


        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <V2, E2> Result<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E2>> okMapper, @NotNull Function<? super E, ? extends Result<? extends V2, ? extends E2>> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return (Result<V2, E2>) requireNonNull( okMapper.apply( value ) );
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
        public @NotNull Result<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper) {
            requireNonNull( predicate );
            requireNonNull( mapper );
            if (predicate.test( value )) {
                return this;
            }
            return new Err<>( mapper.apply( value ) );
        }

        @Override
        public @NotNull Result<V, E> match(@NotNull Consumer<? super V> okConsumer) {
            requireNonNull( okConsumer );
            okConsumer.accept( value );
            return this;
        }

        @Override
        public @NotNull <V2> Result<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
            requireNonNull( okMapper );
            return new OK<>( okMapper.apply( value ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <V2> Result<V2, E> flatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper) {
            requireNonNull( okMapper );
            return (Result<V2, E>) requireNonNull( okMapper.apply( value ) );
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
        public @NotNull Result<V, E> matchErr(@NotNull Consumer<? super E> errConsumer) {
            requireNonNull( errConsumer );
            return this;
        }

        @Override
        public @NotNull <E2> Result<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( errMapper );
            return coerce();
        }

        @Override
        public @NotNull <E2> Result<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper) {
            requireNonNull( errMapper );
            return coerce();
        }

        @Override
        public boolean ifPredicateErr(@NotNull Predicate<E> errPredicate) {
            requireNonNull( errPredicate );
            return false;
        }

        @Override
        public boolean containsErr(@Nullable E errValue) {
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
        public @NotNull <V2> Result<V2, E> and(@NotNull Result<V2, E> nextResult) {
            requireNonNull( nextResult );
            return nextResult;
        }

        @Override
        public @NotNull <V2> Result<V2, E> and(@NotNull Supplier<Result<V2, E>> nextResultSupplier) {
            requireNonNull( nextResultSupplier );
            return requireNonNull( nextResultSupplier.get() );
        }

        @Override
        public @NotNull <E2> Result<V, E2> or(@NotNull Result<V, E2> nextResult) {
            requireNonNull( nextResult );
            return coerce();
        }

        @Override
        public @NotNull <E2> Result<V, E2> or(@NotNull Supplier<Result<V, E2>> nextResultSupplier) {
            requireNonNull( nextResultSupplier );
            return coerce();
        }

        @Override
        public @NotNull V expect() throws RuntimeException {
            return value;
        }

        @Override
        public <X extends Exception> @NotNull V getOrThrowMapped(@NotNull Function<E, X> exFn) {
            return value;
        }


        @Override
        public <X extends Exception> @NotNull V getOrThrow(@NotNull Supplier<X> supplier) {
            return value;
        }

        // For types where the error type is unchanged and exists, but the generic type of the value differs
        // just cast and return. Types are erased so there is no need to create a new object.
        // The value stays the same, only the empty error signature changes
        @SuppressWarnings("unchecked")
        @NotNull
        private <E2> Result<V, E2> coerce() {
            return (Result<V, E2>) this;
        }
    }

    /**
     * Error Result.
     *
     * @param error Error value
     * @param <V>   OK type
     * @param <E>   Error type
     */
    record Err<V, E>(@NotNull E error) implements Result<V, E> {

        /**
         * Create an OK with the given non-null value.
         *
         * @param error OK Value
         */
        public Err {
            requireNonNull( error, "Err: error cannot be null!" );
        }


        /**
         * Get the Err E
         *
         * @return unwrapped Err E
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
        public @NotNull Result<E, V> swap() {
            return new OK<>( error );
        }

        @Override
        public @NotNull Result<V, E> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Consumer<? super E> errConsumer) {
            requireNonNull( okConsumer );
            requireNonNull( errConsumer );
            errConsumer.accept( error );
            return this;
        }

        @Override
        public @NotNull <V2, E2> Result<V2, E2> biMap(@NotNull Function<? super V, ? extends V2> okMapper, @NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return new Err<>( errMapper.apply( error ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <V2, E2> Result<V2, E2> biFlatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E2>> okMapper, @NotNull Function<? super E, ? extends Result<? extends V2, ? extends E2>> errMapper) {
            requireNonNull( okMapper );
            requireNonNull( errMapper );
            return (Result<V2, E2>) requireNonNull( errMapper.apply( error ) );
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
        public @NotNull Result<V, E> filter(@NotNull Predicate<? super V> predicate, @NotNull Function<? super V, ? extends E> mapper) {
            requireNonNull( predicate );
            requireNonNull( mapper );
            return this;
        }

        @Override
        public @NotNull Result<V, E> match(@NotNull Consumer<? super V> okConsumer) {
            requireNonNull( okConsumer );
            return this;
        }

        @Override
        public @NotNull <V2> Result<V2, E> map(@NotNull Function<? super V, ? extends V2> okMapper) {
            requireNonNull( okMapper );
            return coerce();
        }

        @Override
        public @NotNull <V2> Result<V2, E> flatMap(@NotNull Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper) {
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
        public @NotNull Result<V, E> matchErr(@NotNull Consumer<? super E> errConsumer) {
            requireNonNull( errConsumer );
            errConsumer.accept( error );
            return this;
        }

        @Override
        public @NotNull <E2> Result<V, E2> mapErr(@NotNull Function<? super E, ? extends E2> errMapper) {
            requireNonNull( errMapper );
            return new Err<>( errMapper.apply( error ) );
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull <E2> Result<V, E2> flatMapErr(@NotNull Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper) {
            requireNonNull( errMapper );
            return (Result<V, E2>) requireNonNull( errMapper.apply( error ) );
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
        public @NotNull <V2> Result<V2, E> and(@NotNull Result<V2, E> nextResult) {
            requireNonNull( nextResult );
            return coerce();
        }

        @Override
        public @NotNull <V2> Result<V2, E> and(@NotNull Supplier<Result<V2, E>> nextResultSupplier) {
            requireNonNull( nextResultSupplier );
            return coerce();
        }

        @Override
        public @NotNull <E2> Result<V, E2> or(@NotNull Result<V, E2> nextResult) {
            requireNonNull( nextResult );
            return nextResult;
        }

        @Override
        public @NotNull <E2> Result<V, E2> or(@NotNull Supplier<Result<V, E2>> nextResultSupplier) {
            requireNonNull( nextResultSupplier );
            return requireNonNull( nextResultSupplier.get() );
        }

        @Override
        public @NotNull V expect() throws RuntimeException {
            // TODO: when pattern-switch is out of preview, convert this code
            if(error instanceof RuntimeException e) {
                throw e;
            } else if(error instanceof Throwable t) {
                throw new NoSuchElementException( t );
            } else {
                throw new NoSuchElementException( String.valueOf( error ) );
            }
        }

        @Override
        public <X extends Exception> @NotNull V getOrThrowMapped(@NotNull Function<E, X> exFn) throws X {
            requireNonNull( exFn );
            throw requireNonNull( exFn.apply( error ) );
        }

        @Override
        public <X extends Exception> @NotNull V getOrThrow(@NotNull Supplier<X> supplier) throws X {
            requireNonNull( supplier );
            throw requireNonNull( supplier.get() );
        }

        // For types where the error type is unchanged and exists, but the generic type of the value differs
        // just cast and return. Types are erased so there is no need to create a new object.
        // The Error stays the same; only the empty value signature changes
        @SuppressWarnings("unchecked")
        @NotNull
        private <V2> Result<V2, E> coerce() {
            return (Result<V2, E>) this;
        }
    }


}
