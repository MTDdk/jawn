package net.javapla.jawn.core;

import com.google.inject.Key;
import com.google.inject.name.Names;

public interface Injection {

    <T> T require(Key<T> key);
    
    default <T> T require(final Class<T> type) {
        return require(Key.get(type));
    }
    
    /**
     * Retrieve a named instance
     */
    default <T> T require(final Class<T> type, final String name) {
        return require(Key.get(type, Names.named(name)));
    }
}
