package net.javapla.jawn.core.server;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Value;

public interface WebSocketMessage extends Value {
    
    static WebSocketMessage create(/*Context ctx, */String value) {
        return new WebSocketMessage() {
            
            @Override
            public String value() {
                return value;
            }
            
            @Override
            public boolean isPresent() {
                return true;
            }
        };
    }

}
