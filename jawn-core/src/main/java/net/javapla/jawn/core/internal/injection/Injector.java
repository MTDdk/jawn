package net.javapla.jawn.core.internal.injection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import jakarta.inject.Provider;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.RegistryException;

public class Injector implements Registry.ServiceRegistry {
    
    private final Map<Key<?>, Provider<?>> bindings = new ConcurrentHashMap<>();
    
    public Injector() {
        // reference yourself
        bindings.put(Key.of(Injector.class), provider(this));
    }

    @Override
    public <T> T require(Class<T> type) throws Up.RegistryException {
        return require(Key.of(type));
    }

    @Override
    public <T> T require(Class<T> type, String name) throws Up.RegistryException {
        return require(Key.of(type, name));
    }

    @Override
    public <T> T require(Key<T> key) throws RegistryException {
        return getBinding(key).get();
    }
    
    @Override
    public <T> Injector register(Key<T> key, T instance) {
        bindings.put(key, provider(instance));
        return this;
    }

    @Override
    public <T> Injector register(Key<T> key, Provider<T> provider) {
        bindings.put(key, provider);
        return this;
    }

    private static <T> Provider<T> provider(T instance) {
        return () -> instance;
    }
    
    private <T> Provider<T> getBinding(Key<T> key) {
        @SuppressWarnings("unchecked")
        Provider<T> existing = (Provider<T>) bindings.get(key);
        if (existing != null) return existing;
        // nothing already exists
        
        InjectionPoint constructor = InjectionPoint.forConstructorOf(key.type);
        //parameterProviders(constructor);
        
            
        return null;
    }
    
    private static void parameterProviders(InjectionPoint point) {
        System.out.println(point);
        
        System.out.println(point.dependencies);
    }
    
    

    
    @Override
    public <T> T require(RegistryKey<T> key) throws Up.RegistryException {
        throw new UnsupportedOperationException();
    }
    @Override
    public <T> ServiceRegistry register(RegistryKey<T> key, Supplier<T> service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> ServiceRegistry register(RegistryKey<T> key, T service) {
        throw new UnsupportedOperationException();
    }

}
