package net.javapla.jawn.core.internal.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LoxScanner {
    
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.IF);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
    }
    
    
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    
    private int start = 0, current = 0, line = 1;

    LoxScanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme
            start = current;
            scanToken();
        }
        
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }
    
    // Scan string literals until a code block is found.
    // A bit reverse of ordinary code scanning
    
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            //case '': addToken(TokenType.); break;
            case '!': 
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); 
                break;
            case '/': 
                if (match('/')) {
                    // A comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
                
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace;
                break;
            case '\n':
                line++;
                break;
                
            case '"': string(); break;
            
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }
    
    private char advance() {
        return source.charAt(current++);
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        return true;
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        
        // The closing "
        advance();
        
        // Trim the surrounding quotes
        String value = source.substring(start + 1, current -1);
        addToken(TokenType.STRING, value);
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
    
    private void number() {
        while (isDigit(peek())) advance();
        
        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            
            while (isDigit(peek())) advance();
        }
        
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
