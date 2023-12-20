package net.xyzsd.dichotomy.collectors;

import net.xyzsd.dichotomy.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        Result<List<String>, List<Integer>> collect = RESLIST.stream().collect( ResultCollectors.collector() );
        System.out.println(collect);
        collect = RESLIST_ONLY_SUCCESS.stream().collect( ResultCollectors.collector() );
        System.out.println(collect);
        collect = RESLIST_ONLY_FAIL.stream().collect( ResultCollectors.collector() );
        System.out.println(collect);
        collect = RESLIST_EMPTY.stream().collect( ResultCollectors.collector() );
        System.out.println(collect);
        throw new UnsupportedOperationException("TODO: *validate*");
    }

    @Test
    void okBiasedCollector() {
        Result<List<String>, List<Integer>> collect = RESLIST.stream().collect( ResultCollectors.okBiasedCollector() );
        System.out.println(collect);
        collect = RESLIST_ONLY_SUCCESS.stream().collect( ResultCollectors.okBiasedCollector() );
        System.out.println(collect);
        collect = RESLIST_ONLY_FAIL.stream().collect( ResultCollectors.okBiasedCollector() );
        System.out.println(collect);
        collect = RESLIST_EMPTY.stream().collect( ResultCollectors.okBiasedCollector() );
        System.out.println(collect);
        throw new UnsupportedOperationException("TODO: *validate*");
    }

    @Test
    void both() {
        ResultCollectors.Both<String, Integer> collect = RESLIST.stream().collect( ResultCollectors.both() );
        System.out.println(collect);
        collect = RESLIST_ONLY_SUCCESS.stream().collect( ResultCollectors.both() );
        System.out.println(collect);
        collect = RESLIST_ONLY_FAIL.stream().collect( ResultCollectors.both() );
        System.out.println(collect);
        collect = RESLIST_EMPTY.stream().collect( ResultCollectors.both() );
        System.out.println(collect);
        throw new UnsupportedOperationException("TODO: *validate*");
    }

}