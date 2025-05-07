package net.xyzsd.dichotomy.stream;

import net.xyzsd.dichotomy.Result;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

///
///  This is just the beginnings of a test, and currently the output must be
///  manually verified.
///
class ResultStreamSmokeTest {

    @Test
    public void basicSmokeTest() {
        List<Result<String,Integer>> resultList = new ArrayList<>();
        for(int i=1; i<100; i++) {
            if(i % 10 != 0) {
                resultList.add(Result.ofOK("s_"+i+"_s"));
            } else {
                resultList.add(Result.ofErr( -i ));
            }
        }

        {
            System.out.println("--------");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            AtomicInteger i = new AtomicInteger(0);
            System.out.println( "COUNT (unfiltered): "+rs.count() );
        }

        {
            System.out.println("--------");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            AtomicInteger i = new AtomicInteger(0);
            rs.forEachOrdered( x -> i.incrementAndGet() );
            System.out.println( "COUNT (forEachOrdered (OK): "+i.get() );
        }


        {
            System.out.println("--------");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            AtomicInteger i = new AtomicInteger(0);
            rs.parallel().forEachOrdered( x -> i.incrementAndGet() );
            System.out.println( "COUNT (parallel:forEachOrdered (OK): "+i.get() );
        }

        {
            System.out.println("-------- untilOK/forEachOrdered");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            rs.untilErr()
                    .forEach( System.out::println );
        }

        {
            System.out.println("-------- untilOK/forEachOrderedErr");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            rs.untilErr()
                    .forEachErr( System.out::println );
        }

        {
            System.out.println("-------- ffOK");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            rs.findFirst()
                    .ifPresentOrElse( System.out::println, () -> System.out.println("NONE"));
        }

        {
            System.out.println("-------- ffErr");
            final ResultStream<String, Integer> rs = ResultStream.from( resultList.stream() );
            rs.findFirstErr()
                    .ifPresentOrElse( System.out::println, () -> System.out.println("NONE"));
        }

    }
}