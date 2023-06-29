package net.javapla.jawn.core.internal.injection;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.RegistryException;

/**
 * Simple dependency injection implementation
 */
public class Injector implements Registry {
    
    private final Map<Key<?>, Provider<?>> bindings = new ConcurrentHashMap<>();
    
    public Injector() {
        // reference yourself
        bindings.put(Key.of(Injector.class), singleton(this));
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
        bindings.put(key, singleton(instance));
        return this;
    }

    @Override
    public <T> Injector register(Key<T> key, Provider<T> provider) {
        bindings.put(key, provider);
        return this;
    }

    private static <T> Provider<T> singleton(T instance) {
        return () -> instance;
    }
    
    <T> Provider<T> provider(Key<T> key) {
        InjectionPoint injectionPoint = InjectionPoint.forConstructorOf(key.type);
        ConstructorProxy<T> proxy = ConstructorProxy.create(injectionPoint);
        
        return () -> {
            try {
                return proxy.newInstance(params(injectionPoint));
            } catch (InvocationTargetException e) {
                throw new Registry.ProvisionException(e);
            }
        };
    }
    
    private <T> Provider<T> getBinding(Key<T> key) {
        Provider<?> existing = bindings.get(key);
        
        if (existing != null) {
            @SuppressWarnings("unchecked") // we only put in bindings that match their key types
            Provider<T> provider = (Provider<T>) existing;
            return provider;
        }
        
        // nothing already exists
        
        
        
        //justInTimeBinding(key);
        Provider<T> provider = provider(key);
        
        if (key.type.isAnnotationPresent(Singleton.class)) {
            // instantiate and save as singleton
            provider = singleton(provider.get());
        }
        
        register(key, provider);
        
        return provider;
    }
    
    /*private <T> void justInTimeBinding(Key<T> key) {
        InjectionPoint constructor = InjectionPoint.forConstructorOf(key.type);
        //parameterProviders(constructor);
        
        
    }*/
    
    final Object[] emptyParams = new Object[0];
    private Object[] params(InjectionPoint point) {
        if (point.dependencies.isEmpty()) return emptyParams;
        
        Object[] params = new Object[point.dependencies.size()];
        
        int index = 0;
        for (var dependency : point.dependencies) {
            params[index++] = require(dependency.key);
        }
        
        return params;
    }
    
}
