package net.javapla.jawn.core.internal.template.lox;

import java.util.ArrayList;
import java.util.List;

import net.javapla.jawn.core.internal.template.lox.source.Source;

public class TemplateScanner {
    
    private final Source source;
    private final List<Token> tokens = new ArrayList<>();
    
    public TemplateScanner(Source source) {
        this.source = source;
    }
    
    List<Token> tokenise() {
        
        while (!source.isAtEnd()) {
            if (CODE_START.test(source)) {
                
                // What we have read so far
                addToken(TokenType.STRING);
                
                scanCode();
            }
            
            source.advance();
        }

        // Rest of the template
        addToken(TokenType.STRING);
        
        addToken(TokenType.EOF, "");
        
        return tokens;
    }
    
    private void scanCode() {
        // Indicate code block
        CODE_START.skip(source);
        addToken(TokenType.CODE_START);
        
        // Scan the code block
        tokens.addAll(new CodeScanner(source, CODE_END).tokenise());
        
        // Indicate code end
        CODE_END.skip(source);
        addToken(TokenType.CODE_END);
    }
    
    private void addToken(TokenType type) {
        String lexeme = source.substring();
        if (lexeme.isEmpty()) return;
        addToken(type, lexeme);
    }
    
    private void addToken(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, null, source.line(), source.posInLine()));
        source.mark();
    }
    
    static class BlockPredicate {
        private final char a;
        private final char b;

        public BlockPredicate(char a, char b) {
            this.a = a;
            this.b = b;
        }
        
        public boolean test(Source s) {
            return s.peek() == a && s.peekNext() == b;
        }
        
        public void skip(Source s) {
            s.advance();
            s.advance();
        }
    }
    static final BlockPredicate CODE_START = new BlockPredicate('{','{');
    static final BlockPredicate CODE_END = new BlockPredicate('}','}');
}
