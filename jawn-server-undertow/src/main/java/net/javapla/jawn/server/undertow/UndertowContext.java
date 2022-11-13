package net.javapla.jawn.server.undertow;

import static io.undertow.server.handlers.form.FormDataParser.FORM_DATA;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Deque;

import io.undertow.Handlers;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.AbstractContext;
import net.javapla.jawn.core.Body;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.StreamUtil;

final class UndertowContext extends AbstractContext implements IoCallback {
    
    //private static final ByteBuffer EMPTY_BODY = ByteBuffer.allocate(0);
    
    private final HttpServerExchange exchange;
    
    final HttpMethod method;
    final String path;
    
    private Request req;
    private final Response resp;
    Body body = null;

    UndertowContext(HttpServerExchange exchange) {
        this.exchange = exchange;
        this.path = exchange.getRequestPath();
        
        //this.req = _req();
        this.resp = _resp();
        
        
        //this.method = HttpMethod._getMethod(exchange.getRequestMethod().toString()/*, () -> req.multipart()*/);
        final HttpString m = exchange.getRequestMethod();
        this.method = HttpMethod._getMethod(m::byteAt);
    }
    
    private Request _req() {
        return new AbstractRequest() {
            private MultiList<FormItem> multipart = null;
            
            @Override
            public String path() {
                return path;
            }

            @Override
            public HttpMethod httpMethod() {
                return method;
            }
            
            @Override
            public long contentLength() {
                return exchange.getRequestContentLength();
            }
            
            @Override
            public String queryString() {
                return exchange.getQueryString();
            }
            
            @Override
            public Value header(String name) {
                return Value.of(exchange.getRequestHeaders().get(name));
            }
            
            @Override
            public MultiList<FormItem> multipart() {
                if (multipart == null) {
                    
                    MultiList<FormItem> list = MultiList.empty();
                    formData(list);
                    
                    multipart = list;
                }
                
                return multipart;
            }
            
            /*@Override
            public InputStream stream() {
                startBlocking();
                return exchange.getInputStream();
            }*/
            
            @Override
            public Body body() {
                if (body == null) body = Body.empty(); 
                return body;
            }
            
            public void upgrade(WebSocket.Initialiser init) {
                try {
                    Handlers.websocket((exchange, channel) -> {
                        UndertowWebSocket socket = new UndertowWebSocket(UndertowContext.this, channel);
                        init.init(this, socket);
                        socket.fireConnected();
                    }).handleRequest(exchange);
                } catch (Exception e) {
                    throw Up.IO(e);
                }
            }
        };
    }
    
    private Response _resp() {
        return new AbstractResponse() {
            
            @Override
            public Value header(String name) {
                return Value.of(exchange.getResponseHeaders().get(name));
            }

            @Override
            public Response header(String name, String value) {
                exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
                return this;
            }
            
            @Override
            public Response removeHeader(String name) {
                exchange.getResponseHeaders().remove(name);
                return this;
            }
            
            @Override
            public Response status(int statusCode) {
                exchange.setStatusCode(statusCode);
                return this;
            }
            
            @Override
            public int status() {
                return exchange.getStatusCode();
            }
            
            @Override
            public Response contentType(MediaType type) {
                responseType = type;
                setContentType();
                return this;
            }
            
            @Override
            public MediaType contentType() {
                return defaultResponseType == null ? responseType : defaultResponseType;
            }
            

            protected void setContentType() {
                if (this.responseType != null) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, responseType + (cs != null ? (";charset=" + cs) : ""));
                }
            }
            
            @Override
            public Response charset(Charset encoding) {
                cs = encoding;
                setContentType();
                return this;
            }
            
            @Override
            public Charset charset() {
                return cs;
            }
            
            @Override
            public OutputStream stream() {
                startBlocking();
                setChunked();
                return exchange.getOutputStream();
            }
            
            /*@Override
            public PrintWriter writer(MediaType type, Charset charset) {
                startBlocking();
                //contentType(type);
                //charset(charset);
                setChunked();
                return new PrintWriter(new OutputStreamWriter(exchange.getOutputStream(), charset()));
            }*/
            
