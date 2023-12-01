package net.xyzsd.dichotomy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmptyTest {

    @Test
    void uniqueness() {
        assertSame(Empty.getInstance(), Empty.getInstance());
    }

    @Test
    void equality() {
        assertEquals(Empty.getInstance(), Empty.getInstance());
    }

}