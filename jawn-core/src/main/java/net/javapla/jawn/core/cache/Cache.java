package net.javapla.jawn.core.cache;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface for all cache implementations.
 * 
 * Inject this interface where you want to use it and you are
 * ready to go.
 * 
 * Expiration is specified in seconds.
 */
public interface Cache {
    
    /**
     * Only adds the value, if the key is not already a part of the cache
     * @param key
     * @param value
     */
    <T> void add(String key, T value);
    /**
     * @see #add(String, Object)
     * @param key
     * @param value
     * @param seconds
     */
    <T> void add(String key, T value, int seconds);
    /**
     * Gets the value for the given key.
     * @param key
     * @return
     */
    <T> T get(String key);
    /**
     * Sets the given value to the key, regardless if the key already exists in the cache,
     * and thusly overrides any previous value for the key.
     * @param key
     * @param value
     */
    <T> void set(String key, T value);
    /**
     * @see #set(String, Object)
     * @param key
     * @param value
     * @param seconds
     */
    <T> void set(String key, T value, int seconds);
    
    
    
    <T> T computeIfAbsent(String key, Function<String, T> mappingFunction);
    default <T>  T computeIfAbsent(String key, int seconds, Function<String, T> mappingFunction) {
        T v, newValue;
        
        if ((v = get(key)) == null && (newValue = mappingFunction.apply(key)) != null) {
            set(key, newValue, seconds);
            return newValue;
        }
        
        return v;
    }
    
    <T> T computeIfAbsent(String key, Supplier<T> supplier);
    default <T> T computeIfAbsent(String key, int seconds, Supplier<T> supplier) {
        T v, newValue;
        
        if ((v = get(key)) == null && (newValue = supplier.get()) != null) {
            set(key, newValue, seconds);
            return newValue;
        }
        
        return v;
    }

    /**
     * 
     * @param key
     * @return in seconds
     */
    int getExpiration(String key);
    void setExpiration(String key, int seconds);
    
    /**
     * 
     * @return in seconds
     */
    int getDefaultCacheExpiration();
    void setDefaultCacheExpiration(int seconds);
    
    boolean isSet(String key);
    void delete(String key);
    void clear();
}