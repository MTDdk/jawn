package net.javapla.jawn.core.internal.template;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class LoxTest {

    @Test
    void test() {
        String source = "<html> {{ attribute }} </html>";
        Scanner scanner = new Scanner(source);
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
        Scanner scanner = new Scanner(source);
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
    
    @Test
    void justCode() {
        String source = "{{attribute1}} {{ attribute2 }}";
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        for (Token token : tokens) {
            System.out.println(token);
        }
        
        assertEquals(8, tokens.size());
        assertEquals(TokenType.CODE_START,tokens.get(0).type);
        assertEquals("attribute1",tokens.get(1).lexeme);
        assertEquals(TokenType.CODE_END,tokens.get(2).type);
        assertEquals(TokenType.STRING,tokens.get(3).type);
        assertEquals(TokenType.CODE_START,tokens.get(4).type);
        assertEquals("attribute2",tokens.get(5).lexeme);
        assertEquals(TokenType.CODE_END,tokens.get(6).type);
    }

}
