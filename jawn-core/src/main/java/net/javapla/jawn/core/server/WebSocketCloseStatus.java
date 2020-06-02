package net.javapla.jawn.core.server;

import java.util.Optional;

import javax.annotation.Nullable;

public class WebSocketCloseStatus {
    
    public static final int NORMAL_CODE = 1_000;
    
    public static final int GOING_AWAY_CODE = 1_001;
    
    public static final int PROTOCOL_ERROR_CODE = 1_002;
    
    public static final int NOT_ACCEPTABLE_CODE = 1_003;
    
    public static final int HARSH_DISCONNECT_CODE = 1_006;
    
    public static final int BAD_DATA_CODE = 1_007;
    
    public static final int POLICY_VIOLATION_CODE = 1_008;
    
    public static final int TOO_BIG_TO_PROCESS_CODE = 1_009;
    
    public static final int REQUIRED_EXTENSION_CODE = 1_010;
    
    public static final int SERVER_ERROR_CODE = 1_011;
    
    public static final int SERVICE_RESTARTED_CODE = 1_012;
    
    public static final int SERVICE_OVERLOAD_CODE = 1_013;

    
    public static final WebSocketCloseStatus NORMAL = new WebSocketCloseStatus(NORMAL_CODE, "Normal");
    
    public static final WebSocketCloseStatus GOING_AWAY = new WebSocketCloseStatus(GOING_AWAY_CODE, "Going Away");
    
    public static final WebSocketCloseStatus PROTOCOL_ERROR = new WebSocketCloseStatus(PROTOCOL_ERROR_CODE, "Protocol error");

    public static final WebSocketCloseStatus NOT_ACCEPTABLE = new WebSocketCloseStatus(NOT_ACCEPTABLE_CODE, "Not acceptable");

    public static final WebSocketCloseStatus HARSH_DISCONNECT = new WebSocketCloseStatus(HARSH_DISCONNECT_CODE, "Harsh disconnect");

    public static final WebSocketCloseStatus BAD_DATA = new WebSocketCloseStatus(BAD_DATA_CODE, "Bad data");

    public static final WebSocketCloseStatus POLICY_VIOLATION = new WebSocketCloseStatus(POLICY_VIOLATION_CODE, "Policy violation");

    public static final WebSocketCloseStatus TOO_BIG_TO_PROCESS = new WebSocketCloseStatus(TOO_BIG_TO_PROCESS_CODE, "Too big to process");

    public static final WebSocketCloseStatus REQUIRED_EXTENSION = new WebSocketCloseStatus(REQUIRED_EXTENSION_CODE, "Required extension");

    public static final WebSocketCloseStatus SERVER_ERROR = new WebSocketCloseStatus(SERVER_ERROR_CODE, "Server error");

    public static final WebSocketCloseStatus SERVICE_RESTARTED = new WebSocketCloseStatus(SERVICE_RESTARTED_CODE, "Service restarted");

    public static final WebSocketCloseStatus SERVICE_OVERLOAD = new WebSocketCloseStatus(SERVICE_OVERLOAD_CODE, "Service overload");
    
    
    private final int code;
    private final String reason;
    
    public WebSocketCloseStatus(int code, @Nullable String reason) {
        this.code = code;
        this.reason = reason;
    }
    
    public int code() { return code; }
    public @Nullable String reason() { return reason; }
    
    public static Optional<WebSocketCloseStatus> valueOf(int code) {
        switch (code) {
            case -1:
            case NORMAL_CODE:
                return Optional.of(NORMAL);
            case GOING_AWAY_CODE:
                return Optional.of(GOING_AWAY);
            case PROTOCOL_ERROR_CODE:
                return Optional.of(PROTOCOL_ERROR);
            case NOT_ACCEPTABLE_CODE:
                return Optional.of(NOT_ACCEPTABLE);
            case BAD_DATA_CODE:
                return Optional.of(BAD_DATA);
            case POLICY_VIOLATION_CODE:
                return Optional.of(POLICY_VIOLATION);
            case TOO_BIG_TO_PROCESS_CODE:
                return Optional.of(TOO_BIG_TO_PROCESS);
            case REQUIRED_EXTENSION_CODE:
                return Optional.of(REQUIRED_EXTENSION);
            case SERVER_ERROR_CODE:
                return Optional.of(SERVER_ERROR);
            case SERVICE_RESTARTED_CODE:
                return Optional.of(SERVICE_RESTARTED);
            case SERVICE_OVERLOAD_CODE:
                return Optional.of(SERVICE_OVERLOAD);
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
