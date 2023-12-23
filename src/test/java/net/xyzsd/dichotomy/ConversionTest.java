package net.xyzsd.dichotomy;

import net.xyzsd.dichotomy.trying.Try;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConversionTest {

    public static final String GOOD = "GoodString";
    public static final Integer BAD = -666;
    public static final String EX_MSG = "BadMessage";
    public static final IOException EX = new IOException(EX_MSG);


    public static final Result<String,Integer> GOOD_RESULT = Result.ofOK(GOOD);
    public static final Result<String,Integer> BAD_RESULT = Result.ofErr(BAD);

    public static final Either<Integer,String> GOOD_EITHER = Either.ofRight( GOOD );
    public static final Either<Integer,String> BAD_EITHER = Either.ofLeft( BAD );


    public static final Try<String> GOOD_TRY = Try.ofSuccess( GOOD );
    public static final Try<String> BAD_TRY = Try.ofFailure( EX );


    public static final Result<String,IOException> EX_RESULT_BAD = Result.ofErr( EX );
    public static final Result<String,IOException> EX_RESULT_GOOD = Result.ofOK( GOOD );



    @Test
    void toEitherFromResult() {
        assertEquals(
                GOOD_EITHER,
                Conversion.toEither( GOOD_RESULT )
        );

        assertEquals(
                BAD_EITHER,
                Conversion.toEither( BAD_RESULT )
        );
    }

    @Test
    void toEitherFromTry() {
        assertEquals(
                Either.ofRight( GOOD ),
                Conversion.toEither( GOOD_TRY )
        );

        assertEquals(
                Either.ofLeft( EX ),
                Conversion.toEither( BAD_TRY )
        );
    }

    @Test
    void toResultFromEither() {
        assertEquals(
                GOOD_RESULT,
                Conversion.toResult( GOOD_EITHER )
        );

        assertEquals(
                BAD_RESULT,
                Conversion.toResult( BAD_EITHER )
        );   }

    @Test
    void toResultFromTry() {
        assertEquals(
                Result.ofOK( GOOD ),
                Conversion.toResult( GOOD_TRY )
        );

        assertEquals(
                Result.ofErr( EX ),
                Conversion.toResult( BAD_TRY )
        );
    }

    @Test
    void toTryFromEither() {
        assertEquals(
                GOOD_TRY,
                Conversion.toTry( Conversion.toEither( EX_RESULT_GOOD) )
        );

        assertEquals(
                BAD_TRY,
                Conversion.toTry( Conversion.toEither( EX_RESULT_BAD) )
        );
    }

    @Test
    void toTryFromResult() {
        assertEquals(
                GOOD_TRY,
                Conversion.toTry( EX_RESULT_GOOD )
        );

        assertEquals(
                BAD_TRY,
                Conversion.toTry( EX_RESULT_BAD )
        );
    }
}