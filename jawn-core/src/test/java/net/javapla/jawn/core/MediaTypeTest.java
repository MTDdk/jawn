package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class MediaTypeTest {

    @Test
    void parse() {
        List<MediaType> list = MediaType.parse("application/json, text/html, *");
        assertEquals(3, list.size());
        assertEquals("application/json", list.get(0).value());
        assertEquals("text/html", list.get(1).value());
        assertEquals("*/*", list.get(2).value());
        
        assertEquals(1, MediaType.parse("text/plain, ").size());
    }
    
    @Test
    void parameters() {
        MediaType type = MediaType.valueOf("text/html; charset=utf-8");
        assertEquals("utf-8", type.parameter("charset"));
        assertEquals("utf-8", type.charset());
        
        type = MediaType.valueOf("text/html; charset=utf-8; something=else;more=of");
        assertEquals("else", type.parameter("something"));
        assertEquals("of", type.parameter("more"));
        assertEquals("utf-8", type.charset());
    }
    
    @Test
    void accept() {
        // This string is most commonly used in "Accept" header
        List<MediaType> list = MediaType.parse("text/html, application/xml;q=0.9, */*;q=0.8");
        assertEquals(3, list.size());
        assertEquals(MediaType.HTML, list.get(0));
        assertEquals(MediaType.XML, list.get(1));
        assertEquals(MediaType.WILDCARD, list.get(2));
        
        assertEquals(1.0f, list.get(0).quality());
        assertEquals(0.9f, list.get(1).quality());
        assertEquals(0.8f, list.get(2).quality());
    }

}

