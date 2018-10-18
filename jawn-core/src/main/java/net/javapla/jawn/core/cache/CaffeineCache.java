package net.javapla.jawn.core.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.benmanes.caffeine.cache.Caffeine;

public class CaffeineCache implements Cache {
    
    private final com.github.benmanes.caffeine.cache.Cache<String, Object> cache;
    
    public CaffeineCache() {
        this.cache = 
            Caffeine
                .newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build()
                ;
    }

    @Override
    public int getDefaultCacheExpiration() {
        return cache.policy().expireAfterWrite().flatMap(ex -> Optional.of(ex.getExpiresAfter(TimeUnit.SECONDS))).orElse(10 * 60l).intValue();
    }

    @Override
    public void setDefaultCacheExpiration(int seconds) {
        cache.policy().expireAfterWrite().ifPresent(expiration -> expiration.setExpiresAfter(seconds, TimeUnit.SECONDS));
    }

    @Override
    public <T> void add(String key, T value) {
    }

    @Override
    public <T> void add(String key, T value, int seconds) {
    }

    @Override
    public <T> T get(String key) {
        return null;
    }

    @Override
    public <T> void set(String key, T value) {
    }

    @Override
    public <T> void set(String key, T value, int seconds) {
    }

    @Override
    public <T> T computeIfAbsent(String key, Function<String, T> mappingFunction) {
        return null;
    }

    @Override
    public <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        return null;
    }

    @Override
    public int getExpiration(String key) {
        return 0;
    }

    @Override
    public void setExpiration(String key, int seconds) {
    }
    
    @Override
    public long getExpectedExpiration(String key) {
        return 0;
    }
    
    @Override
    public boolean isSet(String key) {
        return false;
    }

    @Override
    public void delete(String key) {
    }

    @Override
    public void clear() {
    }

}
