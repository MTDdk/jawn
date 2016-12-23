package net.javapla.jawn.core.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.javapla.jawn.core.http.Response;
import net.javapla.jawn.core.http.ResponseStream;

public class ServerResponseStream implements ResponseStream {
    
    private final Response response;
    private OutputStream stream;
    private Writer writer;

    ServerResponseStream(Response response) {
        this.response = response;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return (stream = response.outputStream());
    }

    @Override
    public Writer getWriter() throws IOException {
        return (writer = new OutputStreamWriter(getOutputStream()));
    }

    @Override
    public void close() throws IOException {
        if (writer != null)
            writer.close();
        else if (stream != null)
            stream.close();
        response.end();
    }
    
}
