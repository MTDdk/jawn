package net.javapla.jawn.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public interface Session {
    
    /** Session attribute name */
    String NAME = "jawnsession";
    
    
    String id();
    
    Value get(String name);
    
    Session put(String name, String value);
    
    default Session put(String name, int value) {
        return put(name, Integer.toString(value));
    }
    
    default Session put(String name, long value) {
        return put(name, Long.toString(value));
    }
    
    default Session put(String name, float value) {
        return put(name, Float.toString(value));
    }
    
    default Session put(String name, double value) {
        return put(name, Double.toString(value));
    }
    
    default Session put(String name, boolean value) {
        return put(name, Boolean.toString(value));
    }
    
    boolean has(String name);
    
    /**
     * Remove a session attribute
     * 
     * @param name Name of the attribute
     * @return The value of the removed attribute
     */
    Value remove(String name);
    
    /**
     * Read-only copy of session attributes
     * 
     * @return Read-only attributes 
     */
    Map<String, String> data();
    
    /**
     * Clears attributes
     * 
     * @return this
     */
    Session clear();
    
    /**
     * Destroy / invalidate entire session
     */
    void invalidate(); // destroy
    
    Instant created();
    
    Instant lastAccessed();
    
    void updateAccess();
    
    default boolean isExpired(Duration timeout) {
        Duration timeElapsed = Duration.between(lastAccessed(), Instant.now());
        return timeElapsed.compareTo(timeout) > 0;
    }

    
    class SessionImpl implements Session {
        
        private Instant lastAccessed = Instant.now();
        
        private final SessionStore store;
        private final Context context;
        private final String id;
        private final Instant created;
        private final Map<String, String> attributes;
        
        private SessionImpl(final SessionStore sessionStore, final Context context, final String id, final Instant createdTime, final Map<String, String> attributes) {
            this.store = sessionStore;
            this.context = context;
            this.id = id;
            this.created = createdTime;
            this.attributes = attributes;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Value get(String name) {
            return Value.of(attributes.get(name));
        }

        @Override
        public Session put(String name, String value) {
            attributes.put(name, value);
            updateState();
            return this;
        }
        
        @Override
        public boolean has(String key) {
            return attributes.containsKey(key);
        }

        @Override
        public Value remove(String name) {
            String attr = attributes.remove(name);
            updateState();
            return Value.of(attr);
        }

        @Override
        public Map<String, String> data() {
            return attributes; //TODO read-only
        }

        @Override
        public Session clear() {
            attributes.clear();
            updateState();
            return this;
        }
        
        @Override
        public void invalidate() { // destroy
            context.removeAttribute(NAME);
            attributes.clear();
            store.deleteSession(context, this);
        }

        @Override
        public Instant created() {
            return created;
        }

        @Override
        public Instant lastAccessed() {
            return lastAccessed;
        }
        
        @Override
        public void updateAccess() {
            lastAccessed = Instant.now();
        }
        
        private void updateState() {
            updateAccess();
            store.touchSession(context, this);
        }
    }
    
    static interface SessionConfig {
        
    }
}
