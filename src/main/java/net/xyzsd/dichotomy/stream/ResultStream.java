package net.xyzsd.dichotomy.stream;

import net.xyzsd.dichotomy.Result;
import net.xyzsd.dichotomy.gatherers.ResultGatherers;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Gatherer;
import java.util.stream.Stream;


///
/// EXPERIMENTAL PROTOTYPE IMPLEMENTATION
///
/// A specialized Stream for handling `Stream<Result>`
///
/// Most Stream interface operations are supported. The main difference between
/// a `ResultStream` and a [Stream] is that there are operations which work on
/// [Result.OK] and [Result.Err] values directly, to simplify processing.
///
/// *Implementation Note:* This wraps an underlying [Stream], delegating methods
/// as appropriate, and makes use of [java.util.stream.Gatherers] where delegation
/// does not make sense or cannot be used.
///
/// This class does not implement the Stream interface, but can be converted back
/// to a stream.
///
@NullMarked
public class ResultStream<V, E> implements AutoCloseable {

    /*
    TODO:
        a) testing! and use to refine API
        b) testing!!
        c) documentation
        d) verify all generic params
        e) code / comment cleanup
        f) performance

     */


    // we wrap a Stream, for both ease of implementation and performance
    // without Gatherers, this would be far from trivial.
    private final Stream<Result<V, E>> stream;

    ///  Constructor. No null check
    private ResultStream(final Stream<Result<V, E>> stream) {
        this.stream = stream;
    }

    ///  Create a ResultStream from a Stream of [Result] objects.
    public static <V, E> ResultStream<V, E> from(final Stream<Result<V, E>> stream) {
        return new ResultStream<>( Objects.requireNonNull( stream ) );
    }

    ///  Internal convenience method. No null check.
    ///  This is partly for safety; in case any methods return a new stream rather than the same
    ///  stream reference.
    @SuppressWarnings("unchecked")
    private <V2, E2> ResultStream<V2, E2> of(final Stream<Result<V2, E2>> in) {
        if ( (Stream<?>) stream == (Stream<?>) in) {
            return (ResultStream<V2, E2>) this;
        }

        /*
        if (this.stream.equals( in )) {
            return (ResultStream<V2, E2>) this;
        }
        */

        return new ResultStream<>( in );
    }

    ///  Converts this [ResultStream] to a standard [Stream].
    ///
    /// This returns the underlying stream, so caution is warranted. This should
    /// only be used after [ResultStream] processing (and never concurrently with
    /// [ResultStream] processing)
    public Stream<Result<V, E>> toStream() {
        return stream;
    }

    /// Returns a ResultStream consisting of [Result.OK]s, until a [Result.Err] is encountered.
    /// The [Result.Err] will be included in the stream, but no further elements will be sent downstream.
    public ResultStream<V, E> untilErr() {
        return of( stream.gather( ResultGatherers.untilErr() ) );
    }

    /// Returns a ResultStream consisting of [Result.Err], until a [Result.OK] is encountered.
    /// The [Result.OK] will be included in the stream but no further elements will be sent downstream.
    public ResultStream<V, E> untilOK() {
        return of( stream.gather( ResultGatherers.untilOK() ) );
    }

    /// Returns a stream of [Result.OK] that match the given predicate.
    /// Note that any [Result.Err], if present, will be included in the stream.
    public ResultStream<V, E> filter(final Predicate<? super V> predicate) {
        return of( stream.gather( ResultGatherers.filterOK( predicate ) ) );
    }

    /// Returns a stream of [Result.Err] that match the given predicate.
    /// Note that any [Result.OK], if present, will be included in the stream.
    public ResultStream<V, E> filterErr(final Predicate<? super E> predicate) {
        return of( stream.gather( ResultGatherers.filterErr( predicate ) ) );
    }

    ///  Swap the values in a Result. Equivalent to `stream.map(Result::swap)`
    public ResultStream<E, V> swap() {
        return of( stream.map( Result::swap ) );
    }

    ///  Map the [Result] to a new value. Do nothing for [Result.Err].
    ///  Equivalent to [Result#map(Function)]
    public <V2> ResultStream<V2, E> map(final Function<? super V, ? extends V2> okMapper) {
        return of( stream.gather( ResultGatherers.mapOK( okMapper ) ) );
    }

    ///  Map the [Result.Err] to a new value. Do nothing for [Result.OK].
    ///  Equivalent to [Result#mapErr(Function)]
    public <E2> ResultStream<V, E2> mapErr(final Function<? super E, ? extends E2> errMapper) {
        return of( stream.gather( ResultGatherers.mapErr( errMapper ) ) );
    }


