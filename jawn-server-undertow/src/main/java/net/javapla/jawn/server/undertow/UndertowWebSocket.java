package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xnio.IoUtils;

import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Server;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.WebSocket;

class UndertowWebSocket extends AbstractReceiveListener implements WebSocket, WebSocket.Listener, WebSocketCallback<Void> {
    
    // The specification for websockets states that we have to keep track of all clients
    // (this might be handled by the WebSocketProtocolHandshakeHandler (which hands over the peers/clients to WebSocketChannel))
    //private static final ConcurrentHashMap<String, List<WebSocket>> SESSIONS = new ConcurrentHashMap<>();
    

    private final CountDownLatch ready = new CountDownLatch(1);
    private final AtomicBoolean open = new AtomicBoolean(false);
    
    private WebSocket.OnConnect onConnect;
    private WebSocket.OnMessage onMessage;
    private WebSocket.OnError   onError;
    private WebSocket.OnClose   onClose;
    
    private final UndertowContext context;
    private final WebSocketChannel channel;

    public UndertowWebSocket(UndertowContext context, WebSocketChannel channel) {
        this.context = context;
        this.channel = channel;
    }

    /* WebSocket.Initialiser */
    @Override
    public WebSocket.Listener onConnect(OnConnect callback) {
        onConnect = callback;
        return this;
    }

    @Override
    public WebSocket.Listener onMessage(OnMessage callback) {
        onMessage = callback;
        return this;
    }

    @Override
    public WebSocket.Listener onError(OnError callback) {
        onError = callback;
        return this;
    }

    @Override
    public WebSocket.Listener onClose(OnClose callback) {
        onClose = callback;
        return this;
    }
    
    /* WebSocket */
    @Override
    public boolean isOpen() {
        return open.get() && channel.isOpen();
    }
    
    @Override
    public Context context() {
        return context;
    }
    
    @Override
    public WebSocket send(String message, boolean broadcast) {
        return send(message.getBytes(StandardCharsets.UTF_8) ,broadcast);
    }
    
    @Override
    public WebSocket send(byte[] message, boolean broadcast) {
        if (broadcast) {
            for (WebSocketChannel peer : channel.getPeerConnections()) {
                if (peer.isOpen())
                    WebSockets.sendText(ByteBuffer.wrap(message), peer, this);
            }
        } else {
            if (isOpen())
                WebSockets.sendText(ByteBuffer.wrap(message), channel, this);
        }
        return this;
    }
    
