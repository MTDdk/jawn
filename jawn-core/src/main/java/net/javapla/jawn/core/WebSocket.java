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
    
    class WebSocketMessage {
        
    }
    
    class WebSocketCloseStatus {
        
    }

    /** Max message size for websocket (128K). */
    int MAX_BUFFER_SIZE = 128 * 1024;
    
    class WebSocketHandler implements Route.Handler {
        private WebSocket.Initialiser initialiser;

        public WebSocketHandler(WebSocket.Initialiser initialiser) {
          this.initialiser = initialiser;
        }

        @Override
        public Object handle(Context ctx) throws Exception {
            // Only GET is supported to start the handshake, but we assume this has been dealt with prior to getting this far
            
            boolean webSocket = ctx.req().header("Upgrade").value("").equalsIgnoreCase("websocket");
            if (webSocket) {
                ctx.req().upgrade(initialiser);
            }
            if (!ctx.resp().isResponseStarted()) {
                ctx.resp().status(Status.NOT_FOUND);
            }
            
            //return Results.status(Status.OK/*ACCEPTED*/);//.contentType(MediaType.JSON);//TODO
            //return ctx;
            return null;
        }
        
        private static final long serialVersionUID = 706588927746912140L;
    }
}
