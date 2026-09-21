package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import net.javapla.jawn.core.Server;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.WebSocket;

public class UtowWS extends AbstractReceiveListener implements WebSocket, WebSocket.Listener {
    
    private final CountDownLatch ready = new CountDownLatch(1);
    private final AtomicBoolean open = new AtomicBoolean(false);
    
    private final UndertowContext ctx;
    private final WebSocketChannel channel;
    private final boolean dispatch;
    private final Server.ServerConfig config;
    
    private OnConnect onConnectCallback;
    private OnMessage onMessageCallback;
    private OnError onErrorCallback;
    private OnClose onCloseCallback;

    public UtowWS(UndertowContext ctx, WebSocketChannel channel, Server.ServerConfig config) {
        this.ctx = ctx;
        this.channel = channel;
        this.dispatch = !ctx.isInIoThread();
        this.config = config;
    }
    
    @Override
    public Listener onConnect(OnConnect callback) {
        onConnectCallback = callback;
        return this;
    }

    @Override
    public Listener onMessage(OnMessage callback) {
        onMessageCallback = callback;
        return this;
    }

    @Override
    public Listener onError(OnError callback) {
        onErrorCallback = callback;
        return this;
    }

    @Override
    public Listener onClose(OnClose callback) {
        onCloseCallback = callback;
        return this;
    }

    @Override
    public boolean isOpen() {
        return open.get() && channel.isOpen();
    }