    ///  flatMap the [Result.OK] to a new value. Do nothing for [Result.Err].
    ///  Equivalent to [Result#flatMap(Function)]
    public <V2> ResultStream<V2, E> flatMap(final Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper) {
        return of( stream.gather( ResultGatherers.flatMapOK( okMapper ) ) );
    }

    ///  flatMap the [Result.Err] to a new value. Do nothing for [Result.OK].
    ///  Equivalent to [Result#flatMapErr(Function)]
    public <E2> ResultStream<V, E2> flatMapErr(final Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper) {
        return of( stream.gather( ResultGatherers.flatMapErr( errMapper ) ) );
    }

    ///  Map the [Result] according to the given mapping functions.
    ///  Equivalent to [Result#biMap(Function, Function)]
    public <V2, E2> ResultStream<V2, E2> biMap(final Function<? super V, ? extends V2> okMapper,
                                               final Function<? super E, ? extends E2> errMapper) {

        return of( stream.gather( ResultGatherers.biMap( okMapper, errMapper ) ) );
    }

    ///  flatMap the [Result] according to the given mapping functions.
    ///  Equivalent to [Result#biFlatMap(Function, Function)]
    public <V2, E2> ResultStream<V2, E2> biFlatMap(final Function<? super V, ? extends Result<? extends V2, ? extends E2>> okMapper,
                                                   final Function<? super E, ? extends Result<? extends V2, ? extends E2>> errMapper) {
        return of( stream.gather( ResultGatherers.biFlatMap( okMapper, errMapper ) ) );
    }


    ///  Find the first [Result.OK], if any.
    public Optional<V> findFirst() {
        return stream.<V>mapMulti( Result::consume ).findFirst();
    }

    ///  Find the any [Result.OK].
    public Optional<V> findAny() {
        return stream.<V>mapMulti( Result::consume ).findAny();
    }


    ///  Find the first [Result.Err], if any.
    public Optional<E> findFirstErr() {
        return stream.<E>mapMulti( Result::matchErr ).findFirst();
    }

    ///  Find any [Result.Err].
    public Optional<E> findAnyErr() {
        return stream.<E>mapMulti( Result::matchErr ).findAny();
    }

    ///  If _any_ [Result.OK] matches the given predicate, return true. Otherwise,
    ///  return false (no match, empty stream). [Result.Err] are ignored.
    public boolean anyMatch(final Predicate<? super V> predicate) {
        return stream.<V>mapMulti( Result::consume ).anyMatch( predicate );
    }

    ///  If _all_ [Result.OK] match the given predicate, return true. Otherwise,
    ///  return false (no match). [Result.Err] are ignored. An empty stream will return `true`.
    public boolean allMatch(final Predicate<? super V> predicate) {
        return stream.<V>mapMulti( Result::consume ).allMatch( predicate );
    }

    ///  If no [Result.OK] matches the given predicate, return true. Otherwise,
    ///  return false (no match). [Result.Err] are ignored. An empty stream will return `true`.
    public boolean noneMatch(final Predicate<? super V> predicate) {
        return stream.<V>mapMulti( Result::consume ).noneMatch( predicate );
    }

    ///  If _any_ [Result.Err] matches the given predicate, return true. Otherwise,
    ///  return false (no match, empty stream). [Result.OK] are ignored.
    public boolean anyMatchErr(final Predicate<? super E> predicate) {
        return stream.<E>mapMulti( Result::matchErr ).anyMatch( predicate );
    }

    ///  If _all_ [Result.Err] match the given predicate, return true. Otherwise,
    ///  return false (no match). [Result.OK] are ignored. An empty stream will return `true`.
    public boolean allMatchErr(final Predicate<? super E> predicate) {
        return stream.<E>mapMulti( Result::matchErr ).allMatch( predicate );
    }

    ///  If no [Result.Err] matches the given predicate, return true. Otherwise,
    ///  return false (no match). [Result.OK] are ignored. An empty stream will return `true`.
    public boolean noneMatchErr(final Predicate<? super E> predicate) {
        return stream.<E>mapMulti( Result::matchErr ).noneMatch( predicate );
    }


    /// Performs an action for each [Result.OK] in this stream. Nondeterministic order.
    public void forEach(final Consumer<? super V> action) {
        stream.<V>mapMulti( Result::consume ).forEach( action );
    }

    /// Performs an action for each [Result.OK] in this stream,
    /// in the encounter order of the stream if the stream has a defined encounter order.
    public void forEachOrdered(final Consumer<? super V> action) {
        stream.<V>mapMulti( Result::consume ).forEachOrdered( action );
    }

