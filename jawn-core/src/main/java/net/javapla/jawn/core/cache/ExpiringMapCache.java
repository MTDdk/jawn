package net.javapla.jawn.core.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import net.javapla.jawn.core.util.StringUtil;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

/**
 * ExpiringMap {@link Cache} implementation.
 * 
 * &quot;A high performance, low-overhead, zero dependency, thread-safe {@link ConcurrentMap} implementation that expires entries.&quot;
 * 
 * @see https://github.com/jhalterman/expiringmap
 * 
 * @author MTD
 */
public class ExpiringMapCache implements Cache {
    
    private final ExpiringMap<String, Object> cache;
    
    //TODO probably needs to be provided, so it can be for specific purposes
    // the instagrammanager does not need to be slowed by the calls to video caches.
    public ExpiringMapCache() {
        this.cache = 
            ExpiringMap
                .builder()
                .expiration(10, TimeUnit.MINUTES)
                .expirationPolicy(ExpirationPolicy.CREATED) // time-to-live
                .variableExpiration() // allows the keys to have individual expirations
                .build();
        
    }
    
    @Override
    public int getDefaultCacheExpiration() {
        return (int) cache.getExpiration() / 1000;
    }
    
    @Override
    public void setDefaultCacheExpiration(int seconds) {
        cache.setExpiration(seconds, TimeUnit.SECONDS);
    }
    
    @Override
    public <T> void add(String key, T value) {
        if (isSet(key)) return;
        cache.put(key, value);
    }
    
    @Override
    public <T> void add(String key, T value, int seconds) {
        if (isSet(key)) return;
        cache.put(key, value, seconds, TimeUnit.SECONDS);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) cache.get(key);
    }
    
    @Override
    public <T> void set(String key, T value) {
        if (StringUtil.blank(key)) throw new IllegalArgumentException("Key must not be null or empty");
        cache.put(key, value);
    }
    
    @Override
    public <T> void set(String key, T value, int seconds) {
        set(key, value);
        setExpiration(key, seconds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T computeIfAbsent(String key, Function<String, T> mappingFunction) {
        if (StringUtil.blank(key)) throw new IllegalArgumentException("Key must not be null or empty");
        return (T) cache.computeIfAbsent( key, mappingFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        if (StringUtil.blank(key)) throw new IllegalArgumentException("Key must not be null or empty");
        return (T) cache.computeIfAbsent(key, (k) -> supplier.get());
    }

    @Override
    public int getExpiration(String key) {
        return (int) (cache.getExpiration(key) / 1000);
    }
    
    @Override
    public void setExpiration(String key, int seconds) {
        cache.setExpiration(key, seconds, TimeUnit.SECONDS);
    }
    
    @Override
    public long getExpectedExpiration(String key) {
        return cache.getExpectedExpiration(key);
    }
    
    @Override
    public boolean isSet(String key) {
        if (StringUtil.blank(key)) throw new IllegalArgumentException("Key must not be null or empty");
        return cache.containsKey(key);// && cache.getExpectedExpiration(key) > 0;
    }
    
    @Override
    public void delete(String key) {
        if (StringUtil.blank(key)) throw new IllegalArgumentException("Key must not be null or empty");
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
