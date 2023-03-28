package net.javapla.jawn.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import net.javapla.jawn.core.Up.RegistryException;
import net.javapla.jawn.core.internal.reflection.Materialise;


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
        <T> Supplier<T> register(RegistryKey<T> key, T service);
        
        default <T> Supplier<T> register(Class<T> clazz, T service) {
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
        <T> Supplier<T> register(RegistryKey<T> key, Supplier<T> service);
        
        default <T> Supplier<T> register(Class<T> clazz, Supplier<T> service) {
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
    
    public static final class ProvisionException extends RuntimeException {
        public ProvisionException(Throwable cause) {
            super(cause);
        }
        
        private static final long serialVersionUID = 1L;
    }
    
    public static final class RegistryKey<T> {
        public final TypeLiteral<T> typeLiteral;
        
        public final int hash;
        
        public final String name;
        
        private RegistryKey(Class<T> type, String name) {
            this(TypeLiteral.get(type), name);
        }
        
        private RegistryKey(TypeLiteral<T> type, String name) {
            this.typeLiteral = Materialise.canonicalizeKey(type);
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
                return this.typeLiteral == that.typeLiteral && Objects.equals(this.name, that.name);
            }
            return false;
        }
        
        @Override
        public String toString() {
            if (name == null) return typeLiteral.type.getTypeName();
            return typeLiteral.type.getTypeName() + "(" + name + ")";
        }
        
        public static <T> RegistryKey<T> of(Class<T> type) {
            return new RegistryKey<>(type, null);
        }
        
        public static <T> RegistryKey<T> of(Class<T> type, String name) {
            return new RegistryKey<>(type, name);
        }
        
        public static <T> RegistryKey<T> of(TypeLiteral<T> type) {
            return new RegistryKey<>(type, null);
        }
    }
    
}
