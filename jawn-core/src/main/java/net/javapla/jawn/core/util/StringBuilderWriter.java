package net.javapla.jawn.core.util;

import java.io.Writer;

public class StringBuilderWriter extends Writer {
    
    private StringBuilder bob;
    
    public StringBuilderWriter() {
        bob = new StringBuilder();
        lock = bob;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IndexOutOfBoundsException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        bob.append(cbuf, off, len);
    }
    
    @Override
    public void write(String str) {
        bob.append(str);
    }
    
    @Override
    public void write(char[] cbuf) {
        bob.append(cbuf);
    }
    
    @Override
    public void write(int c) {
        bob.append((char)c);
    }
    
    @Override
    public void write(String str, int off, int len) {
        bob.append(str, off, off+len);
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
    
    @Override
    public String toString() {
        return bob.toString();
    }

}
