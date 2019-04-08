package net.javapla.jawn.core.internal.server.undertow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import net.javapla.jawn.core.server.ServerResponse;

public final class UndertowResponse implements ServerResponse {
    
    private final HttpServerExchange exchange;
    private final Runnable blocking;
    
    private volatile boolean endExchange = true;
    
    private boolean streamCreated = false;

    public UndertowResponse(final HttpServerExchange exchange) {
        this.exchange = exchange;
        this.blocking = () -> {if(!this.exchange.isBlocking()) this.exchange.startBlocking();};
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
        headers.putAll(HttpString.tryFromString(name), Collections.unmodifiableList(values));
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
        exchange.getResponseSender().send(buffer);
    }

    @Override
    public void send(final InputStream stream) throws Exception {
        endExchange = false;
        new ChunkedStream().send(Channels.newChannel(stream), exchange, IoCallback.END_EXCHANGE);
    }

    @Override
    public void send(final FileChannel channel) throws Exception {
        endExchange = false;
        new ChunkedStream().send(channel, exchange, IoCallback.END_EXCHANGE);
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
        if (endExchange)
            exchange.endExchange();
    }

    @Override
    public void reset() {
        exchange.getResponseHeaders().clear();
    }

    @Override
    public Writer writer() {
        return new OutputStreamWriter(outputStream());
    }

    @Override
    public OutputStream outputStream() {
        streamCreated = true;
        blocking.run();
        return exchange.getOutputStream();
    }

    @Override
    public boolean usingStream() {
        return streamCreated;
    }
    
    static class ChunkedStream implements IoCallback, Runnable {

        private ReadableByteChannel source;

        private HttpServerExchange exchange;

        private Sender sender;

        private PooledByteBuffer pooled;

        private IoCallback callback;

        private int bufferSize;

        private int chunk;
        
        private final long len;

        private long total;
        
        public ChunkedStream(final long len) {
            this.len = len;
        }

        public ChunkedStream() {
            this(-1);
        }

        public void send(final ReadableByteChannel source, final HttpServerExchange exchange, final IoCallback callback) {
            this.source = source;
            this.exchange = exchange;
            this.callback = callback;
            this.sender = exchange.getResponseSender();
            ServerConnection connection = exchange.getConnection();
            this.pooled = connection.getByteBufferPool().allocate();
            this.bufferSize = connection.getBufferSize();

            onComplete(exchange, sender);
        }

        @Override
        public void run() {
            ByteBuffer buffer = pooled.getBuffer();
            chunk += 1;
            try {
                buffer.clear();
                int count = source.read(buffer);
                if (count == -1 || (len != -1 && total >= len)) {
                    done();
                    callback.onComplete(exchange, sender);
                } else {
                    total += count;
                    if (chunk == 1) {
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
                    }
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
}
