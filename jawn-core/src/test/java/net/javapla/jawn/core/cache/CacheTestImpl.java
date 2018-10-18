package net.javapla.jawn.core.cache;

import java.util.function.Function;
import java.util.function.Supplier;

public class CacheTestImpl implements Cache {

    @Override
    public <T> void add(String key, T value) {}

    @Override
    public <T> void add(String key, T value, int seconds) {}

    @Override
    public <T> T get(String key) {
        return null;
    }

    @Override
    public <T> void set(String key, T value) {}

    @Override
    public <T> void set(String key, T value, int seconds) {}

    @Override
    public <T> T computeIfAbsent(String key, Function<String, T> mappingFunction) {
        return null;
    }

    @Override
    public <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        return null;
    }

    @Override
    public void setExpiration(String key, int seconds) {}

    @Override
    public void setDefaultCacheExpiration(int seconds) {}

    @Override
    public boolean isSet(String key) {
        return false;
    }

    @Override
    public void delete(String key) {}

    @Override
    public void clear() {}

    @Override
    public int getDefaultCacheExpiration() {
        return 0;
    }

    @Override
    public int getExpiration(String key) {
        return 0;
    }
    
    @Override
    public long getExpectedExpiration(String key) {
        return 0;
    }

}
