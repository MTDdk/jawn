package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ByteRangeTest {

    @Test
    void test() {
        ByteRange range = ByteRange.parse("bytes=1-10", 10);
        assertEquals(Status.PARTIAL_CONTENT, range.status());
        assertEquals(1, range.start());
        assertEquals(9, range.end());
        assertEquals(9, range.contentLength());
        assertEquals("bytes 1-9/10", range.contentRange());
        
        
        range = ByteRange.parse("bytes=99-", 110);
        assertEquals(Status.PARTIAL_CONTENT, range.status());
        assertEquals(99, range.start());
        assertEquals(109, range.end());
        assertEquals(11, range.contentLength());
        assertEquals("bytes 99-109/110", range.contentRange());
        
        range = ByteRange.parse("bytes=-99", 200);
        assertEquals(Status.PARTIAL_CONTENT, range.status());
        assertEquals(101, range.start());
        assertEquals(199, range.end());
        assertEquals(99, range.contentLength());
        assertEquals("bytes 101-199/200", range.contentRange());
        
        // 100-150 is ignored.
        range = ByteRange.parse("bytes=0-50, 100-150", 200);
        assertEquals(Status.PARTIAL_CONTENT, range.status());
        assertEquals(0, range.start());
        assertEquals(50, range.end());
        assertEquals(51, range.contentLength());
        assertEquals("bytes 0-50/200", range.contentRange());
    }
    
    @Test
    void noRange() {
        ByteRange range = ByteRange.parse("bytes=-", 10);
        assertEquals(Status.REQUESTED_RANGE_NOT_SATISFIABLE, range.status());
        
        range = ByteRange.parse(null, 10);
        assertEquals(Status.OK, range.status());
        
        range = ByteRange.parse("foo", 100);
        assertEquals(Status.REQUESTED_RANGE_NOT_SATISFIABLE, range.status());

        range = ByteRange.parse("bytes=", 10);
        assertEquals(Status.REQUESTED_RANGE_NOT_SATISFIABLE, range.status());
        
        range = ByteRange.parse("bytes=z-", 10);
        assertEquals(Status.REQUESTED_RANGE_NOT_SATISFIABLE, range.status());
        
        range = ByteRange.parse("bytes=-z", 10);
        assertEquals(Status.REQUESTED_RANGE_NOT_SATISFIABLE, range.status());
    }

    @Test
    void outOfRange() {
        // Should it instead return a Status.REQUESTED_RANGE_NOT_SATISFIABLE ?
        ByteRange range = ByteRange.parse("bytes=16-300", 250);
        assertEquals(249, range.end());
    }
}
