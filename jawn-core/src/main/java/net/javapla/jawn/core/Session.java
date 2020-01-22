package net.javapla.jawn.core;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Session {

    String getId();
    
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
    
    // put(String, Number) + put(String, CharSequence)
    
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
    void invalidate();
    
    Instant creationTime();
    
    Instant lastAccessedTime();
    
    
    
    static Session create(Context context, String id) {
        return create(context, id, new ConcurrentHashMap<>()); 
    }
    
    static Session create(final Context context, final String id, final Map<String, String> attributes) {
        return new Session() {
            
            

            @Override
            public String getId() {
                return id;
            }

            @Override
            public Value get(String name) {
                return Value.of(attributes.get(name));
            }

            @Override
            public Session put(String name, String value) {
                return null;
            }

            @Override
            public Value remove(String name) {
                return null;
            }

            @Override
            public Map<String, String> data() {
                return attributes; //TODO read-only
            }

            @Override
            public Session clear() {
                attributes.clear();
                return this;
            }

            @Override
            public void invalidate() {
                //context.removeAttribute();
            }

            @Override
            public Instant creationTime() {
                return null;
            }

            @Override
            public Instant lastAccessedTime() {
                return null;
            }
            
        };
    }
}