    public void ping() {
        WebSockets.sendPing(ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8)), channel, this);
    }

    void fireConnected() {
        open.set(true);
        
        // handle sessions
        // If this was not already handled by Undertow, we could do it here
        
        // timeout - read some configs
        long timeout = TimeUnit.MINUTES.toMillis(5);
        if (timeout > 0) {
            channel.setIdleTimeout(timeout);
        }
        
        channel.getReceiveSetter().set(this);
        channel.resumeReceives();
        
        if (onConnect != null) {
            context.dispatch(wrap(() -> onConnect.onConnect(this)));
        }
        
        ready.countDown();
    }
    
    
    /* AbstractReceiveListener */
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        waitForConnect();
        
        if (onMessage != null) {
            context.dispatch(wrap(() -> onMessage.onMessage(this, WebSocket.WebSocketMessage.create(message.getData(), StandardCharsets.UTF_8))));
        }
    }
    // TODO handle binary messages?
    
    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
        // should just close?
        if (Server.connectionResetByPeer(error) || Up.isFatal(error)) {
            if (isOpen()) {
                handleClose(WebSocketCloseStatus.SERVER_ERROR);
            }
        }
        
        if (onError == null) {
            if (Server.connectionResetByPeer(error)) {
                context.error("WebSocket connection lost", error);
            } else {
                context.error("WebSocket resulted in exception", error);
            }
        } else  {
            onError.onError(this, error);
        }
        
        if (Up.isFatal(error)) throw Up.IO(error);
    }
    
    @Override
    protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
        if (isOpen()) {
            handleClose(WebSocketCloseStatus.valueOf(cm.getCode()).orElseGet(() -> new WebSocketCloseStatus(cm.getCode(), cm.getReason())));
        }
    }
    
    @Override
    protected long getMaxTextBufferSize() {
        return WebSocket.MAX_BUFFER_SIZE; // TODO read from config.hasPath("websocket.maxSize")
    }
    
    @Override
    protected long getMaxBinaryBufferSize() {
        return WebSocket.MAX_BUFFER_SIZE;
    }
    
    /* WebSocketCallback<Void> */
    @Override
    public void complete(WebSocketChannel channel, Void context) {
        IoUtils.safeClose(channel);
    }
    
    @Override
    public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
        IoUtils.safeClose(channel);
        UndertowWebSocket.this.onError(channel, throwable);
    }
    
    
    private void waitForConnect() {
        try {
            ready.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable e) {
                onError(null, e);
            }
        };
    }
    
    private void handleClose(WebSocket.WebSocketCloseStatus status) {
        if (isOpen()) {
            open.set(false);
            
            // send close
            if (!channel.isCloseFrameSent()) { // TODO this check necessary?
                WebSockets.sendClose(status.code(), status.reason(), channel, this);
            }
        }
        
        try {
            // fire
            if (onClose != null) {
                // By utilising AtomicReference for "onError" like such:
                // OnClose ref = AtomicReference<OnClose>.getAndSet(null); ref.onClose(this, status);
                // we could guarantee that the callback only gets called once.
                // But it is - until further notice - deemed not necessary
                
                onClose.onClose(this, status);
            }
        } catch (Throwable e) {
            onError(channel, e);
        }/* finally {
            // remove session ("if we had any!")
        }*/
    }
    
    
    static void newConnection(WebSocket.Initialiser init, UndertowContext context, HttpServerExchange exchange) {
        // Save for later use in the WebSocketProtocolHandshakeHandler
        exchange.putAttachment(CONTEXT_KEY, context);
        
        
        WebSocketProtocolHandshakeHandler handler = SOCKET_HANDLERS.computeIfAbsent(init, p -> 
            // "Handlers.websocket" handles all the handshaking and upgrading.
            // Automatically responds with a 404 if the headers are not correctly set
            Handlers.websocket((wsexchange, channel) -> { // WebSocketConnectionCallback.onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel)
                
                // Fetch the current context that we saved in the exchange earlier.
                // This makes the resulting WebSocketProtocolHandshakeHandler of this method
                // context unaware and therefore reusable, which in turn makes it able for us to
                // utilise the already built-in mechanisms for peer connections 
                // (as these are saved within WebSocketProtocolHandshakeHandler)
                UndertowContext current = wsexchange.getAttachment(CONTEXT_KEY);
                UndertowWebSocket socket = new UndertowWebSocket(current, channel);
                init.init(current.req(), socket);
                socket.fireConnected();
                
                
                // Cleanup task
                // Whenever a channel is closed, have a look at how many other channels for the same handler/route are currently
                // available and remove the WebSocketProtocolHandshakeHandler if it is no longer needed.
                channel.addCloseTask(ch -> { if (ch.getPeerConnections().size() == 0) SOCKET_HANDLERS.remove(init); });
            })
        );
    
        try {
            handler.handleRequest(exchange);
        } catch (Exception e) {
            throw Up.IO(e);
        }
    }
    // Assuming that lambdas and other implementations of WebSocket.Initialiser have a
    // fixed hash/reference, this can be used as an identifier for the WebSocketProtocolHandshakeHandler,
    // which in turn will keep a list of its current sessions.
    // This eliminates the need for storing the handlers based on the Route.originalPath, as the WebSocket.Initialiser
    // already refers to this single Route
    final static ConcurrentHashMap<WebSocket.Initialiser, WebSocketProtocolHandshakeHandler> SOCKET_HANDLERS = new ConcurrentHashMap<>(2);
    final static AttachmentKey<UndertowContext> CONTEXT_KEY = AttachmentKey.create(UndertowContext.class);
}
