package net.javapla.jawn.server.undertow;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

import io.undertow.server.HttpServerExchange;
import net.javapla.jawn.core.http.Resp;

public class UndertowResponse implements Resp {
    
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
    public void header(String name, Iterable<String> values) {
    }

    @Override
    public void header(String name, String value) {
    }

    @Override
    public void send(byte[] bytes) throws Exception {
    }

    @Override
    public void send(ByteBuffer buffer) throws Exception {
    }

    @Override
    public void send(InputStream stream) throws Exception {
    }

    @Override
    public void send(FileChannel channel) throws Exception {
    }

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public void statusCode(int code) {
    }

    @Override
    public boolean committed() {
        return false;
    }

    @Override
    public void end() {
    }

    @Override
    public void reset() {
    }

}
