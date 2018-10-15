package net.javapla.jawn.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.javapla.jawn.core.http.ResponseStream;

public class ResponseStreamWriter implements ResponseStream {

    private final Writer writer;
    
    public ResponseStreamWriter() {
        writer = new StringBuilderWriter();
    }
    
    public ResponseStreamWriter(Writer w) {
        writer = w;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public Writer getWriter() {
        return writer;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
