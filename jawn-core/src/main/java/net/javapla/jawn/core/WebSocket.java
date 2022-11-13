package net.javapla.jawn.core;

public interface WebSocket {
    
    interface Initialiser {
        void init(Context.Request req, Listener init);
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
    
    interface Listener {
        Listener onConnect(WebSocket.OnConnect callback);
        
        Listener onMessage(WebSocket.OnMessage callback);
        
        Listener onError(WebSocket.OnError callback);
        
        Listener onClose(WebSocket.OnClose callback);
    }
    
    interface WebSocketMessage {
        
    }
    
    class WebSocketCloseStatus {
        
    }

}
