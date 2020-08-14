package net.javapla.jawn.server.undertow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;

import io.undertow.connector.PooledByteBuffer;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerResponse;

final class UndertowResponse implements ServerResponse, IoCallback {
    
    private static Logger logger = LoggerFactory.getLogger(ServerResponse.class);
    
    private final HttpServerExchange exchange;
    
//    private volatile boolean endExchange = true;
//    private boolean streamCreated = false;

    public UndertowResponse(final HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public Optional<String> header(String name) {
        String value = exchange.getResponseHeaders().getFirst(name);
        return Optional.ofNullable(value);
    }

    @Override
    public List<String> headers(String name) {
        Objects.requireNonNull(name, "A header's name is required.");
        HeaderValues values = exchange.getResponseHeaders().get(name);
        return values == null ? Collections.emptyList() : values;
    }

    @Override
    public void header(String name, List<String> values) {
        HeaderMap headers = exchange.getResponseHeaders();
        headers.putAll(HttpString.tryFromString(name), /*Collections.unmodifiableList(*/values/*)*/);
    }

    @Override
    public void header(String name, String value) {
        exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
    }

    @Override
    public void removeHeader(String name) {
        exchange.getResponseHeaders().remove(name);
    }

    @Override
    public void send(final byte[] bytes) throws Exception {
        send(ByteBuffer.wrap(bytes));
    }

    @Override
    public void send(final ByteBuffer buffer) throws Exception {
//        endExchange = false;
        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, Long.toString(buffer.remaining()));
        exchange.getResponseSender().send(buffer, this);
    }

    @Override
    public void send(final InputStream stream) throws Exception {
//        endExchange = false;
        
        if (stream instanceof FileInputStream) {
            // use channel
            send(((FileInputStream) stream).getChannel());
            return;
        }
        
        chuncked();
        
        long len = exchange.getResponseContentLength();
        ByteRange range = ByteRange.parse(exchange.getRequestHeaders().getFirst(Headers.RANGE), len);
        range.apply(this);

        new ChunkedStream(len).send(Channels.newChannel(stream), exchange, this/*IoCallback.END_EXCHANGE*/);
    }

    @Override
    public void send(final FileChannel channel) throws Exception {
//        endExchange = false;
        
        long len = channel.size();
        exchange.setResponseContentLength(len);
        
        //chuncked(); TODO set this?
        
        ByteRange range = ByteRange.parse(exchange.getRequestHeaders().getFirst(Headers.RANGE), len);
        range.apply(this);
        
        channel.position(range.getStart());
        new ChunkedStream(range.getEnd()).send(channel, exchange, this/*IoCallback.END_EXCHANGE*/);
    }
    
    

    @Override
    public int statusCode() {
        return exchange.getStatusCode();
    }

    @Override
    public void statusCode(int code) {
        exchange.setStatusCode(code);
    }

    @Override
    public boolean committed() {
        return exchange.isResponseStarted();
    }

    @Override
    public void end() {
        /*NativeWebSocket ws = exchange.getAttachment(UndertowRequest.SOCKET);
        if (ws != null) {
          try {
            Handlers.websocket((wsExchange, channel) -> {
              ((UndertowWebSocket) ws).connect(channel);
            }).handleRequest(exchange);
          } catch (Exception ex) {
            log.error("Upgrade result in exception", ex);
          } finally {
            exchange.removeAttachment(UndertowRequest.SOCKET);
          }
        }*/
        
        // this is a noop when response has been set, still call it...
        /*if (endExchange || streamCreated) {
            exchange.endExchange();
        }*/
    }

    @Override
    public void reset() {
        exchange.getResponseHeaders().clear();
    }

    /*@Override
    public Writer writer(final Charset charset) {
        return new OutputStreamWriter(outputStream(), charset);
    }*/
    
    @Override
    public OutputStream outputStream() {
//        streamCreated = true;
        blocking();
        chuncked();
        return exchange.getOutputStream();
    }

    /*@Override
    public boolean usingStream() {
        return streamCreated;
    }*/
    
