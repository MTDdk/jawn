package net.javapla.jawn.core.server;

import java.util.List;
import java.util.Optional;

import net.javapla.jawn.core.Context;

public interface WebSocket {
    
    interface Initialiser {
        void init(Context.Request req, WebSocketConfigurer init);
    }

    interface OnConnect {
        void onConnect(WebSocket ws);
    }
    
    interface OnMessage {
        void onMessage(WebSocket ws, WebSocketMessage message);
    }
    
    interface OnClose {
        void onClose(WebSocket ws, WebSocketCloseStatus status);
    }
    
    interface OnError {
        void onError(WebSocket ws, Throwable cause);
    }
    
    /** Max message size for websocket (13k) */
    int MAX_BUFFER_SIZE = 131_072;
    
    
    //Context context();
    
    
    /*default WebSocket attribute(String name, Object value) {
        context().attribute(name, value);
        return this;
    }
    default Optional<Object> attribute(String name) {
        return context().attribute(name);
    }
    default <T> Optional<T> attribute(String name, Class<T> type) {
        return context().attribute(name, type);
    }*/
    
    
    
    WebSocket send(String message, boolean broadcast);
    WebSocket send(byte[] message, boolean broadcast);
    default WebSocket send(String message) {
        return send(message, false);
    }
    default WebSocket send(byte[] message) {
        return send(message, false);
    }

    
    boolean isOpen();
    
    /**
     * Websockets connected to the same path.
     * Does not include current websocket.
     * 
     * @return connected websockets or empty
     */
    List<WebSocket> sessions();
    
    
    /*WebSocket render(Object value, boolean broadcast);
    default WebSocket render(Object value) {
        return render(value, false);
    }*/
    
    
    WebSocket close(WebSocketCloseStatus status);
    default WebSocket close() {
        return close(WebSocketCloseStatus.NORMAL);
    }
}
