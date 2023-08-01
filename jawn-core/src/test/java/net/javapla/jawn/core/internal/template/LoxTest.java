package net.javapla.jawn.core.internal.template;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class LoxTest {

    @Test
    void test() {
        String source = "<html>{{attribute}}</html>";
        LoxScanner scanner = new LoxScanner(source);
        List<Token> tokens = scanner.scan();
        for (Token token : tokens) {
            System.out.println(token);
        }
        
        assertEquals(6, tokens.size());
        assertEquals("<html>",tokens.get(0).lexeme);
    }

}
