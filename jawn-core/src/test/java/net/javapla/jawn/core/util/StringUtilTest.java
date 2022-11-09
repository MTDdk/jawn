package net.javapla.jawn.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class StringUtilTest {

    @Test
    void split_multiLines() {
        String[] lines = {
            "mode=test", 
            "server=highest",
            "port=8000"
        };
        
        AtomicInteger index = new AtomicInteger(0);
        
        StringUtil.split(lines, '=', (s1,s2) -> {
            assertEquals(s1 + "=" + s2, lines[index.getAndIncrement()]);
        });
        
        assertEquals(3, index.get());
    }
    
}
