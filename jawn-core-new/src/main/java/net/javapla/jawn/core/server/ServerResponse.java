package net.javapla.jawn.core.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

public interface ServerResponse {
    
    Optional<String> header(final String name);
    List<String> headers(String name);
    void header(String name, List<String> values);
    void header(String name, String value);
    void removeHeader(String name);
    void send(byte[] bytes) throws Exception;
    void send(ByteBuffer buffer) throws Exception;
    void send(InputStream stream) throws Exception;
    void send(FileChannel channel) throws Exception;
    int statusCode();
    void statusCode(int code);
    boolean committed();
    void end();
    void reset();
    Writer writer();
    OutputStream outputStream();
    boolean usingStream();
}
