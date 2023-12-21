package net.xyzsd.dichotomy.collectors;

import net.xyzsd.dichotomy.Either;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EitherCollectorsTest {


    List<Either<Integer,String>> RESLIST = List.of(
            Either.ofRight("First"),
            Either.ofRight("Second"),
            Either.ofLeft(-3),
            Either.ofRight("Fourth"),
            Either.ofRight("Fifth"),
            Either.ofLeft( -6)
    );

    List<Either<Integer,String>> RESLIST_EMPTY = List.of();
    List<Either<Integer,String>> RESLIST_ONLY_SUCCESS = List.of(
            Either.ofRight("First"),
            Either.ofRight("Second")
    );

    List<Either<Integer,String>> RESLIST_ONLY_FAIL = List.of(
            Either.ofLeft(-111),
            Either.ofLeft(-222)
    );



    @Test
    void collector() {
        assertEquals(
                Either.ofLeft( List.of(-3, -6) ),
                RESLIST.stream().collect( EitherCollectors.collector() )
        );

        assertEquals(
                Either.ofRight(List.of("First","Second")),
                RESLIST_ONLY_SUCCESS.stream().collect( EitherCollectors.collector() )
        );

        assertEquals(
                Either.ofLeft(List.of(-111,-222)),
                RESLIST_ONLY_FAIL.stream().collect( EitherCollectors.collector() )
        );

        assertEquals(
                Either.ofLeft(List.of()),
                RESLIST_EMPTY.stream().collect( EitherCollectors.collector() )
        );
    }

    @Test
    void rightBiasedCollector() {
        assertEquals(
                Either.ofRight( List.of("First", "Second", "Fourth", "Fifth") ),
                RESLIST.stream().collect( EitherCollectors.rightBiasedCollector() )
        );

        assertEquals(
                Either.ofRight( List.of("First", "Second") ),
                RESLIST_ONLY_SUCCESS.stream().collect( EitherCollectors.rightBiasedCollector() )
        );

        assertEquals(
                Either.ofLeft( List.of(-111,-222) ),
                RESLIST_ONLY_FAIL.stream().collect( EitherCollectors.rightBiasedCollector() )
        );

        assertEquals(
                Either.ofRight( List.of() ),
                RESLIST_EMPTY.stream().collect( EitherCollectors.rightBiasedCollector() )
        );
    }

    @Test
    void both() {
        assertEquals(
                new EitherCollectors.LeftsAndRights<>(
                        List.of(-3, -6),
                        List.of("First", "Second", "Fourth", "Fifth") ),
                RESLIST.stream().collect( EitherCollectors.both() )
        );

        assertEquals(
                new EitherCollectors.LeftsAndRights<>( List.of(),
                        List.of("First", "Second") ),
                RESLIST_ONLY_SUCCESS.stream().collect( EitherCollectors.both() )
        );

        assertEquals(
                new EitherCollectors.LeftsAndRights<>( List.of(-111,-222), List.of() ),
                RESLIST_ONLY_FAIL.stream().collect( EitherCollectors.both() )
        );

        assertEquals(
                new EitherCollectors.LeftsAndRights<>( List.of(),List.of() ),
                RESLIST_EMPTY.stream().collect( EitherCollectors.both() )
        );
    }
    
    
}