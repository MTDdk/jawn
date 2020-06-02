package net.javapla.jawn.server.undertow;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import net.javapla.jawn.core.server.WebSocket;
import net.javapla.jawn.core.server.WebSocket.OnClose;
import net.javapla.jawn.core.server.WebSocket.OnConnect;
import net.javapla.jawn.core.server.WebSocket.OnError;
import net.javapla.jawn.core.server.WebSocket.OnMessage;
import net.javapla.jawn.core.server.WebSocketConfigurer;

public class UndertowWebSocket implements WebSocketConfigurer/*, WebSocket*/ {
    
    private static final ConcurrentMap<String, List<WebSocket>> ALL = new ConcurrentHashMap<>();
    
    private final UndertowRequest req;
    private final WebSocketChannel channel;
    private final boolean dispatch;

    public UndertowWebSocket(UndertowRequest req, WebSocketChannel channel) {
        this.req = req;
        this.channel = channel;
        this.dispatch = req.isInIoThread();
        
    }

    @Override
    public WebSocketConfigurer onConnect(OnConnect callback) {
        return null;
    }

    @Override
    public WebSocketConfigurer onMessage(OnMessage callback) {
        return null;
    }

    @Override
    public WebSocketConfigurer onError(OnError callback) {
        return null;
    }

    @Override
    public WebSocketConfigurer onClose(OnClose callback) {
        return null;
    }

}
