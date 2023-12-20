package net.xyzsd.dichotomy.trying.function;

import net.xyzsd.dichotomy.TestUtils;
import net.xyzsd.dichotomy.TestUtils.SingleUseConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExConsumerTest {

    @Test
    void from() {
        assertThrows( NullPointerException.class, () -> ExConsumer.from( null ) );
        SingleUseConsumer<String> consumer = new SingleUseConsumer<>();
        final ExConsumer<String> from = ExConsumer.from( consumer );
        assertDoesNotThrow(  () -> from.accept("test") );
        assertTrue( consumer.usedJustOnce() );
    }

    @Test
    void andThen() {
        SingleUseConsumer<String> firstSUC = new SingleUseConsumer<>();
        SingleUseConsumer<String> lastSUC = new SingleUseConsumer<>();
        ExConsumer<String> firstEC = ExConsumer.from( firstSUC );
        ExConsumer<String> lastEC = ExConsumer.from( lastSUC );
        final ExConsumer<String> composed = firstEC.andThen( lastEC );
        assertDoesNotThrow( () -> composed.accept( "test" ) );
        assertTrue( firstSUC.usedJustOnce() );
        assertTrue( lastSUC.usedJustOnce() );
    }
}