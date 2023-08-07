package net.javapla.jawn.core.internal.template.lox.source;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.internal.template.lox.source.CharArraySource;


class CharArraySourceTest {

    @Test
    void read() {
        CharArraySource source = new CharArraySource("<html><body><h1>Headline</h1></body></html>");
        
        assertTrue(source.substring().isEmpty());
        
        assertEquals('<', source.peek());
        assertEquals('h', source.peekNext());
        
        source.advance(); // <
        source.advance(); // h
        source.advance(); // t
        source.advance(); // m
        source.advance(); // l
        source.advance(); // >
        
        assertEquals("<html>", source.substring());
        
        assertEquals('<', source.peek());
        assertEquals('<', source.consume());
        assertEquals('b', source.consume());
    }

    
    @Test
    void consumeRest() {
        CharArraySource source = new CharArraySource("<html> attr </html>");
        
        source.advance(); // <
        source.advance(); // h
        source.advance(); // t
        source.advance(); // m
        source.advance(); // l
        source.advance(); // >
        
        source.advance(); //  
        source.advance(); // a
        source.advance(); // t
        source.advance(); // t
        source.advance(); // r
        source.advance(); //
        
        source.mark();
        
        while (!source.isAtEnd()) source.advance();
        assertEquals("</html>", source.substring());
        
        source.mark();
        assertTrue(source.substring().isEmpty());
    }
    
    @Test
    void newlines() {
        CharArraySource source = new CharArraySource("<html> \nattr \n</html>");
        while (!source.isAtEnd()) source.advance();
        
        assertEquals(3, source.line);
        assertEquals(7, source.posInLine);
    }
}