    @Override
    public void onComplete(HttpServerExchange exchange, Sender sender) {
        destroy(null);
    }
    @Override
    public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
        destroy(exception);
    }
    
    void destroy(Exception cause) {
        try {
            if (cause != null) {
                if (Server.connectionResetByPeer(cause)) {
                    logger.debug("exception found while sending response {} {}", exchange.getRequestMethod(), exchange.getRequestPath(), cause);
                } else {
                    logger.error("exception found while sending response {} {}", exchange.getRequestMethod(), exchange.getRequestPath(), cause);
                }
            }
        } finally {
            this.exchange.endExchange();
        }
    }
    
    private void blocking() { if (!this.exchange.isBlocking()) this.exchange.startBlocking(); }
    private void chuncked() {
        HeaderMap responseHeaders = exchange.getResponseHeaders();
        if (!responseHeaders.contains(Headers.CONTENT_LENGTH)) {
            exchange.getResponseHeaders().put(Headers.TRANSFER_ENCODING, Headers.CHUNKED.toString());
        }
    }

    static class ChunkedStream implements IoCallback, Runnable {

        private ReadableByteChannel source;

        private HttpServerExchange exchange;

        private Sender sender;

        private PooledByteBuffer pooled;

        private IoCallback callback;

        //private int bufferSize;

        //private int chunk;
        
        private final long len;

        private long total;
        
        public ChunkedStream(final long len) {
            this.len = len;
        }

        /*public ChunkedStream() {
            this(-1);
        }*/

        public void send(final ReadableByteChannel source, final HttpServerExchange exchange, final IoCallback callback) {
            this.source = source;
            this.exchange = exchange;
            this.callback = callback;
            this.sender = exchange.getResponseSender();
            ServerConnection connection = exchange.getConnection();
            this.pooled = connection.getByteBufferPool().allocate();
            //this.bufferSize = connection.getBufferSize();

            /*if (this.pooled == null) {
                System.out.println("-----------  Pooled");
                onException(exchange, sender, new IOException(new NullPointerException("pooled")));
            } else*/ {
                onComplete(exchange, sender);
            }
        }

        @Override
        public void run() {
            ByteBuffer buffer = pooled.getBuffer();
            //chunk += 1;
            try {
                buffer.clear();
                int count = source.read(buffer);
                if (count == -1 || (len != -1 && total >= len)) {
                    done();
                    callback.onComplete(exchange, sender);
                } else {
                    total += count;
                    /*if (chunk == 1) {
                        if (count < bufferSize) {
                            HeaderMap headers = exchange.getResponseHeaders();
                            if (!headers.contains(Headers.CONTENT_LENGTH)) {
                                headers.put(Headers.CONTENT_LENGTH, count);
                                headers.remove(Headers.TRANSFER_ENCODING);
                            }
                        } else {
                            HeaderMap headers = exchange.getResponseHeaders();
                            // just check if
                            if (!headers.contains(Headers.CONTENT_LENGTH)) {
                                headers.put(Headers.TRANSFER_ENCODING, "chunked");
                            }
                        }
                    }*/
                    buffer.flip();
                    if (len > 0) {
                        if (total > len) {
                            long limit = count - (total - len);
                            buffer.limit((int) limit);
                        }
                    }
                    sender.send(buffer, this);
                }
            } catch (IOException ex) {
                onException(exchange, sender, ex);
            }
        }

        @Override
        public void onComplete(final HttpServerExchange exchange, final Sender sender) {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
            } else {
                run();
            }
        }

        @Override
        public void onException(final HttpServerExchange exchange, final Sender sender,
                final IOException ex) {
            done();
            callback.onException(exchange, sender, ex);
        }

        private void done() {
            pooled.close();
            pooled = null;
            IoUtils.safeClose(source);
        }

    }

    static interface ByteRange {
        
        // range header prefix
        String BYTES_RANGE = "bytes=";
        
        /**
         * Start range or <code>-1</code>.
         *
         * @return Start range or <code>-1</code>.
         */
        long getStart();

        /**
         * End range or <code>-1</code>.
         *
         * @return End range or <code>-1</code>.
         */
        long getEnd();

        /**
         * New content length.
         *
         * @return New content length.
         */
        long getContentLength();

        /**
         * Value for <code>Content-Range</code> response header.
         *
         * @return Value for <code>Content-Range</code> response header.
         */
        String getContentRange();

        /**
         * For partial requests this method returns {@link Status#PARTIAL_CONTENT}.
         *
         * For not satisfiable requests this returns {@link Status#REQUESTED_RANGE_NOT_SATISFIABLE}..
         *
         * Otherwise just returns {@link Status#OK}.
         *
         * @return Status code.
         */
        Status getStatusCode();
        
        ByteRange apply(ServerResponse ctx);
        
        static ByteRange parse(final String headerValue, final long contentLength) {
            if (contentLength <= 0 || headerValue == null) {
                // NOOP
                return noByteRange(contentLength);
            }
            
            if ( ! headerValue.startsWith(BYTES_RANGE)) {
                //return notSatisfiableByteRange(headerValue, contentLength);
                throw new Up(Status.REQUESTED_RANGE_NOT_SATISFIABLE, headerValue);
            }

            try {
                long[] range = {-1, -1};
                int r = 0;
                int len = headerValue.length();
                int i = BYTES_RANGE.length();
                int offset = i;
                char ch;
                // Only Single Byte Range Requests:
                while (i < len && (ch = headerValue.charAt(i)) != ',') {
                    if (ch == '-') {
                        if (offset < i) {
                            range[r] = Long.parseLong(headerValue.substring(offset, i).trim());
                        }
                        offset = i + 1;
                        r += 1;
                    }
                    i += 1;
                }
                if (offset < i) {
                    if (r == 0) {
                        //return notSatisfiableByteRange(headerValue, contentLength);
                        throw new Up(Status.REQUESTED_RANGE_NOT_SATISFIABLE, headerValue);
                    }
                    range[r++] = Long.parseLong(headerValue.substring(offset, i).trim());
                }
                if (r == 0 || (range[0] == -1 && range[1] == -1)) {
                    //return notSatisfiableByteRange(headerValue, contentLength);
                    throw new Up(Status.REQUESTED_RANGE_NOT_SATISFIABLE, headerValue);
                }

                long start = range[0];
                long end = range[1];
                if (start == -1) {
                    start = contentLength - end;
                    end = contentLength - 1;
                }
                if (end == -1 || end > contentLength - 1) {
                    end = contentLength - 1;
                }
                if (start > end) {
                    //return notSatisfiableByteRange(headerValue, contentLength);
                    throw new Up(Status.REQUESTED_RANGE_NOT_SATISFIABLE, headerValue);
                }
                // offset
                long limit = (end - start + 1);
                return singleByteRange(start, limit, limit);
                
            } catch (NumberFormatException expected) {
                //return notSatisfiableByteRange(headerValue, contentLength);
                throw new Up(Status.REQUESTED_RANGE_NOT_SATISFIABLE, headerValue);
            }
        }
        
        static ByteRange noByteRange(final long contentLength) {
            return new ByteRange() {
                
                @Override
                public long getStart() {
                    return 0;
                }
                
                @Override
                public long getEnd() {
                    return contentLength;
                }
                
                @Override
                public long getContentLength() {
                    return contentLength;
                }
                
                @Override
                public String getContentRange() {
                    return "bytes */" + contentLength;
                }
                
                @Override
                public Status getStatusCode() {
                    return Status.OK;
                }
                
                @Override
                public ByteRange apply(ServerResponse ctx) {
                    return this;
                }
            };
        }
        
//        static ByteRange notSatisfiableByteRange(final String headerValue, final long contentLength) {
//            return new ByteRange() {
//                
//                @Override
//                public long getStart() {
//                    return -1;
//                }
//                
//                @Override
//                public long getEnd() {
//                    return -1;
//                }
//                
//                @Override
//                public long getContentLength() {
//                    return contentLength;
//                }
//                
//                @Override
//                public String getContentRange() {
//                    return "bytes */" + contentLength;
//                }
//                
//                @Override
//                public Status getStatusCode() {
//                    return Status.REQUESTED_RANGE_NOT_SATISFIABLE;
//                }
//            };
//        }
        
        static ByteRange singleByteRange(final long start, final long end, final long contentLength) {
            return new ByteRange() {
                
                @Override
                public long getStart() {
                    return start;
                }
                
                @Override
                public long getEnd() {
                    return end;
                }
                
                @Override
                public long getContentLength() {
                    return contentLength;
                }
                
                @Override
                public String getContentRange() {
                    return "bytes " + start + "-" + end + "/" + contentLength;
                }
                
                @Override
                public Status getStatusCode() {
                    return Status.PARTIAL_CONTENT;
                }
                
                @Override
                public ByteRange apply(ServerResponse ctx) {
                    ctx.header("Accept-Ranges", "bytes");
                    ctx.header("Content-Range", getContentRange());
                    ctx.header(Headers.CONTENT_LENGTH_STRING, Long.toString(contentLength));
                    ctx.statusCode(Status.PARTIAL_CONTENT.value());
                    
                    return this;
                }
            };
        }
    }
}
