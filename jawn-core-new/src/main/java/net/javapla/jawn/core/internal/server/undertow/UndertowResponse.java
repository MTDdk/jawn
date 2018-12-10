package net.javapla.jawn.core.internal.server.undertow;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import io.undertow.server.HttpServerExchange;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.server.Response;

public class UndertowResponse implements Response {

    public UndertowResponse(final HttpServerExchange exchange) {
        
    }

    @Override
    public Optional<String> header(String name) {
        return null;
    }

    @Override
    public List<String> headers(String name) {
        return null;
    }

    @Override
    public void header(String name, List<String> values) {}

    @Override
    public void header(String name, String value) {}

    @Override
    public void removeHeader(String name) {}

    @Override
    public void send(byte[] bytes) throws Exception {}

    @Override
    public void send(ByteBuffer buffer) throws Exception {}

    @Override
    public void send(InputStream stream) throws Exception {}

    @Override
    public void send(FileChannel channel) throws Exception {}

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public void statusCode(int code) {}

    @Override
    public boolean committed() {
        return false;
    }

    @Override
    public void end() {}

    @Override
    public void reset() {}

    @Override
    public Writer writer() {
        return null;
    }

    @Override
    public OutputStream outputStream() {
        return null;
    }

    @Override
    public boolean usingStream() {
        return false;
    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public void contentType(String contentType) {}

    @Override
    public void addCookie(Cookie cookie) {}

    @Override
    public void characterEncoding(String encoding) {}

    @Override
    public Optional<Charset> characterEncoding() {
        return null;
    }

}
