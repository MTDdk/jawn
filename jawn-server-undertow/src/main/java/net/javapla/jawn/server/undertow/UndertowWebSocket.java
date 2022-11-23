package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xnio.IoUtils;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import net.javapla.jawn.core.Server;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.WebSocket;

class UndertowWebSocket extends AbstractReceiveListener implements WebSocket, WebSocket.Listener {
    
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

    void fireConnected() {
        System.out.println("Before :: " + channel.getPeerConnections() );
        open.set(true);
        
        // handle sessions..? TODO
        
        // timeout - read some configs
        long timeout = TimeUnit.MINUTES.toMillis(5);
        if (timeout > 0) {
            channel.setIdleTimeout(timeout);
        }
        
        ready.countDown();
        channel.getReceiveSetter().set(this);
        channel.resumeReceives();
        
        if (onConnect != null) {
            context.dispatch(wrap(() -> onConnect.onConnect(this)));
        }
        
        System.out.println("After :: " + channel.getPeerConnections() );
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        System.out.println(message);
        
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
        System.out.println( channel.getPeerConnections() );
        if (isOpen()) {
            handleClose(WebSocketCloseStatus.valueOf(cm.getCode()).orElseGet(() -> new WebSocketCloseStatus(cm.getCode(), cm.getReason())));
        }
    }
    
    private boolean isOpen() {
        return open.get() && channel.isOpen();
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
                
            }
        };
    }
    
    private void handleClose(WebSocket.WebSocketCloseStatus status) {
        if (isOpen()) {
            open.set(false);
            
            // send close
            if (!channel.isCloseFrameSent()) { // TODO this check necessary?
                WebSockets.sendClose(status.code(), status.reason(), channel, new WSCallback());
            }
        }
        
        try {
            // fire
            if (onClose != null) {
                // By utilising AtomicReference for "onError" like such:
                // OnClose ref = AtomicReference<OnClose>.getAndSet(null);
                // we could guarantee that the callback only gets called once
                
                onClose.onClose(this, status);
            }
        } catch (Throwable e) {
            onError(channel, e);
        }/* finally {
            // remove session ("if we had any!")
        }*/
    }
    
    private class WSCallback implements WebSocketCallback<Void> {

        @Override
        public void complete(WebSocketChannel channel, Void context) {
            IoUtils.safeClose(channel);
        }

        @Override
        public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
            IoUtils.safeClose(channel);
            UndertowWebSocket.this.onError(channel, throwable);
        }
        
    }
}
