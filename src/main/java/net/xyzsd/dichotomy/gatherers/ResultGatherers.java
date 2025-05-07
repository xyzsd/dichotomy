package net.xyzsd.dichotomy.gatherers;

import net.xyzsd.dichotomy.Result;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Gatherer;


///
/// An assortment of [Gatherer]s used to process streams of [Result] values.
///
@NullMarked
public final class ResultGatherers {
    // TODO: mapOK vs. using default bias (to ok) and naming it 'map'
    //       generally I prefer the simpler names reflecting the default bias BUT
    //       since these are standalone statics I think more explicit naming
    //       is clearer
    // TODO: documentation
    // TODO: performance evaluation



    ///  biMap implemented as a Gatherer
    public static <V, E, V2, E2> Gatherer<Result<V, E>, ?, Result<V2, E2>> biMap(
            Function<? super V, ? extends V2> okMapper, Function<? super E, ? extends E2> errMapper) {
        return Gatherer.of(
                (_, result, downstream) ->
                        downstream.push( result.biMap( okMapper, errMapper ) )
        );
    }

    ///  mapOK implemented as a Gatherer (using biMap)
    public static <V, E, V2> Gatherer<Result<V, E>, ?, Result<V2, E>> mapOK(
            Function<? super V, ? extends V2> okMapper) {
        return biMap( okMapper, Function.identity() );
    }


    ///  mapErr implemented as a Gatherer (using biMap)
    public static <V, E, E2> Gatherer<Result<V, E>, ?, Result<V, E2>> mapErr(
            Function<? super E, ? extends E2> errMapper) {
        return biMap( Function.identity(), errMapper );
    }


    ///  biFlatMap implemented as a Gatherer
    public static <V, E, V2, E2> Gatherer<Result<V, E>, ?, Result<V2, E2>> biFlatMap(
            Function<? super V, ? extends Result<? extends V2, ? extends E2>> okMapper,
            Function<? super E, ? extends Result<? extends V2, ? extends E2>> errMapper) {
        return Gatherer.of(
                (_, result, downstream) ->
                        downstream.push( result.biFlatMap( okMapper, errMapper ) )
        );
    }

    ///  flatMapOK implemented as a Gatherer (using biFlatMap)
    public static <V, E, V2> Gatherer<Result<V, E>, ?, Result<V2, E>> flatMapOK(
            Function<? super V, ? extends Result<? extends V2, ? extends E>> okMapper) {
        return biFlatMap( okMapper, Result::ofErr );
    }

    ///  flatMapErr implemented as a Gatherer (using biFlatMap)
    public static <V, E, E2> Gatherer<Result<V, E>, ?, Result<V, E2>> flatMapErr(
            Function<? super E, ? extends Result<? extends V, ? extends E2>> errMapper) {
        return biFlatMap( Result::ofOK, errMapper );
    }

    ///  Filter [Result.OK]:
    ///
    ///  Only [Result.OK] that match the predicate are pushed downstream.
    ///
    ///  *All* [Result.Err] are pushed downstream.
    public static <V, E> Gatherer<Result<V, E>, ?, Result<V, E>> filterOK(
            Predicate<? super V> predicate) {
        return biFilter( predicate, _ -> true );
    }


    ///  Filter [Result.Err]:
    ///
    ///  Only [Result.Err] that match the predicate are pushed downstream.
    ///
    ///  *All* [Result.OK] are pushed downstream.
    public static <V, E> Gatherer<Result<V, E>, ?, Result<V, E>> filterErr(
            Predicate<? super E> predicate) {
        return biFilter( _ -> true, predicate );
    }


    ///  internal: bi-filter; only push results that match the given predicate(s)
    ///  todo: Consider making this public!
    private static <V, E> Gatherer<Result<V, E>, ?, Result<V, E>> biFilter(
            Predicate<? super V> okPredicate, Predicate<? super E> errPredicate) {
        return Gatherer.of(
                (_, result, downstream) -> switch (result) {
                    case Result.OK<V, E> ok when okPredicate.test( ok.get() ) -> downstream.push( result );
                    case Result.Err<V, E> err when errPredicate.test( err.get() ) -> downstream.push( result );
                    default -> true;
                }
        );
    }


    ///  A Gatherer that passes all [Result.OK] through, until a Result.Err is encountered.
    ///  Once a [Result.Err] is encountered, that [Result.Err] is pushed downstream, _however_ no
    ///  further Results (of any type, whether OK or Err) are pushed (stream is terminated).
    public static <V, E> Gatherer<Result<V,E>, ?, Result<V,E>> untilErr() {
        return until( Result::isErr );
        /* originally: (see 'until()')
            ...
            if (state.get()) {
                return false;   // we are done
            } else if (result instanceof Result.Err<V,E> _) {
                state.set( true );
            }
            ...
         */
    }


    ///  A Gatherer that passes all Result.Err through, until a Result.OK is encountered.
    ///  Once a Result.OK is encountered, that Result.OK is pushed downstream, _however_ no
    ///  further Results (of any type, whether OK or Err) are pushed (stream is terminated).
    public static <V, E> Gatherer<Result<V, E>, ?, Result<V, E>> untilOK() {
        return until( Result::isOK );
    }


    /// pass through until condition is met; the result meeting the condition is passed through BUT
    /// no further results are passed.
    //  TODO: consider more carefully how this might work on parallel streams
    private static <V, E> Gatherer<Result<V, E>, ?, Result<V, E>>
    until(Predicate<Result<V, E>> condition) {
        // state (AtomicBoolean): false == keep going; true == stop
        Gatherer.Integrator<AtomicBoolean, Result<V, E>, Result<V, E>>
                integrator = (state, result, downstream) -> {
            if (state.get()) {
                return false;   // we are done
            } else if (condition.test( result )) {
                state.set( true ); // we will be done, AFTER we pass the next element
            }
            return downstream.push( result );
        };

        return Gatherer.ofSequential( AtomicBoolean::new, integrator );
    }


}
