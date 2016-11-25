package net.javapla.jawn.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import net.javapla.jawn.core.http.ResponseStream;

public final class ResponseStreamServlet implements ResponseStream {

    private final HttpServletResponse response;
    
    public ResponseStreamServlet(HttpServletResponse response) {
        this.response = response;
    }
    
    @Override
    public final OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public final Writer getWriter() throws IOException {
        return response.getWriter();
    }
    
    @Override
    public void close() throws IOException {
        response.flushBuffer();
    }

}
