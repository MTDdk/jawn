package net.javapla.jawn.core.util;

import java.io.IOException;
import java.io.Writer;

public class StringBuilderWriter extends Writer {
    
    private StringBuilder bob;
    
    public StringBuilderWriter() {
        bob = new StringBuilder();
        lock = bob;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        bob.append(cbuf, off, len);
    }
    
    @Override
    public void write(String str) throws IOException {
        bob.append(str);
    }
    
    @Override
    public void write(char[] cbuf) throws IOException {
        bob.append(cbuf);
    }
    
    @Override
    public void write(int c) throws IOException {
        bob.append((char)c);
    }
    
    @Override
    public void write(String str, int off, int len) throws IOException {
        bob.append(str, off, off+len);
    }

    @Override
    public void flush() throws IOException {
        
    }

    @Override
    public void close() throws IOException {
        
    }
    
    @Override
    public String toString() {
        return bob.toString();
    }

}
