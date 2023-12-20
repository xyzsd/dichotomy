package net.xyzsd.dichotomy.collectors;


import net.xyzsd.dichotomy.Result;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

public interface ResultCollectors {


    static <OK, ERR> Collector<Result<OK, ERR>, ?, Result<List<OK>, List<ERR>>> collector() {
        return Collector.<Result<OK, ERR>, Accumulator<OK,ERR>, Result<List<OK>, List<ERR>>>of(
                Accumulator::new,
                ResultCollectors::add,
                Accumulator::append,
                Accumulator::finishBiasErr
        );
    }

    static <OK, ERR> Collector<Result<OK, ERR>, ?, Result<List<OK>, List<ERR>>> okBiasedCollector() {
        return Collector.<Result<OK, ERR>, Accumulator<OK,ERR>, Result<List<OK>, List<ERR>>>of(
                Accumulator::new,
                ResultCollectors::add,
                Accumulator::append,
                Accumulator::finishBiasOK
        );
    }

    static <OK, ERR> Collector<Result<OK, ERR>, ?, Both<OK, ERR>> both() {
        return Collector.<Result<OK, ERR>, Accumulator<OK,ERR>, Both<OK,ERR>>of(
                Accumulator::new,
                ResultCollectors::add,
                Accumulator::append,
                accum -> new Both<>( accum.okList, accum.errList )

        );
    }



    static private <OK, ERR> void add(Accumulator<OK,ERR> listBox, Result<OK,ERR> result) {
        switch (result) {
                case Result.OK<OK, ERR> ok -> listBox.okList.add( ok.get() );
                case Result.Err<OK, ERR> err -> listBox.errList.add( err.get() );
            }
    }




    record Both<OK, ERR>(List<OK> oks, List<ERR> errs) {

        public Both {
            Objects.requireNonNull( oks );
            Objects.requireNonNull( errs );
            oks = List.copyOf( oks );
            errs = List.copyOf( errs);
        }

    }
}
