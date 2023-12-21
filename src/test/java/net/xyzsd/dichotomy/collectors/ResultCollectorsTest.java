package net.xyzsd.dichotomy.collectors;

import net.xyzsd.dichotomy.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ResultCollectorsTest {

    List<Result<String,Integer>> RESLIST = List.of(
            Result.ofOK("First"),
            Result.ofOK("Second"),
            Result.ofErr(-3),
            Result.ofOK("Fourth"),
            Result.ofOK("Fifth"),
            Result.ofErr( -6)
    );

    List<Result<String,Integer>> RESLIST_EMPTY = List.of();
    List<Result<String,Integer>> RESLIST_ONLY_SUCCESS = List.of(
            Result.ofOK("First"),
            Result.ofOK("Second")
    );

    List<Result<String,Integer>> RESLIST_ONLY_FAIL = List.of(
            Result.ofErr(-111),
            Result.ofErr(-222)
            );



    @Test
    void collector() {
        assertEquals(
                Result.ofErr( List.of(-3, -6) ),
                RESLIST.stream().collect( ResultCollectors.collector() )
        );

        assertEquals(
                Result.ofOK(List.of("First","Second")),
                RESLIST_ONLY_SUCCESS.stream().collect( ResultCollectors.collector() )
        );

        assertEquals(
                Result.ofErr(List.of(-111,-222)),
                RESLIST_ONLY_FAIL.stream().collect( ResultCollectors.collector() )
        );

        assertEquals(
                Result.ofErr(List.of()),
                RESLIST_EMPTY.stream().collect( ResultCollectors.collector() )
        );
    }

    @Test
    void okBiasedCollector() {
        assertEquals(
                Result.ofOK( List.of("First", "Second", "Fourth", "Fifth") ),
                RESLIST.stream().collect( ResultCollectors.okBiasedCollector() )
        );

        assertEquals(
                Result.ofOK( List.of("First", "Second") ),
                RESLIST_ONLY_SUCCESS.stream().collect( ResultCollectors.okBiasedCollector() )
        );

        assertEquals(
                Result.ofErr( List.of(-111,-222) ),
                RESLIST_ONLY_FAIL.stream().collect( ResultCollectors.okBiasedCollector() )
        );

        assertEquals(
                Result.ofOK( List.of() ),
                RESLIST_EMPTY.stream().collect( ResultCollectors.okBiasedCollector() )
        );
    }

    @Test
    void both() {
        assertEquals(
                new ResultCollectors.OKsAndErrs<>( List.of("First", "Second", "Fourth", "Fifth"),List.of(-3,-6) ),
                RESLIST.stream().collect( ResultCollectors.both() )
        );

        assertEquals(
                new ResultCollectors.OKsAndErrs<>( List.of("First", "Second"),List.of() ),
                RESLIST_ONLY_SUCCESS.stream().collect( ResultCollectors.both() )
        );

        assertEquals(
                new ResultCollectors.OKsAndErrs<>( List.of(),List.of(-111,-222) ),
                RESLIST_ONLY_FAIL.stream().collect( ResultCollectors.both() )
        );

        assertEquals(
                new ResultCollectors.OKsAndErrs<>( List.of(),List.of() ),
                RESLIST_EMPTY.stream().collect( ResultCollectors.both() )
        );
    }

}