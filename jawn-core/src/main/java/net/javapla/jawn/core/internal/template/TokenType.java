package net.javapla.jawn.core.internal.template;

enum TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    
    BANG, BANG_EQUAL,
    
    STRING, NUMBER, IDENTIFIER,
    
    IF, TRUE, FALSE,
    
    EOF
}