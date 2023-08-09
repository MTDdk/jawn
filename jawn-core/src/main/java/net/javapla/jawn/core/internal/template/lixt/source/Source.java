package net.javapla.jawn.core.internal.template.lixt.source;

public interface Source {
    
    void advance();
    int current();
    
    char consume();
    
    char peek();
    char peekNext();
    
    boolean match(char expected);
    
    boolean isAtEnd();

    void mark();
    
    String substring();
    
    int line();
    int posInLine();
    
    static Source of(String source) {
        return new CharArraySource(source);
    }
}
