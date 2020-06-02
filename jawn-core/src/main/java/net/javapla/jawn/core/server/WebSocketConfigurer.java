package net.javapla.jawn.core.server;

public interface WebSocketConfigurer { // TODO rename
    
    WebSocketConfigurer onConnect(WebSocket.OnConnect callback);
    
    WebSocketConfigurer onMessage(WebSocket.OnMessage callback);
    
    WebSocketConfigurer onError(WebSocket.OnError callback);
    
    WebSocketConfigurer onClose(WebSocket.OnClose callback);

}
