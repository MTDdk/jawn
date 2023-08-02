package net.javapla.jawn.core.internal.template;

class Token {

    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line, posInLine;
    
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.posInLine = 0;
    }
    
    Token(TokenType type, String lexeme, Object literal, int line, int pos) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.posInLine = pos;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal + " (" + line + "," + posInLine + ")";
    }
}
