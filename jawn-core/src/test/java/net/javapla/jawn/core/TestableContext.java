package net.javapla.jawn.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;

import net.javapla.jawn.core.WebSocket.Initialiser;
import net.javapla.jawn.core.util.MultiList;

public class TestableContext extends AbstractContext {
    
    public TestableContext() {
        super(SessionStore.EMPTY);
    }

    public TestableContext(SessionStore sessionStore) {
        super(sessionStore);
    }

    @Override
    public Request req() {
        return new AbstractContext.AbstractRequest() {
            
            @Override
            public void upgrade(Initialiser init) {}
            
            @Override
            public String queryString() {
                return null;
            }
            
            @Override
            public String path() {
                return null;
            }
            
            @Override
            public Form multipart() {
                return null;
            }
            
            @Override
            public HttpMethod httpMethod() {
                return null;
            }
            
            @Override
            public Value header(String name) {
                return null;
            }
            
            @Override
            public MultiList<String> headers() {
                return null;
            }
            
            @Override
            public long contentLength() {
                return 0;
            }
            
            @Override
            public Body body() {
                return null;
            }
            
            @Override
            public String address() {
                return null;
            }

            @Override
            public Value cookie(String name) {
                return null;
            }

            @Override
            public Map<String, String> cookies() {
                return null;
            }

            @Override
            public String protocol() {
                return null;
            }

            @Override
            public String scheme() {
                return null;
            }
        };
    }

    @Override
    public Response resp() {
        return new AbstractResponse() {
            
            @Override
            public OutputStream stream() {
                return null;
            }
            
            @Override
            public int status() {
                return 0;
            }
            
            @Override
            public Response status(int statusCode) {
                return this;
            }
            
            @Override
            public Response respond(FileChannel channel) {
                return this;
            }
            
            @Override
            public Response respond(InputStream stream) {
                return this;
            }
            
            @Override
            public Response respond(ByteBuffer data) {
                return this;
            }
            
            @Override
            public Response respond(Status status) {
                return this;
            }
            
            @Override
            public Response removeHeader(String name) {
                return this;
            }
            
            @Override
            public boolean isResponseStarted() {
                return false;
            }
            
            @Override
            public Response header(String name, String value) {
                return this;
            }
            
            @Override
            public Value header(String name) {
                return null;
            }
            
            @Override
            public Response contentType(MediaType type) {
                return this;
            }
            
            @Override
            public long contentLength() {
                return 0;
            }
            
            @Override
            public Response contentLength(long length) {
                return this;
            }
            
            @Override
            public MediaType contentType() {
                return null;
            }
            
            @Override
            public Charset charset() {
                return null;
            }
            
            @Override
            public Response charset(Charset encoding) {
                return this;
            }

            @Override
            public Response cookie(Cookie cookie) {
                return this;
            }
        };
    }

    @Override
    public String requestHeader(String name) {
        return null;
    }

}
