package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.xnio.IoUtils;

import io.undertow.connector.PooledByteBuffer;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;

public class UndertowStream implements IoCallback, Runnable {
    
    private final long length;
    private final ReadableByteChannel source;
    private final HttpServerExchange exchange;
    private final IoCallback callback;
    
    private final Sender sender;
    private PooledByteBuffer pooled;
    long total = 0;

    public UndertowStream(
                          final long len, 
                          final ReadableByteChannel source, 
                          final HttpServerExchange exchange,
                          final IoCallback callback) {
        this.length = len;
        this.source = source;
        this.exchange = exchange;
        this.callback = callback;
        
        this.sender = exchange.getResponseSender();
        this.pooled = exchange.getConnection().getByteBufferPool().allocate();
        
    }
    
    void start() {
        onComplete(exchange, sender);
    }
    

    @Override
    public void run() {
        ByteBuffer buffer = pooled.getBuffer();
        
        
        try {
            buffer.clear();
            int read = source.read(buffer);
            if (read == -1 || (length != -1 && total >= length)) {
                close();
                callback.onComplete(exchange, sender);
            } else {
                total += read;
                buffer.flip();
                if (length > 0) {
                    if (total > length) {
                        int limit = (int)(read - (total - length));
                        buffer.limit(limit);
                    }
                }
                sender.send(buffer, this);
            }
            
        } catch (IOException e) {
            onException(exchange, sender, e);
        }
    }

    @Override
    public void onComplete(HttpServerExchange exchange, Sender sender) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            run();
        }
    }

    @Override
    public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
        close();
        callback.onException(exchange, sender, exception);
    }
    
    private void close() {
        if (pooled != null) {
            pooled.close();
            pooled = null;
        }
        IoUtils.safeClose(source);
    }

}
