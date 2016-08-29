package net.javapla.jawn.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.javapla.jawn.core.http.Resp;
import net.javapla.jawn.core.http.ResponseStream;

public class ServerResponseStream implements ResponseStream {
    
    private final Resp response;

    ServerResponseStream(Resp response) {
        this.response = response;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.outputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        return new OutputStreamWriter(getOutputStream());
    }

}
