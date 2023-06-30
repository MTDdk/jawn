package net.javapla.jawn.core;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import net.javapla.jawn.core.annotation.Named;
import net.javapla.jawn.core.internal.injection.Provider;
import net.javapla.jawn.core.internal.reflection.Materialise;


/**
 * Dependency Injection pattern which may be provided by a dependency injection framework
 *
 */
public interface Registry {
// formerly known as Injection
    
    // TODO come back to all the throwables
    <T> T require(Class<T> type) throws Up.RegistryException;
    
    <T> T require(Class<T> type, String name) throws Up.RegistryException;
    
    <T> T require(Key<T> key) throws Up.RegistryException;
    
    <T> Provider<T> provider(Key<T> key) throws Up.RegistryException;
    
    <T> Registry register(Key<T> key, T instance);

    <T> Registry register(Key<T> key, Provider<T> provider);
    
    default <T> Registry register(Class<T> clazz, T service) {
        return register(Key.of(clazz), service);
    }
    
    
    /**
     * Registry for storing services in a simple key/value mechanism.
     */
    public static interface ServiceRegistry {
        
        /**
         * Register a service. 
         * Overrides any previous registered service
         * 
         * @param <T> Service type
         * @param key
         * @param service
         * @return this 
         * //Any previously registered service or <code>null</code>
         */
        <T> ServiceRegistry register(RegistryKey<T> key, T service);
        
        default <T> ServiceRegistry register(Class<T> clazz, T service) {
            //return register(RegistryKey.of(clazz), service);
            return register(Key.of(clazz), service);
        }
        
        /**
         * Register a service. 
         * Overrides any previous registered service
         * 
         * @param <T> Service type
         * @param key
         * @param service
         * @return this 
         * //Any previously registered service or <code>null</code>
         */
        @Deprecated
        <T> ServiceRegistry register(RegistryKey<T> key, Supplier<T> service);
        
        @Deprecated
        default <T> ServiceRegistry register(Class<T> clazz, Supplier<T> service) {
            return register(RegistryKey.of(clazz), service);
        }
        
        <T> ServiceRegistry register(Key<T> key, Provider<T> service);
        default <T> ServiceRegistry register(Class<T> clazz, Provider<T> service) {
            return register(Key.of(clazz), service);
        }
        
        /*@Override
        default <T> T require(Class<T> type) throws RegistryException {
            return require(Key.of(type));//require(RegistryKey.of(type));
        }
        
        @Override
        default <T> T require(Class<T> type, String name) throws RegistryException {
            return require(type, name);//require(RegistryKey.of(type, name));
        }*/

        <T> ServiceRegistry register(Key<T> key, T service);
    }
    
    public static final class ProvisionException extends RuntimeException {
        public ProvisionException(Throwable cause) {
            super(cause);
        }
        
        public ProvisionException(String msg) {
            super(msg);
        }
        
        private static final long serialVersionUID = 1L;
    }
    
    public static final class Key<T> {
        public final Class<T> type;
        public final String name;
        public final int hashCode;
        public final Class<? extends Annotation> qualifier;
        
        private Key(Class<T> type, String name, Class<? extends Annotation> qualifier) {
            this.type = type;
            this.name = name;
            this.qualifier = qualifier;
            this.hashCode = Arrays.hashCode(new Object[]{type, name, qualifier}); // automatically checks for null
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            if (!(obj instanceof Key)) return false;
            
            Key<?> that = (Key<?>) obj;
            return this.type.equals(that.type) && Objects.equals(this.name, that.name) && Objects.equals(this.qualifier, that.qualifier);
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public String toString() {
            String s = type.getName();
            if (name != null) s += "(" + name + ")";
            if (qualifier != null) s += "@" + qualifier.getSimpleName();
            return s;
        }
        
        public static <T> Key<T> of(Class<T> type) {
            return new Key<>(type, null, null);
        }
        
        public static <T> Key<T> of(Class<T> type, String name) {
            return new Key<>(type, name, Named.class);
        }
        
        public static <T> Key<T> of(Class<T> type, Annotation annotation) {
            if (annotation == null) return Key.of(type);
            
            return annotation.annotationType().equals(Named.class) ?
                Key.of(type, ((Named)annotation).value()) :
                new Key<>(type, null, annotation.annotationType());
        }
    }
    
    @Deprecated
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
