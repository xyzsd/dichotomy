package net.xyzsd.dichotomy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;



public sealed interface Maybe<V> {

    static <T> Maybe<T> of(@NotNull T value) {
        return new Some<>( value );
    }

    static <T> Maybe<T> ofNone() {
        return new None<>();
    }

    static <T> Maybe<T> ofNullable(@Nullable T value) {
        return (value == null) ? Maybe.ofNone() : Maybe.of( value );
    }


    record Some<T>(@NotNull T value) implements Maybe<T> {
        public Some {
            requireNonNull( value );
        }
    }

    record None<T>() implements Maybe<T> {
        // todo : ensure all None<> are equivalent...

        @SuppressWarnings("unchecked")
        private <T2> None<T2> coerce() {
            return (None<T2>) this;
        }
    }


    default @NotNull Maybe<V> biMatch(@NotNull Consumer<? super V> okConsumer, @NotNull Runnable errRunner) {
        requireNonNull( okConsumer );
        requireNonNull( errRunner );
        switch (this) {
            case Some<V>(var v) -> okConsumer.accept( v );
            case None<V> __ -> errRunner.run();
        }
        return this;
    }


    default @NotNull Maybe<V> filter(@NotNull Predicate<? super V> predicate, @NotNull Supplier<? extends V> mapper) {
        requireNonNull( predicate );
        requireNonNull( mapper );
        return switch (this) {
            case Some<V>(var v) when predicate.test( v ) -> this;
            default -> new Some<>( mapper.get() );
        };
    }


    default <T> @NotNull T fold(@NotNull Function<? super V, ? extends T> fnOK,
                                @NotNull Supplier<? extends T> fnErr) {
        requireNonNull( fnOK );
        requireNonNull( fnErr );
        return switch (this) {
            case Some<V>(var v) -> requireNonNull( fnOK.apply( v ) );
            case None<V> __ -> requireNonNull( fnErr.get() );
        };
    }


    default @NotNull Stream<V> stream() {
        return switch (this) {
            case Some<V>(var v) -> Stream.of( v );
            case None<V> __ -> Stream.empty();
        };
    }


    default <V2> @NotNull Maybe<V2> map(@NotNull Function<? super V, ? extends V2> mapper) {
        requireNonNull( mapper );
        return switch (this) {
            case Some<V>(var v) -> new Some<>( mapper.apply( v ) ); // implicit null check
            case None<V> v -> v.coerce();
        };
    }



    @SuppressWarnings("unchecked")
    default <V2> @NotNull Maybe<V2> flatMap(@NotNull Function<? super V, ? extends Maybe<? extends V2>> mapper) {
        requireNonNull( mapper );
        return switch (this) {
            case Some<V>(var v) -> (Maybe<V2>) requireNonNull( mapper.apply( v ) );
            case None<V> v -> v.coerce();
        };
    }


    default boolean matches(@NotNull Predicate<V> predicate) {
        requireNonNull( predicate );
        return switch (this) {
            case Some<V>(var v) -> predicate.test( v );
            case None<V> __ -> false;
        };
    }


    default boolean contains(@Nullable final V value) {
        return switch (this) {
            case Some<V>(var v) -> Objects.equals( v, value );
            case None<V> __ -> false;
        };
    }


    default @NotNull V orElse(@NotNull V alternate) {
        requireNonNull( alternate );
        return switch (this) {
            case Some<V>(var v) -> v;
            case None<V> __ -> alternate;
        };
    }


    default @NotNull V orElseGet(@NotNull Supplier<? extends V> supplier) {
        requireNonNull( supplier );
        return switch (this) {
            case Some<V>(var v) -> v;
            case None<V> __ -> requireNonNull( supplier.get() );
        };
    }


    @SuppressWarnings("unchecked")
    default <V2> @NotNull Maybe<V2> and(@NotNull Maybe<? extends V2> nextSome) {
        requireNonNull( nextSome );
        return switch (this) {
            case Some<V> __ -> (Maybe<V2>) nextSome;
            case None<V> v -> v.coerce();
        };
    }


    @SuppressWarnings("unchecked")
    default <V2> @NotNull Maybe<V2> and(@NotNull Supplier<Maybe<? extends V2>> nextSomeSupplier) {
        requireNonNull( nextSomeSupplier );
        return switch (this) {
            case Some<V> __ -> requireNonNull( (Maybe<V2>) nextSomeSupplier.get() );
            case None<V> v -> v.coerce();
        };
    }


    default @NotNull Maybe<V> or(@NotNull Maybe<V> nextSome) {
        requireNonNull( nextSome );
        return switch (this) {
            case Some<V> __ -> this;
            case None<V> v -> nextSome;
        };
    }


    default @NotNull Maybe<V> or(@NotNull Supplier<Maybe<V>> nextSomeSupplier) {
        requireNonNull( nextSomeSupplier );
        return switch (this) {
            case Some<V> __ -> this;
            case None<V> v -> requireNonNull( nextSomeSupplier.get() );
        };
    }


    default @NotNull V expect() {
        return switch (this) {
            case Some<V>(var v) -> v;
            case None<V> v -> throw new NoSuchElementException();
        };
    }


    @NotNull
    default <X extends Throwable> V orThrow(@NotNull Supplier<X> supplier) throws X {
        requireNonNull( supplier );
        return switch (this) {
            case Some<V>(var v) -> v;
            case None<V> v -> throw requireNonNull( supplier.get() );
        };
    }

}
