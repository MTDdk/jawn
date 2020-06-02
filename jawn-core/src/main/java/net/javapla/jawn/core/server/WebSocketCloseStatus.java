package net.javapla.jawn.core.server;

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
    
    
    private final int code;
    private final String reason;
    
    public WebSocketCloseStatus(int code, @Nullable String reason) {
        this.code = code;
        this.reason = reason;
    }
    
    public int code() { return code; }
    public @Nullable String reason() { return reason; }
}
