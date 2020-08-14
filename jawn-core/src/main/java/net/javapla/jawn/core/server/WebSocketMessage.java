package net.javapla.jawn.core.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.util.StringUtil;

public interface WebSocketMessage extends Value.ByteArrayValue {
    
    static WebSocketMessage create(/*Context ctx, */String value) {
        return new WebSocketMessage() {
            
            @Override
            public String value() {
                return value;
            }
            
            @Override
            public boolean isPresent() {
                return !StringUtil.blank(value);
            }

            @Override
            public byte[] bytes() {
                return value.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public long size() {
                return value.getBytes().length;
            }

            @Override
            public boolean inMemory() {
                return true;
            }

            @Override
            public InputStream stream() {
                return new ByteArrayInputStream(bytes());
            }
        };
    }
    
    static WebSocketMessage create(byte[] bytes) {
        return new WebSocketMessage() {
            
            @Override
            public String value() {
                return value(StandardCharsets.UTF_8);
            }
            
            @Override
            public boolean isPresent() {
                return bytes.length > 0;
            }

            @Override
            public byte[] bytes() {
                return bytes;
            }

            @Override
            public long size() {
                return bytes.length;
            }

            @Override
            public boolean inMemory() {
                return true;
            }

            @Override
            public InputStream stream() {
                return new ByteArrayInputStream(bytes);
            }  
        };
    }

}
