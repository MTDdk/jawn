package net.javapla.jawn.core.internal.template;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class LoxTest {

    @Test
    void test() {
        String source = "<html> {{ attribute }} </html>";
        LoxScanner scanner = new LoxScanner(source);
        List<Token> tokens = scanner.scan();
        for (Token token : tokens) {
            System.out.println(token);
        }
        
        assertEquals(6, tokens.size());
        assertEquals("<html> ",tokens.get(0).lexeme);
        assertEquals(TokenType.CODE_START,tokens.get(1).type);
        assertEquals("attribute",tokens.get(2).lexeme);
        assertEquals(TokenType.CODE_END,tokens.get(3).type);
        assertEquals(" </html>",tokens.get(4).lexeme);
    }
    
    
    @Test
    void noTextAtEnd() {
        String source = "somethingsomething {{ attribute }}";
        LoxScanner scanner = new LoxScanner(source);
        List<Token> tokens = scanner.scan();
        for (Token token : tokens) {
            System.out.println(token);
        }
        
        assertEquals(5, tokens.size());
        assertEquals("somethingsomething ",tokens.get(0).lexeme);
        assertEquals(TokenType.CODE_START,tokens.get(1).type);
        assertEquals("attribute",tokens.get(2).lexeme);
        assertEquals(TokenType.CODE_END,tokens.get(3).type);
    }

}
