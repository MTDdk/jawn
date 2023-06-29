package net.javapla.jawn.core.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import jakarta.inject.Provider;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Registry.Key;
import net.javapla.jawn.core.Registry.RegistryKey;
import net.javapla.jawn.core.Registry.ServiceRegistry;
import net.javapla.jawn.core.Up.RegistryException;
import net.javapla.jawn.core.internal.reflection.injection.ConstructorProxyFactory;
import net.javapla.jawn.core.internal.reflection.injection.ConstructorProxyFactory.ConstructorProxy;
import net.javapla.jawn.core.internal.reflection.injection.InjectionPoint;

/**
 * Simple dependency injection implementation
 */
class InjectionRegistry implements Registry.ServiceRegistry {
    
    private final Map<RegistryKey<?>, Supplier<?>> bindings = new ConcurrentHashMap<>();
    private final Map<Key<?>, Provider<?>> b = new ConcurrentHashMap<>();
    

    /*@Override
    public <T> T require(RegistryKey<T> key) throws RegistryException {
        Supplier<?> provider = bindings.get(key);
        System.out.println(bindings);
        System.out.println(key);
        System.out.println(key.name);
        System.out.println(key.hashCode());
        System.out.println(provider);
        
        if (provider != null) {
            @SuppressWarnings("unchecked") // we only put in BindingImpls that match their key types 
            T t = (T) provider.get();
            return t;
        }
        
        // just-in-time binding
        return justInTimeBinding(key);
    }*/
    
    
    public <T> T require(Key<T> key) throws RegistryException {
        Provider<?> provider = b.get(key);
        
        if (provider != null) {
            @SuppressWarnings("unchecked") // we only put in BindingImpls that match their key types
            T object = (T) provider.get();
            return object;
        }
        
        // just-in-time binding
        return justInTimeBinding(RegistryKey.of(key.type));
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
        System.out.println(".............................................");
        System.out.println(key);
        System.out.println(key.name);
        System.out.println(key.hashCode());
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
        b.put(key, service);
        return this;
    }

    @Override
    public <T> ServiceRegistry register(Key<T> key, T service) {
        return register(key, new Provider<>() {
            @Override
            public T get() {
                return service;
            }
        });
    }
}