    /// Performs an action for each [Result.Err] in this stream. Nondeterministic order.
    public void forEachErr(final Consumer<? super E> action) {
        stream.<E>mapMulti( Result::matchErr ).forEach( action );
    }

    /// Performs an action for each [Result.Err] in this stream,
    /// in the encounter order of the stream if the stream has a defined encounter order.
    public void forEachOrderedErr(final Consumer<? super E> action) {
        stream.<E>mapMulti( Result::matchErr ).forEachOrdered( action );
    }


    ///  Equivalent to [Stream#limit(long)]
    public ResultStream<V, E> limit(final long maxSize) {
        return of( stream.limit( maxSize ) );
    }

    ///  Equivalent to [Stream#distinct()]
    public ResultStream<V, E> distinct() {
        return of( stream.distinct() );
    }

    ///  Equivalent to [Stream#sorted(Comparator)]
    public ResultStream<V, E> sorted(final Comparator<? super Result<V, E>> comparator) {
        return of( stream.sorted(comparator) );
    }


    /// Equivalent to [Stream#peek(Consumer)]
    public ResultStream<V, E> peek(final Consumer<? super Result<V, E>> action) {
        return of( stream.peek( action ) );
    }


    /// Equivalent to [Stream#skip(long)]
    public ResultStream<V, E> skip(final long n) {
        return of( stream.skip( n ) );
    }


    /// Equivalent to [Stream#reduce(Object, BinaryOperator)]
    public Result<V, E> reduce(final Result<V, E> identity, BinaryOperator<Result<V, E>> accumulator) {
        return stream.reduce( identity, accumulator );
    }


    /// Equivalent to {@link Stream#reduce(BinaryOperator)}
    public Optional<Result<V, E>> reduce(final BinaryOperator<Result<V, E>> accumulator) {
        return stream.reduce( accumulator );
    }


    /// Equivalent to [Stream#reduce(Object, BiFunction, BinaryOperator)]
    public <U> U reduce(final U identity, final BiFunction<U, ? super Result<V, E>, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce( identity, accumulator, combiner );
    }


    /// Equivalent to [Stream#collect(Supplier, BiConsumer, BiConsumer)]
    public <R> R collect(final Supplier<R> supplier, BiConsumer<R, ? super Result<V, E>> accumulator, final BiConsumer<R, R> combiner) {
        return stream.collect( supplier, accumulator, combiner );
    }


    /// Equivalent to [Stream#collect(Collector)]
    public <R, A> R collect(final Collector<? super Result<V, E>, A, R> collector) {
        return stream.collect( collector );
    }


    /// Equivalent to [Stream#toList()]
    /// The returned List is immutable.
    @SuppressWarnings("unchecked")
    public List<Result<V, E>> toList() {
        return List.of( (Result<V, E>[]) stream.toArray( Result[]::new ) );
    }


    /// Equivalent to [Stream#gather(Gatherer)]
    public <V2, E2> ResultStream<V2, E2> gather(final Gatherer<? super Result<V, E>, ?, Result<V2, E2>> gatherer) {
        return of( stream.gather( gatherer ) );
    }


    /// Equivalent to [Stream#min(Comparator)]
    public Optional<Result<V, E>> min(final Comparator<? super Result<V, E>> comparator) {
        return stream.min( comparator );
    }


    /// Equivalent to [Stream#max(Comparator)]
    public Optional<Result<V, E>> max(final Comparator<? super Result<V, E>> comparator) {
        return stream.max( comparator );
    }


    /// Equivalent to [Stream#count()]
    public long count() {
        return stream.count();
    }


    /// Equivalent to [Stream#iterator()]
    public Iterator<Result<V, E>> iterator() {
        return stream.iterator();
    }


    /// Equivalent to [Stream#spliterator()]
    public Spliterator<Result<V, E>> spliterator() {
        return stream.spliterator();
    }


    /// Equivalent to [Stream#isParallel()]
    public boolean isParallel() {
        return stream.isParallel();
    }


    /// Equivalent to [Stream#sequential()]
    public ResultStream<V, E> sequential() {
        return of( stream.sequential() );
    }


    /// Equivalent to [Stream#parallel()]
    public ResultStream<V, E> parallel() {
        return of( stream.parallel() );
    }


    /// Equivalent to [Stream#unordered()]
    public ResultStream<V, E> unordered() {
        return of( stream.unordered() );
    }


    /// Equivalent to [Stream#onClose(Runnable)]
    public ResultStream<V, E> onClose(Runnable closeHandler) {
        return of( stream.onClose( closeHandler ) );
    }

    /// Equivalent to [Stream#close()]
    public void close() {
        stream.close();
    }
}
