package net.javapla.jawn.core.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Up.RegistryException;

/**
 * Simple dependency injection implementation
 */
class InjectionRegistry implements Registry.ServiceRegistry {
    
    private final Map<RegistryKey<?>, Supplier<?>> bindings = new ConcurrentHashMap<>();
    

    @Override
    @SuppressWarnings("unchecked") // we only put in BindingImpls that match their key types 
    public <T> T require(RegistryKey<T> key) throws RegistryException {
        Supplier<?> provider = bindings.get(key);
        
        if (provider == null) return null;
        
        return (T) provider.get();
    }

    @Override
    public <T> T register(RegistryKey<T> key, T service) {
        return register(key, provide(service));
    }

    @Override
    @SuppressWarnings("unchecked") // we only put in BindingImpls that match their key types
    public <T> T register(RegistryKey<T> key, Supplier<T> service) {
        return (T) bindings.put(key, service);
    }

    private static <T> Supplier<T> provide(T service) {
        return () -> service;
    }
}
