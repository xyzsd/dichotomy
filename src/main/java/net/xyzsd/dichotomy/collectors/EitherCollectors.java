package net.xyzsd.dichotomy.collectors;

import net.xyzsd.dichotomy.Conversion;
import net.xyzsd.dichotomy.Either;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

/**
 * Collectors for {@link Either}s.
 * <p>
 * Three collectors are provided. The default collector ({@link #collector()}
 * is left-biased. However, both right-biased and a collector which will return
 * all left and right values encountered are included.
 * </p>
 * <p>
 * A left-biased collector will only return {@link net.xyzsd.dichotomy.Either.Right} values
 * if no {@link net.xyzsd.dichotomy.Either.Left} values have been encountered, and at least
 * one Right value was found. Otherwise,
 * all encountered {@link net.xyzsd.dichotomy.Either.Left} values will be returned.
 * </p>
 */
@NullMarked
public interface EitherCollectors {


    /**
     * A left-biased collector of {@link Either}s.
     * <p>
     * This will return right values iff there are no left values, and
     * at least one right value is present.
     * </p>
     *
     * @param <L> Left type
     * @param <R> Right type
     * @return An Either containing a List of Left or Right values, as above.
     */
    static <L, R> Collector<Either<L, R>, ?, Either<List<L>, List<R>>> collector() {
        return Collector.<Either<L, R>, Accumulator<R, L>, Either<List<L>, List<R>>>of(
                Accumulator::new,
                EitherCollectors::add,
                Accumulator::append,
                accum -> Conversion.toEither( accum.finishBiasErr() )
        );
    }

    /**
     * A right-biased collector of {@link Either}s.
     * <p>
     * This will return Left values iff there are no right values
     * and at least a single left value is present.
     * </p>
     *
     * @param <L> Left type
     * @param <R> Right type
     * @return An Either containing a List of Left or Right values, as above.
     */
    static <L, R> Collector<Either<L, R>, ?, Either<List<L>, List<R>>> rightBiasedCollector() {
        return Collector.<Either<L, R>, Accumulator<R, L>, Either<List<L>, List<R>>>of(
                Accumulator::new,
                EitherCollectors::add,
                Accumulator::append,
                accum -> Conversion.toEither( accum.finishBiasOK() )
        );
    }

    /**
     * An unbiased collector of {@link Either}s.
     * <p>
     * This will return <b>both</b> Left and Right values
     * </p>
     *
     * @param <L> Left type
     * @param <R> Right type
     * @return a tuple containing both Left and Right values.
     */
    static <L, R> Collector<Either<L, R>, ?, LeftsAndRights<L, R>> both() {
        return Collector.<Either<L, R>, Accumulator<R, L>, LeftsAndRights<L, R>>of(
                Accumulator::new,
                EitherCollectors::add,
                Accumulator::append,
                accum -> new LeftsAndRights<>( accum.errList, accum.okList )
        );
    }


    static private <L, R> void add(Accumulator<R, L> listBox, Either<L, R> either) {
        switch (either) {
            case Either.Right(R r) -> listBox.okList.add( r );
            case Either.Left(L l) -> listBox.errList.add( l );
        }
    }

    /**
     * Tuple containing lists of Left and Right values
     * <p>
     * Contained lists are immutable and never null, but may be empty.
     * </p>
     *
     * @param lefts  Left values
     * @param rights Right values
     * @param <L>    Left type
     * @param <R>    Right type
     */
    record LeftsAndRights<L, R>(List<L> lefts, List<R> rights) {

        /**
         * Create a LeftAndRights
         *
         * @param lefts  left values
         * @param rights right values
         */
        public LeftsAndRights {
            Objects.requireNonNull( lefts );
            Objects.requireNonNull( rights );
            lefts = List.copyOf( lefts );
            rights = List.copyOf( rights );
        }

    }


}
