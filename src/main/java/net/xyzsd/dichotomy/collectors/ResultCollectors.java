package net.xyzsd.dichotomy.collectors;


import net.xyzsd.dichotomy.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

/**
 * Collectors for {@link Result}s.
 * <p>
 * Three collectors are provided. The default collector ({@link #collector()}
 * is error-biased. However, both ok-biased and a collector which will return
 * all ok and error values encountered are included.
 * </p>
 */
public interface ResultCollectors {


    /**
     * Default error-biased collector.
     * <p>
     * This will return Err values, unless there are none; then OK values will be returned.
     * If there are no OK values (and no Err values), an Err with an empty List will be returned.
     * </p>
     *
     * @param <OK>  OK type
     * @param <ERR> Err type
     * @return A Result containing a List of OK or Err values as described above.
     */
    static <OK, ERR> Collector<Result<OK, ERR>, ?, Result<List<OK>, List<ERR>>> collector() {
        return Collector.<Result<OK, ERR>, Accumulator<OK, ERR>, Result<List<OK>, List<ERR>>>of(
                Accumulator::new,
                ResultCollectors::add,
                Accumulator::append,
                Accumulator::finishBiasErr
        );
    }

    /**
     * OK-biased collector.
     * <p>
     * This will return OK values, unless there are none; then Err values will be returned.
     * If there are no Err values (and no OK values), an OK with an empty List will be returned.
     * </p>
     *
     * @param <OK>  OK type
     * @param <ERR> Err type
     * @return A Result containing a List of OK or Err values as described above.
     */
    static <OK, ERR> Collector<Result<OK, ERR>, ?, Result<List<OK>, List<ERR>>> okBiasedCollector() {
        return Collector.<Result<OK, ERR>, Accumulator<OK, ERR>, Result<List<OK>, List<ERR>>>of(
                Accumulator::new,
                ResultCollectors::add,
                Accumulator::append,
                Accumulator::finishBiasOK
        );
    }

    /**
     * Unbiased Result collector.
     * <p>
     * This will return <b>both</b> OK and Err values
     * </p>
     *
     * @param <OK>  OK type
     * @param <ERR> Err type
     * @return Tuple containing OK and Err values
     */
    static <OK, ERR> Collector<Result<OK, ERR>, ?, OKsAndErrs<OK, ERR>> both() {
        return Collector.<Result<OK, ERR>, Accumulator<OK, ERR>, OKsAndErrs<OK, ERR>>of(
                Accumulator::new,
                ResultCollectors::add,
                Accumulator::append,
                accum -> new OKsAndErrs<>( accum.okList, accum.errList )

        );
    }


    static private <OK, ERR> void add(Accumulator<OK, ERR> listBox, Result<OK, ERR> result) {
        switch (result) {
            case Result.OK<OK, ERR> ok -> listBox.okList.add( ok.get() );
            case Result.Err<OK, ERR> err -> listBox.errList.add( err.get() );
        }
    }


    /**
     * Tuple containing lists of OK and Err values
     * <p>
     *     Contained lists are immutable and never null, but may be empty.
     * </p>
     *
     * @param oks OK values
     * @param errs Err values
     * @param <OK> OK type
     * @param <ERR> Err type
     */
    record OKsAndErrs<OK, ERR>(@NotNull List<OK> oks, @NotNull List<ERR> errs) {

        /**
         * Create a OKsAndErrs
         * @param oks OK values
         * @param errs Err values
         */
        public OKsAndErrs {
            requireNonNull( oks );
            requireNonNull( errs );
            oks = List.copyOf( oks );
            errs = List.copyOf( errs );
        }

    }
}
