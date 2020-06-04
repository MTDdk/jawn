package net.javapla.jawn.core;

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
