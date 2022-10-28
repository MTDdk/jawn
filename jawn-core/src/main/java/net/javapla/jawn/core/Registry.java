package net.javapla.jawn.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import net.javapla.jawn.core.Up.RegistryException;


/**
 * Dependency Injection pattern which may be provided by a dependency injection framework
 *
 */
public interface Registry {
// formerly known as Injection
    
    
    <T> T require(Class<T> type) throws Up.RegistryException;
    
    <T> T require(Class<T> type, String name) throws Up.RegistryException;
    
    <T> T require(RegistryKey<T> key) throws Up.RegistryException;
    
    
    /**
     * Registry for storing services in a simple key/value mechanism.
     */
    public static interface ServiceRegistry extends Registry {
        
        /**
         * Register a service. 
         * Overrides any previous registered service
         * 
         * @param <T> Service type
         * @param key
         * @param service
         * @return Any previously registered service or <code>null</code>
         */
        <T> T register(RegistryKey<T> key, T service);
        
        default <T> T register(Class<T> clazz, T service) {
            return register(RegistryKey.of(clazz), service);
        }
        
        /**
         * Register a service. 
         * Overrides any previous registered service
         * 
         * @param <T> Service type
         * @param key
         * @param service
         * @return Any previously registered service or <code>null</code>
         */
        <T> T register(RegistryKey<T> key, Supplier<T> service);
        
        default <T> T register(Class<T> clazz, Supplier<T> service) {
            return register(RegistryKey.of(clazz), service);
        }
        
        @Override
        default <T> T require(Class<T> type) throws RegistryException {
            return require(RegistryKey.of(type));
        }
        
        @Override
        default <T> T require(Class<T> type, String name) throws RegistryException {
            return require(RegistryKey.of(type, name));
        }
    }
    
    public static final class RegistryKey<T> {
        public final Class<T> type; // TODO do we need Guice TypeLiteral equivalent?
        
        public final int hash;
        
        public final String name;
        
        private RegistryKey(Class<T> type, String name) {
            this.type = type;
            this.hash = Arrays.hashCode(new Object[]{type, name}); // automatically checks for null
            this.name = name;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RegistryKey) {
                RegistryKey<?> that = (RegistryKey<?>) obj;
                return this.type == that.type && Objects.equals(this.name, that.name);
            }
            return false;
        }
        
        @Override
        public String toString() {
            if (name == null) return type.getName();
            return type.getName() + "(" + name + ")";
        }
        
        public static <T> RegistryKey<T> of(Class<T> type) {
            return new RegistryKey<>(type, null);
        }
        
        public static <T> RegistryKey<T> of(Class<T> type, String name) {
            return new RegistryKey<>(type, name);
        }
    }
    
}
