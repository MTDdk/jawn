package net.javapla.jawn.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

public class ResponseStreamServlet implements ResponseStream {

    private final HttpServletResponse response;
    
    public ResponseStreamServlet(HttpServletResponse response) {
        this.response = response;
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        return response.getWriter();
    }

}
