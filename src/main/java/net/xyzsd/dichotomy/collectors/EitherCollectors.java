package net.xyzsd.dichotomy.collectors;

import net.xyzsd.dichotomy.Conversion;
import net.xyzsd.dichotomy.Either;
import net.xyzsd.dichotomy.Result;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;


public interface EitherCollectors {


    static <L, R> Collector<Either<L, R>, ?, Either<List<L>, List<R>>> collector() {
        return Collector.<Either<L, R>, Accumulator<R, L>, Either<List<L>, List<R>>>of(
                Accumulator::new,
                EitherCollectors::add,
                Accumulator::append,
                    accum -> Conversion.toEither(accum.finishBiasErr())
        );
    }

    static <L, R> Collector<Either<L, R>, ?, Either<List<L>, List<R>>> rightBiasedCollector() {
        return Collector.<Either<L, R>, Accumulator<R, L>, Either<List<L>, List<R>>>of(
                Accumulator::new,
                EitherCollectors::add,
                Accumulator::append,
                accum -> Conversion.toEither(accum.finishBiasOK())
        );
    }

    static <L, R> Collector<Either<L, R>, ?, Both<L, R>> both() {
        return Collector.<Either<L, R>, Accumulator<R, L>, Both<L, R>>of(
                Accumulator::new,
                EitherCollectors::add,
                Accumulator::append,
                accum -> new Both<>( accum.errList, accum.okList )
        );
    }

    static private <L,R> void add(Accumulator<R,L> listBox, Either<L,R> either) {
        switch (either) {
            case Either.Left<L, R> left -> listBox.errList.add( left.value() );
            case Either.Right<L, R> right -> listBox.okList.add( right.value() );
        }
    }

    record Both<ERR, OK>(List<ERR> errs, List<OK> oks) {

        public Both {
            Objects.requireNonNull( oks );
            Objects.requireNonNull( errs );
            oks = List.copyOf( oks );
            errs = List.copyOf( errs);
        }

    }


}
