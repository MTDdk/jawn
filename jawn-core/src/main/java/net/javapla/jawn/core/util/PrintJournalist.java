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
        write(new String(cbuf, off, len));
    }

    @Override
    public void write(String str) throws IOException {
        byte[] bytes = str.getBytes(charset);
        out.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        if (off == 0 && str.length() == len) {
            write(str);
        } else {
            write(str.substring(off, len));
        }
    }

    @Override
    public void write(int c) throws IOException {
        char[] cbuf = new char[1];
        cbuf[0] = (char) c;
        write(cbuf, 0, 1);
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
