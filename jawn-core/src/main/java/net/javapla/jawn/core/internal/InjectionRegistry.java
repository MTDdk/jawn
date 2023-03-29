package net.javapla.jawn.core.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import jakarta.inject.Provider;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Up.RegistryException;
import net.javapla.jawn.core.internal.reflection.injection.ConstructorProxyFactory;
import net.javapla.jawn.core.internal.reflection.injection.ConstructorProxyFactory.ConstructorProxy;
import net.javapla.jawn.core.internal.reflection.injection.InjectionPoint;

/**
 * Simple dependency injection implementation
 */
class InjectionRegistry implements Registry.ServiceRegistry {
    
    private final Map<RegistryKey<?>, Supplier<?>> bindings = new ConcurrentHashMap<>();
    

    @Override
    public <T> T require(RegistryKey<T> key) throws RegistryException {
        Supplier<?> provider = bindings.get(key);
        
        if (provider != null) {
            @SuppressWarnings("unchecked") // we only put in BindingImpls that match their key types 
            T t = (T) provider.get();
            return t;
        }
        
        // just-in-time binding
        return justInTimeBinding(key);
    }
    
    private <T> T justInTimeBinding(RegistryKey<T> key) {
        Supplier<T> binding = provide(key);
        register(key, binding);
        return binding.get();
    }
    
    @Override
    public <T> InjectionRegistry register(RegistryKey<T> key, T service) {
        return register(key, provide(service));
    }

    @Override
    public <T> InjectionRegistry register(RegistryKey<T> key, Supplier<T> service) {
        //@SuppressWarnings("unchecked") // we only put in BindingImpls that match their key types
        bindings.put(key, service);
        return this;
    }

    private static <T> Supplier<T> provide(T service) {
        return () -> service;
    }
    
    <T> Supplier<T> provide(Registry.RegistryKey<T> key) {
        ConstructorProxy<T> proxy = ConstructorProxyFactory.create(InjectionPoint.forConstructorOf(key.typeLiteral));
        
        return () -> {
            try {
                // TODO handle injectables
                return proxy.newInstance();
            } catch (InvocationTargetException e) {
                throw new Registry.ProvisionException(e);
            }
        };
    }

    @Override
    public <T> ServiceRegistry register(Key<T> key, Provider<T> service) {
        return null;
    }

    @Override
    public <T> ServiceRegistry register(Key<T> key, T service) {
        return null;
    }
}
