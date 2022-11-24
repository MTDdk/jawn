package net.javapla.jawn.core;

import java.nio.charset.Charset;
import java.util.Optional;

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
    
    

    /** Max message size for websocket (128K). */
    int MAX_BUFFER_SIZE = 128 * 1024;
    
    boolean isOpen();
    
    Context context();
    
    WebSocket send(String message, boolean broadcast);
    
    default WebSocket send(String message) {
        return send(message, false);
    }
    
    WebSocket send(byte[] message, boolean broadcast);
    
    // ** Attributes ** //
    default WebSocket attribute(final String name, final Object value) {
        context().attribute(name, value);
        return this;
    }
    default Optional<Object> attribute(final String name) {
        return context().attribute(name);
    }
    default WebSocket removeAttribute(final String name) {
        context().removeAttribute(name);
        return this;
    }
    
    
    
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

    class WebSocketMessage extends Body.ByteArrayBody {

        public WebSocketMessage(byte[] bytes) {
            super(bytes);
        }
        
        
        public static WebSocketMessage create(byte[] bytes) {
            return new WebSocketMessage(bytes);
        }
        
        public static WebSocketMessage create(String msg, Charset charset) {
            return new WebSocketMessage(msg.getBytes(charset));
        }
        
    }
    
    class WebSocketCloseStatus {
        /**
         * Normal closure; the connection successfully completed whatever purpose for which it was created.
         */
        public static final int NORMAL_CODE = 1_000;
        
        /**
         * The endpoint is going away, either because of a server failure or because the browser is 
         * navigating away from the page that opened the connection.
         */
        public static final int GOING_AWAY_CODE = 1_001;
        
        /**
         * The endpoint is terminating the connection due to a protocol error.
         */
        public static final int PROTOCOL_ERROR_CODE = 1_002;
        
        /**
         * The connection is being terminated because the endpoint received data of a type it cannot 
         * accept (for example, a text-only endpoint received binary data).
         */
        public static final int UNSUPPORTED_DATA_CODE = 1_003;
        
        /**
         * Used to indicate that a connection was closed abnormally (that is, with no close frame being sent) 
         * when a status code is expected.
         */
        public static final int ABNORMAL_CLOSURE_CODE = 1_006;
        
        /**
         * The endpoint is terminating the connection because a message was received that contained 
         * inconsistent data (e.g., non-UTF-8 data within a text message).
         * 
         * @apiNote also called "Invalid frame payload data" 
         * (https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent)
         */
        public static final int BAD_DATA_CODE = 1_007;
        
        /**
         * The endpoint is terminating the connection because it received a message that violates its policy. 
         * This is a generic status code, used when codes 1003 and 1009 are not suitable.
         */
        public static final int POLICY_VIOLATION_CODE = 1_008;
        
        /**
         * The endpoint is terminating the connection because a data frame was received that is too large.
         */
        public static final int TOO_BIG_TO_PROCESS_CODE = 1_009;
        
        /**
         * The client is terminating the connection because it expected the server to negotiate one or 
         * more extension, but the server didn't.
         */
        public static final int MISSING_EXTENSION_CODE = 1_010;
        
        /**
         * The server is terminating the connection because it encountered an unexpected condition 
         * that prevented it from fulfilling the request.
         */
        public static final int SERVER_ERROR_CODE = 1_011;
        
        /**
         * The server is terminating the connection because it is restarting.
         */
        public static final int SERVICE_RESTARTED_CODE = 1_012;
        
        /**
         * The server is terminating the connection due to a temporary condition, 
         * e.g. it is overloaded and is casting off some of its clients.
         */
        public static final int SERVICE_OVERLOAD_CODE = 1_013;
        
        /**
         * The server was acting as a gateway or proxy and received an invalid response from the upstream server. 
         * This is similar to 502 HTTP Status Code.
         */
        public static final int BAD_GATEWAY_CODE = 1_014;

        
        /**
         * Normal closure; the connection successfully completed whatever purpose for which it was created.
         */
        public static final WebSocketCloseStatus NORMAL = new WebSocketCloseStatus(NORMAL_CODE, "Normal");
        
        /**
         * The endpoint is going away, either because of a server failure or because the browser is 
         * navigating away from the page that opened the connection.
         */
        public static final WebSocketCloseStatus GOING_AWAY = new WebSocketCloseStatus(GOING_AWAY_CODE, "Going Away");
        
        /**
         * The endpoint is terminating the connection due to a protocol error.
         */
        public static final WebSocketCloseStatus PROTOCOL_ERROR = new WebSocketCloseStatus(PROTOCOL_ERROR_CODE, "Protocol error");

        /**
         * The connection is being terminated because the endpoint received data of a type it cannot 
         * accept (for example, a text-only endpoint received binary data).
         */
        public static final WebSocketCloseStatus UNSUPPORTED_DATA = new WebSocketCloseStatus(UNSUPPORTED_DATA_CODE, "Not acceptable");

        /**
         * Used to indicate that a connection was closed abnormally (that is, with no close frame being sent) 
         * when a status code is expected.
         */
        public static final WebSocketCloseStatus ABNORMAL_CLOSURE = new WebSocketCloseStatus(ABNORMAL_CLOSURE_CODE, "Harsh disconnect");
        
        /**
         * The endpoint is terminating the connection because a message was received that contained 
         * inconsistent data (e.g., non-UTF-8 data within a text message).
         * 
         * @apiNote also called "Invalid frame payload data" 
         * (https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent)
         */
        public static final WebSocketCloseStatus BAD_DATA = new WebSocketCloseStatus(BAD_DATA_CODE, "Bad data");

        /**
         * The endpoint is terminating the connection because it received a message that violates its policy. 
         * This is a generic status code, used when codes 1003 and 1009 are not suitable.
         */
        public static final WebSocketCloseStatus POLICY_VIOLATION = new WebSocketCloseStatus(POLICY_VIOLATION_CODE, "Policy violation");

        /**
         * The endpoint is terminating the connection because a data frame was received that is too large.
         */
        public static final WebSocketCloseStatus TOO_BIG_TO_PROCESS = new WebSocketCloseStatus(TOO_BIG_TO_PROCESS_CODE, "Too big to process");

        /**
         * The client is terminating the connection because it expected the server to negotiate one or 
         * more extension, but the server didn't.
         */
        public static final WebSocketCloseStatus MISSING_EXTENSION = new WebSocketCloseStatus(MISSING_EXTENSION_CODE, "Required extension");

        /**
         * The server is terminating the connection because it encountered an unexpected condition 
         * that prevented it from fulfilling the request.
         */
        public static final WebSocketCloseStatus SERVER_ERROR = new WebSocketCloseStatus(SERVER_ERROR_CODE, "Server error");

        /**
         * The server is terminating the connection because it is restarting.
         */
        public static final WebSocketCloseStatus SERVICE_RESTARTED = new WebSocketCloseStatus(SERVICE_RESTARTED_CODE, "Service restarted");

        /**
         * The server is terminating the connection due to a temporary condition, 
         * e.g. it is overloaded and is casting off some of its clients.
         */
        public static final WebSocketCloseStatus SERVICE_OVERLOAD = new WebSocketCloseStatus(SERVICE_OVERLOAD_CODE, "Service overload");
        
        /**
         * The server was acting as a gateway or proxy and received an invalid response from the upstream server. 
         * This is similar to 502 HTTP Status Code.
         */
        public static final WebSocketCloseStatus BAD_GATEWAY = new WebSocketCloseStatus(BAD_GATEWAY_CODE, "Bad gateway");
        
        
        private final int code;
        private final String reason;
        
        public WebSocketCloseStatus(int code, /*@Nullable*/ String reason) {
            this.code = code;
            this.reason = reason;
        }
        
        public int code() { return code; }
        public /*@Nullable*/ String reason() { return reason; }
        
        public static Optional<WebSocketCloseStatus> valueOf(int code) {
            switch (code) {
                case -1:
                case NORMAL_CODE:
                    return Optional.of(NORMAL);
                case GOING_AWAY_CODE:
                    return Optional.of(GOING_AWAY);
                case PROTOCOL_ERROR_CODE:
                    return Optional.of(PROTOCOL_ERROR);
                case UNSUPPORTED_DATA_CODE:
                    return Optional.of(UNSUPPORTED_DATA);
                case BAD_DATA_CODE:
                    return Optional.of(BAD_DATA);
                case POLICY_VIOLATION_CODE:
                    return Optional.of(POLICY_VIOLATION);
                case TOO_BIG_TO_PROCESS_CODE:
                    return Optional.of(TOO_BIG_TO_PROCESS);
                case MISSING_EXTENSION_CODE:
                    return Optional.of(MISSING_EXTENSION);
                case SERVER_ERROR_CODE:
                    return Optional.of(SERVER_ERROR);
                case SERVICE_RESTARTED_CODE:
                    return Optional.of(SERVICE_RESTARTED);
                case SERVICE_OVERLOAD_CODE:
                    return Optional.of(SERVICE_OVERLOAD);
                case BAD_GATEWAY_CODE:
                    return Optional.of(BAD_GATEWAY);
                default:
                    return Optional.empty();
            }
        }
        
        @Override
        public String toString() {
            StringBuilder bob = new StringBuilder();
            bob.append(code);
            if (reason != null) {
                bob.append("(").append(reason).append(")");
            }
            return bob.toString();
        }
    }
}
