package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

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

class UndertowWebSocket extends AbstractReceiveListener implements WebSocket.Listener, WebSocketCallback<Void> {

    private final UndertowContext context;
    private final WebSocketChannel channel;
    
    private final CountDownLatch ready = new CountDownLatch(1);

    public UndertowWebSocket(UndertowContext context, WebSocketChannel channel) {
        this.context = context;
        this.channel = channel;
    }

    @Override
    public Listener onConnect(OnConnect callback) {
        return null;
    }

    @Override
    public Listener onMessage(OnMessage callback) {
        return null;
    }

    @Override
    public Listener onError(OnError callback) {
        return null;
    }

    @Override
    public Listener onClose(OnClose callback) {
        return null;
    }

    void fireConnected() {
        
    }

    @Override
    public void complete(WebSocketChannel channel, Void context) {}

    @Override
    public void onError(WebSocketChannel channel, Void context, Throwable throwable) {}
    
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        
    }
    
    private void waitForConnect() {
        try {
            ready.await();
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
    }
}