            // io.undertow.server.handlers.RedirectHandler
            public Response redirect_found(String location) {
                status(Status.FOUND);
                exchange.getResponseHeaders().put(Headers.LOCATION, location);
                exchange.getResponseSender().close(UndertowContext.this);
                return this;
            }
            
            @Override
            public Response respond(Status status) {
                status(status.value());
                //exchange.getResponseSender().send(EMPTY_BODY, UndertowContext.this);
                // Makes sure that the onComplete handler gets called
                exchange.getResponseSender().close(UndertowContext.this);
                return this;
            }
            
            @Override
            public Response respond(ByteBuffer data) {
                exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, Long.toString(data.remaining()));
                exchange.getResponseSender().send(data, UndertowContext.this);
                return this;
            }
            
            @Override
            public Response respond(InputStream stream) {
                if (stream instanceof FileInputStream) {
                    return respond(((FileInputStream)stream).getChannel());
                }
                    // TODO handle RANGE header
                    // Take note from io.undertow.server.handlers.ByteRangeHandler and io/undertow/conduits/ChunkedStreamSinkConduit.java
                    // as well as io/undertow/conduits/FixedLengthStreamSourceConduit.java
                
                    //long len = exchange.getResponseContentLength();
                    /*if (len == -1) { // the header did not exist
                        new UndertowStream(len, Channels.newChannel(stream), exchange, UndertowContext.this).start();
                        return this;
                    }*/
                    
                    /*final ByteRange range = ByteRange.parse(exchange.getRequestHeaders().getFirst(Headers.RANGE));
                    if (range != null && range.getRanges() == 1) {
                        stream.skip(range.getStart(0));
                        
                        String lastModified = exchange.getResponseHeaders().getFirst(Headers.LAST_MODIFIED);
                        ByteRange.RangeResponseResult rangeResponse = range.getResponseResult(len, exchange.getRequestHeaders().getFirst(Headers.IF_RANGE), lastModified == null ? null : DateUtils.parseDate(lastModified), exchange.getResponseHeaders().getFirst(Headers.ETAG));
                        //long start = rangeResponse.getStart();
                        //long end = rangeResponse.getEnd();
                        exchange.getResponseHeaders().put(Headers.CONTENT_RANGE, rangeResponse.getContentRange());
                        exchange.setStatusCode(rangeResponse.getStatusCode());
                        exchange.setResponseContentLength(rangeResponse.getContentLength());
                        if (rangeResponse.getStatusCode() == StatusCodes.REQUEST_RANGE_NOT_SATISFIABLE) {
                            //return new HeadStreamSinkConduit(factory.create(), null, true);
                            // TODO do a HEAD response, which discards any data
                        }
                        
                        new UndertowStream(range.getEnd(0), Channels.newChannel(stream), exchange, UndertowContext.this).start();
                        return this;
                    }
                    new UndertowStream(len, Channels.newChannel(stream), exchange, UndertowContext.this).start();*/
                    
                    
                setChunked();
                dispatch(() -> {
                        
                    try {
                        Sender sender = exchange.getResponseSender();
                        try ( PooledByteBuffer pool = exchange.getConnection().getByteBufferPool().allocate();
                              ReadableByteChannel channel = Channels.newChannel(stream)) {
                            
                            ByteBuffer buffer = pool.getBuffer();
                            buffer.clear();
                            while (channel.read(buffer) > 0) {
                                buffer.flip();
                                sender.send(buffer);
                                buffer.clear();
                            }
                        } finally {
                            sender.close(UndertowContext.this);
                        }
                        
                    } catch (IOException e) {
                        throw Up.IO(e);
                    }
                });
                
                return this;
            }
            
            @Override
            public Response respond(FileChannel channel) {
                // TODO handle byte-range
                /*setChunked();
                long len;
                try {
                    len = channel.size();
                    final ByteRange range = ByteRange.parse(exchange.getRequestHeaders().getFirst(Headers.RANGE));
                    if (range != null && range.getRanges() == 1) {
                        channel.position(range.getStart(0));
                        
                        String lastModified = exchange.getResponseHeaders().getFirst(Headers.LAST_MODIFIED);
                        ByteRange.RangeResponseResult rangeResponse = range.getResponseResult(len, exchange.getRequestHeaders().getFirst(Headers.IF_RANGE), lastModified == null ? null : DateUtils.parseDate(lastModified), exchange.getResponseHeaders().getFirst(Headers.ETAG));
                        //long start = rangeResponse.getStart();
                        //long end = rangeResponse.getEnd();
                        exchange.getResponseHeaders().put(Headers.CONTENT_RANGE, rangeResponse.getContentRange());
                        exchange.setStatusCode(rangeResponse.getStatusCode());
                        exchange.setResponseContentLength(rangeResponse.getContentLength());
                        
                        
                        channel.transferTo(range.getStart(0), range.getEnd(0), exchange.getResponseChannel());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                
                dispatch(() -> exchange.getResponseSender().transferFrom(channel, UndertowContext.this));
                return this;
            }
            
            @Override
            public boolean isResponseStarted() {
                return exchange.isResponseStarted();
            }
            
        };
    }

    @Override
    public Request req() {
        if (req == null) this.req = _req();
        return req;
    }

    @Override
    public Response resp() {
        return resp;
    }
    
    public String requestHeader(String name) { return exchange.getRequestHeaders().getFirst(name); };
    

    private void formData(MultiList<FormItem> list) {
        // Set by FormDataParser in the UndertowHandler
        FormData data = exchange.getAttachment(FORM_DATA);
        
        if (data != null) {
            data.iterator().forEachRemaining(path -> {
                Deque<FormData.FormValue> values = data.get(path);
                values.forEach(value -> {
                    
                    if (value.isFileItem()) {
                        list.put(path, Context.FormItem.of(path, new UndertowFileUpload(value)));
                    } else {
                        list.put(path, Context.FormItem.of(path, value.getValue()));
                    }
                    
                });
            });
        }
    }
    
    public static class UndertowFileUpload implements Context.FileUpload {
        final FormData.FormValue value;
        final FormData.FileItem item;
        
        UndertowFileUpload(FormData.FormValue value) {
            this.value = value;
            this.item = value.getFileItem();
        }
        
        @Override
        public InputStream stream() {
            try {
                return item.getInputStream();
            } catch (IOException e) {
                throw Up.IO(e);
            }
        }
        
        @Override
        public byte[] bytes() {
            try {
                return StreamUtil.bytes(stream());
            } catch (IOException e) {
                throw Up.IO(e);
            }
        }
        
        @Override
        public long fileSize() {
            try {
                return item.getFileSize();
            } catch (IOException e) {
                return -1;
            }
        }
        
        @Override
        public String fileName() {
            return value.getFileName();
        }
        
        @Override
        public Path path() {
            return item.getFile();
        }
        
        @Override
        public String contentType() {
            return value.getHeaders().getFirst(Headers.CONTENT_TYPE);
        }

        @Override
        public void close() {
            try {
                item.delete();
            } catch (IOException e) {
                throw Up.IO(e);
            }
        }
        
        @Override
        public String toString() {
            return fileName();
        }
    }
    
    
    private void setChunked() {
        HeaderMap headers = exchange.getResponseHeaders();
        if (!headers.contains(Headers.CONTENT_LENGTH)) {
            headers.put(Headers.TRANSFER_ENCODING, Headers.CHUNKED.toString());
        }
    }
    private void startBlocking() {
        if (! exchange.isBlocking()) {
            exchange.startBlocking();
        }
    }
    void dispatch(Runnable action) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(action);
        } else {
            action.run();
        }
    }

    
    /* *******************************
     * IoCallback
     ******************************* */
    @Override
    public void onComplete(HttpServerExchange exchange, Sender sender) {
        // save session
        // TODO
        // ctx.session.save
        this.exchange.endExchange();
    }

    @Override
    public void onException(HttpServerExchange exchange, Sender sender, IOException cause) {
        try {
            if (cause != null) {
                // do some logging
            }
        } finally {
            this.exchange.endExchange();
        }
    }
}
