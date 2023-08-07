package net.javapla.jawn.core.internal.template.lox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.internal.template.lox.source.Source;

class TemplateTest {

    @Test
    void test() {
        String source = "<html> {{ attribute }} </html>";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        
        List<Token> tokens = template.tokenise();
        for (Token token : tokens) {
            System.out.println(token);
        }
        
    }

    @Test
    void endTest() {
        String source = "<html> {{ attribute }}";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        
        List<Token> tokens = template.tokenise();
        
        assertTrue(tokens.get(tokens.size() -2 ).type == TokenType.CODE_END);
        
        source = "<html> {{ attribute }} ";
        template = new TemplateScanner(Source.of(source));
        
        tokens = template.tokenise();
        
        assertTrue(tokens.get(tokens.size() -2 ).type == TokenType.STRING); // the space at the end
    }
    
    @Test
    void startTest() {
        String source = "{{ attribute }} </html>";
        TemplateScanner template = new TemplateScanner(Source.of(source));
        
        List<Token> tokens = template.tokenise();
        
        assertTrue(tokens.get(0).type == TokenType.CODE_START);
    }
}
