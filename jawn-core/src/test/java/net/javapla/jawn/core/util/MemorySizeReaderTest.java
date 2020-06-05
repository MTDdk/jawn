package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MemorySizeReaderTest {

    @Test
    public void test() {
        assertEquals(16777216, MemorySizeReader.bytes("16m"));
        assertEquals(16, MemorySizeReader.bytes("16b"));
        assertEquals(16, MemorySizeReader.bytes("16"));
        assertEquals(16384, MemorySizeReader.bytes("16k"));
        
        assertEquals(1048576 + 16384 + 4, MemorySizeReader.bytes("1m16k4"));
        assertEquals(2048 + 20, MemorySizeReader.bytes("2k20"));
    }

}
