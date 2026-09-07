package net.javapla.jawn.core.internal.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public interface ConstructorProxy<T> {
    
    /** Constructs an instance of {@code T} for the given arguments. */
    T newInstance(Object... arguments) throws InvocationTargetException;
    
    /**
     * Returns the injected constructor. If the injected constructor is synthetic (such as generated
     * code for method interception), the natural constructor is returned.
     */
    Constructor<T> getConstructor();
    
    static <T> ConstructorProxy<T> create(InjectionPoint injectionPoint) {
        @SuppressWarnings("unchecked")
        final Constructor<T> constructor = (Constructor<T>) injectionPoint.member;
        
        return new ReflectionProxy<T>(constructor);
    }
    
    static final class ReflectionProxy<T> implements ConstructorProxy<T> {
        final Constructor<T> constructor;
        
        public ReflectionProxy(Constructor<T> constructor) {
            this.constructor = constructor;
            
            if (!Modifier.isPublic(constructor.getDeclaringClass().getModifiers()) 
                || !Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }
        }
        
        public T newInstance(Object... arguments) throws InvocationTargetException {
            try {
                return constructor.newInstance(arguments);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        
        @Override
        public Constructor<T> getConstructor() {
            return constructor;
        }
    }

}
