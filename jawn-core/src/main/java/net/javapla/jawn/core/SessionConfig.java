package net.javapla.jawn.core;

import java.time.Duration;

public interface SessionConfig {
    SessionConfig memory();
    SessionConfig memory(Duration timeout);
    
    SessionConfig signed(String secret);
    
    SessionConfig store(SessionStore store);
}