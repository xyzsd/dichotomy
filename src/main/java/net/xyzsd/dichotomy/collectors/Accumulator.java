package net.xyzsd.dichotomy.collectors;

import net.xyzsd.dichotomy.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulator for use in creating Collectors.
 *
 * @param <OK> ok/success/right type
 * @param <ERR> err/failure/left type
 */

final class Accumulator<OK, ERR> {
    final List<OK> okList = new ArrayList<>();
    final List<ERR> errList = new ArrayList<>();




    Accumulator<OK, ERR> append(Accumulator<OK, ERR> in) {
        okList.addAll( in.okList );
        errList.addAll( in.errList );
        return this;
    }


    Result<List<OK>, List<ERR>> finishBiasOK() {
        if (errList.isEmpty() || !okList.isEmpty()) {
            return Result.ofOK( List.copyOf( okList ) );
        } else {
            return Result.ofErr( List.copyOf( errList ) );
        }
    }

    Result<List<OK>, List<ERR>> finishBiasErr() {
        if (okList.isEmpty() || !errList.isEmpty()) {
            return Result.ofErr( List.copyOf( errList ) );
        } else {
            return Result.ofOK( List.copyOf( okList ) );
        }
    }


}
