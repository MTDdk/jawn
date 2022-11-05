package net.javapla.jawn.core.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Optional;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Value;

public class ReadOnlyContext implements Context {
    
    private static final String M = "The response has already been started";
    
    protected final Context c;
    protected final Response rs;
    
    public ReadOnlyContext(Context ctx) {
        this.c = ctx;
        
        this.rs = new Response() {
            
            @Override
            public boolean isResponseStarted() {
                return true;
            }

            @Override
            public Value header(String name) {
                return c.resp().header(name);
            }

            @Override
            public Response header(String name, String value) {
                throw new IllegalStateException(M);
            }

            @Override
            public Response status(int statusCode) {
                throw new IllegalStateException(M);
            }

            @Override
            public int status() {
                return c.resp().status();
            }

            @Override
            public Response removeHeader(String name) {
                throw new IllegalStateException(M);
            }

            @Override
            public Response contentType(MediaType type) {
                throw new IllegalStateException(M);
            }

            @Override
            public MediaType contentType() {
                return c.resp().contentType();
            }
            
            @Override
            public Response rendererContentType(MediaType type) {
                throw new IllegalStateException(M);
            }

            @Override
            public Response charset(Charset encoding) {
                throw new IllegalStateException(M);
            }

            @Override
            public Charset charset() {
                return c.resp().charset();
            }

            @Override
            public OutputStream stream() {
                throw new IllegalStateException(M);
            }

            @Override
            public Response respond(Status status) {
                throw new IllegalStateException(M);
            }

            @Override
            public Response respond(ByteBuffer data) {
                throw new IllegalStateException(M);
            }

            @Override
            public Response respond(InputStream stream) {
                throw new IllegalStateException(M);
            }

            @Override
            public Response respond(FileChannel channel) {
                throw new IllegalStateException(M);
            }
            
        };
    }

    @Override
    public Request req() {
        return c.req();
    }

    @Override
    public Response resp() {
        return this.rs;
    }

    @Override
    public Context attribute(String name, Object value) {
        c.attribute(name, value);
        return this;
    }

    @Override
    public Optional<Object> attribute(String name) {
        return c.attribute(name);
    }

    @Override
    public Context removeAttribute(String name) {
        c.removeAttribute(name);
        return this;
    }

}
