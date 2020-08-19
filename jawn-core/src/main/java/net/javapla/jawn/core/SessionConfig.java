package net.javapla.jawn.core;

import java.time.Duration;

public interface SessionConfig {
    SessionConfig memory();
    SessionConfig memory(Duration timeout);
    
    SessionConfig signed(String secret);
    
    SessionConfig store(SessionStore store);
    
    final class Impl implements SessionConfig {
        
        SessionStore sessionStore = SessionStore.memory();
        
        @Override
        public SessionConfig memory() {
            sessionStore = SessionStore.memory();
            return this;
        }
        
        @Override
        public SessionConfig memory(Duration timeout) {
            sessionStore = SessionStore.memory(timeout);
            return this;
        }
        
        @Override
        public SessionConfig signed(String secret) {
            sessionStore = SessionStore.signed(secret);
            return this;
        }
        
        @Override
        public SessionConfig store(SessionStore store) {
            sessionStore = store;
            return this;
        }
    }
}