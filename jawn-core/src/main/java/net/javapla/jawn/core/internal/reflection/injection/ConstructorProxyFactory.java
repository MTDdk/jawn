package net.javapla.jawn.core.internal.reflection.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConstructorProxyFactory {

    public static <T> ConstructorProxy<T> create(InjectionPoint injectionPoint) {
        @SuppressWarnings("unchecked")
        final Constructor<T> constructor = (Constructor<T>) injectionPoint.getMember();
        
        return new ReflectionProxy<T>(constructor);
    }
    
    private static final class ReflectionProxy<T> implements ConstructorProxy<T> {
        final Constructor<T> constructor;
        
        public ReflectionProxy(Constructor<T> constructor) {
            this.constructor = constructor;
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
    
    public interface ConstructorProxy<T> {
        /** Constructs an instance of {@code T} for the given arguments. */
        T newInstance(Object... arguments) throws InvocationTargetException;
        
        /**
         * Returns the injected constructor. If the injected constructor is synthetic (such as generated
         * code for method interception), the natural constructor is returned.
         */
        Constructor<T> getConstructor();
    }
}
