package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.WebSocket.Listener;
import net.javapla.jawn.core.WebSocket.OnClose;
import net.javapla.jawn.core.WebSocket.OnConnect;
import net.javapla.jawn.core.WebSocket.OnError;
import net.javapla.jawn.core.WebSocket.OnMessage;

class UndertowWebSocket extends AbstractReceiveListener implements WebSocket, WebSocket.Listener, WebSocketCallback<Void> {
    
    // The specification for websockets states that we have to keep track of all clients
    // (this might be handled by the WebSocketProtocolHandshakeHandler)
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
        open.set(true);
        
        if (onConnect != null) {
            onConnect.onConnect(this);
        }
        channel.getReceiveSetter().set(this);
        channel.resumeReceives();
    }

    @Override
    public void complete(WebSocketChannel channel, Void context) {}

    @Override
    public void onError(WebSocketChannel channel, Void context, Throwable throwable) {}
    
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        System.out.println(message);
    }
    
    private void waitForConnect() {
        try {
            ready.await();
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
    }
}
