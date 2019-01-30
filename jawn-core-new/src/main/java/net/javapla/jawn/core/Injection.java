package net.javapla.jawn.core;

import com.google.inject.Key;

public interface Injection {

    // registry
    <T> T require(Key<T> key);
    default <T> T require(final Class<T> type) {
        return require(Key.get(type));
    }
}
