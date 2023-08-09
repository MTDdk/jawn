package net.javapla.jawn.core.internal.template.lixt.source;

public class CharArraySource implements Source {

    protected final char[] source;
    
    protected int start = 0, current = 0, line = 1, posInLine = 0;
    
    public CharArraySource(String source) {
        this.source = source.toCharArray();
    }
    
    @Override
    public void advance() {
        if (current < source.length) {
            posInLine++;
            current++;
            
            while (current < source.length && source[current] == '\n') {
                line++;
                posInLine = 0;
                current++;
            }
        }
    }

    @Override
    public char consume() {
        char c = source[current];
        advance();
        return c;
        //return source[current++];
    }

    @Override
    public char peek() {
        if (isAtEnd()) return '\0';
        return source[current];
    }

    @Override
    public char peekNext() {
        if (current + 1 >= source.length) return '\0';
        return source[current + 1];
    }
    
    @Override
    public boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source[current] != expected) return false;
        
        //current++;
        advance();
        return true;
    }

    @Override
    public boolean isAtEnd() {
        return current >= source.length;
    }
    
    @Override
    public void mark() {
        start = current;
    }
    
    @Override
    public String substring() {
        return new String(source, start, current - start);
    }
    
    /*@Override
    public String consumeRest() {
        current = source.length;
        if (start == current) return "";
        return new String(source, start, current - start);
    }*/
    
    @Override
    public int current() {
        return current;
    }
    
    @Override
    public int line() {
        return line;
    }
    
    @Override
    public int posInLine() {
        return posInLine;
    }
}
