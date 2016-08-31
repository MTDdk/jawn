package net.javapla.jawn.core.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

public interface Resp {
    
    Optional<String> header(final String name);
    List<String> headers(String name);
    void header(String name, Iterable<String> values);
    void header(String name, String value);
    void send(byte[] bytes) throws Exception;
    void send(ByteBuffer buffer) throws Exception;
    void send(InputStream stream) throws Exception;
    void send(FileChannel channel) throws Exception;
    int statusCode();
    void statusCode(int code);
    boolean committed();
    void end();
    void reset();
    OutputStream outputStream();
    String contentType();
    void contentType(String contentType);
    void characterEncoding(String encoding);
    Optional<Charset> characterEncoding();

}
