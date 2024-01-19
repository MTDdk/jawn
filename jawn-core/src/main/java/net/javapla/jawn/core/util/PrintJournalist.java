package net.javapla.jawn.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public class PrintJournalist extends Writer {
    
    private final Charset charset;
    private final OutputStream out;
    
    
    public PrintJournalist(OutputStream out, Charset charset) {
        this.out = out;
        this.charset = charset;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        byte[] bytes = new String(cbuf, off, len).getBytes(charset);
        out.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(String str) throws IOException {
        byte[] bytes = str.getBytes(charset);
        out.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        write(str.substring(off, len));
    }

    @Override
    public void write(int c) throws IOException {
        out.write((char) c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public Writer append(char c) throws IOException {
        out.write(c);
        return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null) {
            throw new NullPointerException("CharSequence");
        }
        write(csq.toString());
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        if (csq == null) {
            throw new NullPointerException("CharSequence");
        }
        append(csq.subSequence(start, end));
        return this;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
