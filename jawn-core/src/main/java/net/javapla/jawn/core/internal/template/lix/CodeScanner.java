package net.javapla.jawn.core.internal.template.lix;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.core.internal.template.lix.TemplateScanner.BlockPredicate;
import net.javapla.jawn.core.internal.template.lix.source.Source;

// Tokeniser?
public class CodeScanner {
    
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("{{", TokenType.CODE_START);
        keywords.put("}}", TokenType.CODE_END);
        keywords.put("if", TokenType.IF);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
    }
    
    private final Source source;
    private final BlockPredicate end;
    private final List<Token> tokens = new LinkedList<>();

    public CodeScanner(Source source, BlockPredicate end) {
        this.source = source;
        this.end = end;
    }
    
    List<Token> tokenise() {
        
        while (!source.isAtEnd() && !end.test(source)) {
            source.mark();
            scan();
        }
        
        return tokens;
    }

    private void scan() {
        char c = source.consume();
        switch (c) {
            case '(':
                break;
                
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace;
                break;
                
            default:
                if (isAlphaNumeric(c)) {
                    identifier();
                } else {
                    Lix.error(source.line(), "Unexpected character.");
                }
                break;
        }
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private void identifier() {
        while (isAlphaNumeric(source.peek())) source.advance();
        
        String text = source.substring();
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }
    
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring();
        tokens.add(new Token(type, text, literal, source.line(), source.posInLine()));
        
    }
}
