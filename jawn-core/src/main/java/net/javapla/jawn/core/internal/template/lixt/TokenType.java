package net.javapla.jawn.core.internal.template.lixt;

enum TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    
    BANG, BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    
    STRING, NUMBER, IDENTIFIER, NIL,
    
    IF, TRUE, FALSE,
    
    CODE_START, CODE_END,
    
    EOF
}