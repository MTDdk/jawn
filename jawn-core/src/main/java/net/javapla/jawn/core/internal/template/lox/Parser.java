package net.javapla.jawn.core.internal.template.lox;

import java.util.List;

class Parser {
    
    private final List<Token> tokens;
    
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    Expr parse() {
        try {
            return expression();
        } catch (ParseError e) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }
    
    private Expr equality() {
        Expr expr = comparison();
        
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expr comparison() {
        Expr expr = term();
        
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expr term() {
        Expr expr = factor();
        
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expr factor() {
        Expr expr = unary();
        
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        
        return primary();
    }
    
    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        
        if (match(TokenType.NUMBER, TokenType.STRING)) return new Expr.Literal(previous().literal);
        
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        
        throw error(peek(), "Expect expression.");
    }
    
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        
        throw error(peek(), message);
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            
            switch (peek().type) {
                case IF:
                    return;
                default:
                    break;
            }
            
            advance();
        }
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }
    
    private static class ParseError extends RuntimeException {}
}