    @Override
    public WebSocket send(String message, WriteCallback callback) {
        return sendMessage(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)), FrameType.TEXT, callback);
    }

    @Override
    public WebSocket send(ByteBuffer message, WriteCallback callback) {
        return sendMessage(message, FrameType.TEXT, callback);
    }

    @Override
    public WebSocket binary(String message, WriteCallback callback) {
        return sendMessage(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)), FrameType.BINARY, callback);
    }

    @Override
    public WebSocket binary(ByteBuffer message, WriteCallback callback) {
        return sendMessage(message, FrameType.BINARY, callback);
    }

    @Override
    public WebSocket ping(String message, WriteCallback callback) {
        return sendMessage(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)), FrameType.PING, callback);
    }

    @Override
    public WebSocket ping(ByteBuffer message, WriteCallback callback) {
        return sendMessage(message, FrameType.PING, callback);
    }
    
    private WebSocket sendMessage(ByteBuffer buffer, FrameType type, WriteCallback callback) {
        return UtowWS.sendMessage(this, buffer, type, new WriteCallbackAdaptor(this, callback));
    }
    
    static WebSocket sendMessage(UtowWS ws, ByteBuffer buffer, FrameType type, WebSocketCallback<Void> callback) {
        if (ws.isOpen()) {
            try {
                switch (type) {
                    case BINARY:
                        WebSockets.sendBinary(buffer, ws.channel, callback);
                        break;
                    case TEXT:
                        WebSockets.sendText(buffer, ws.channel, callback);
                        break;
                    case PING:
                        WebSockets.sendPing(buffer, ws.channel, callback);
                        break;
                    case PONG:
                        WebSockets.sendPong(buffer, ws.channel, callback);
                        break;
                    default:
                        throw new IllegalStateException("Unknown frame type: " + type);
                }
            } catch (Throwable t) {
                ws.onError(ws.channel, t);
            }
        } else {
            ws.onError(ws.channel, new IllegalStateException("Attempting to send a message to a closed websocket"));
        }
        return ws;
    }

    @Override
    public WebSocket close(WebSocketCloseStatus status) {
        handleClose(status);
        return this;
    }
    
    @Override
    protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
        handleClose(WebSocket.WebSocketCloseStatus.valueOf(cm.getCode()).orElseGet(() -> new WebSocket.WebSocketCloseStatus(cm.getCode(), cm.getReason())));
    }
    
    private void handleClose(WebSocket.WebSocketCloseStatus status) {
        if (isOpen()) {
            open.set(false);
            
            // send close
            //if (!channel.isCloseFrameSent()) // TODO this check necessary?
            WebSockets.sendClose(status.code(), status.reason(), channel, new WebSocketCallback<UtowWS>() {
                
                @Override
                public void onError(WebSocketChannel channel, UtowWS ws, Throwable throwable) {
                    IoUtils.safeClose(channel);
                    ws.onError(channel, throwable);
                }
                
                @Override
                public void complete(WebSocketChannel channel, UtowWS ws) {
                    IoUtils.safeClose(channel);
                }
            }, this);
        }
        
        try {
            // fire
            if (onCloseCallback != null) {
                // By utilising AtomicReference for "onClose" like such:
                // OnClose ref = AtomicReference<OnClose>.getAndSet(null); ref.onClose(this, status);
                // we could guarantee that the callback only gets called once.
                // But this is - until further notice - deemed not necessary
                
                onCloseCallback.onClose(this, status);
            }
        } catch (Throwable e) {
            onError(channel, e);
        }/* finally {
            // TODO remove session ("if we had any!")
        }*/
    }
    
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        waitForConnect();
        
        if (onMessageCallback != null) {
            dispatch(task(() -> onMessageCallback.onMessage(this,  WebSocketMessage.create(message.getData())), false));
        }
    }
    
    private void waitForConnect() {
        try {
            ready.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void dispatch(Runnable r) {
        if (dispatch) {
            ctx.dispatch(r);
        } else {
            r.run();
        }
    }
    
    private Runnable task(Runnable task, boolean isInit) {
        return () -> {
            try {
                task.run();
            } catch (Throwable e) {
                onError(null, e);
            } finally {
                if (isInit) ready.countDown();
            }
        };
    }
    
    @Override
    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
        waitForConnect();
        
        if (onMessageCallback != null) {
            var data = message.getData();
            try {
                ByteBuffer buffer = WebSockets.mergeBuffers(data.getResource());
                dispatch(task(() -> onMessageCallback.onMessage(this,  WebSocketMessage.create(toArray(buffer))), false));
            } finally {
                data.free();
            }
        }
    }
    
    private byte[] toArray(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
    
    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
     // should just close?
        if (Server.connectionResetByPeer(error) || Up.isFatal(error)) {
            if (isOpen()) {
                handleClose(WebSocketCloseStatus.SERVER_ERROR);
            }
        }
        
        if (onErrorCallback == null) {
            if (Server.connectionResetByPeer(error)) {
                ctx.debug("WebSocket connection lost", error);
            } else {
                ctx.error("WebSocket resulted in exception", error);
            }
        } else  {
            onErrorCallback.onError(this, error);
        }
        
        if (Up.isFatal(error)) throw Up.IO(error);
    }
    
    void fireConnected() {
        // only once
        open.set(true);
        
        try {
        
            // TODO
            // handle sessions
            // If this was not already handled by Undertow, we could do it here
            
            // timeout - read some configs
            long timeout = 
                config.config.hasPath("websocket.idleTimeout") 
                ? config.config.getDuration("websocket.idleTimeout", TimeUnit.MILLISECONDS) 
                : TimeUnit.MINUTES.toMillis(5);
            channel.setIdleTimeout(timeout);
            
            if (onConnectCallback != null) {
                dispatch(task(() -> onConnectCallback.onConnect(this), true));
            } else {
                ready.countDown();
            }
            
            channel.getReceiveSetter().set(this);
            channel.resumeReceives();
        
        } catch (Throwable t) {
            onError(channel, t);
        }
        
    }
    
    enum FrameType {
        TEXT,
        BINARY,
        PING,
        PONG
        ;
    }
    
    private static class WriteCallbackAdaptor implements WebSocketCallback<Void> {
        
        protected final UtowWS ws;
        private final WriteCallback callback;
    
        WriteCallbackAdaptor(UtowWS ws, WebSocket.WriteCallback callback) {
            this.ws = ws;
            this.callback = callback;
        }
    
        @Override
        public void complete(WebSocketChannel channel, Void context) {
            callback.operationComplete(ws, null);
        }
    
        @Override
        public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
            try {
                //TODO use Context#error + #debug
                Logger logger = LoggerFactory.getLogger(getClass());
                if (Server.connectionResetByPeer(throwable)) {
                    //logger.debug("WebSocket {} exception", ws.);
                } else {
                    logger.error("");
                }
            } finally {
                callback.operationComplete(ws, throwable);
            }
        }
    }

}
