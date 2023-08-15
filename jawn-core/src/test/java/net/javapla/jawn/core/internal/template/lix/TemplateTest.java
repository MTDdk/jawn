package net.javapla.jawn.core.internal.template.lix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.internal.template.lix.TemplateScanner;
import net.javapla.jawn.core.internal.template.lix.Token;
import net.javapla.jawn.core.internal.template.lix.TokenType;
import net.javapla.jawn.core.internal.template.lix.source.Source;

class TemplateTest {

    @Test
    void test() {
        String source = "<html> {{ attribute }} </html>";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        
        List<Token> tokens = template.tokenise();
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
    void endTest() {
        String source = "<html> {{ attribute }} ";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        
        List<Token> tokens = template.tokenise();
        
        assertTrue(tokens.get(tokens.size() -2 ).type == TokenType.STRING); // the space at the end
    }
    
    @Test
    void noTextAtEnd() {
        String source = "<html> {{ attribute }}";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        
        List<Token> tokens = template.tokenise();
        
        assertTrue(tokens.get(tokens.size() -2 ).type == TokenType.CODE_END);
        assertEquals(5, tokens.size());
        assertEquals("<html> ",tokens.get(0).lexeme);
        assertEquals(TokenType.CODE_START,tokens.get(1).type);
        assertEquals("attribute",tokens.get(2).lexeme);
        assertEquals(TokenType.CODE_END,tokens.get(3).type);
    }
    
    @Test
    void startTest() {
        String source = "{{ attribute }} </html>";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        List<Token> tokens = template.tokenise();
        
        assertTrue(tokens.get(0).type == TokenType.CODE_START);
    }
    
    @Test
    void justCode() {
        String source = "{{attribute1}} {{ attribute2 }}";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        List<Token> tokens = template.tokenise();
        
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
