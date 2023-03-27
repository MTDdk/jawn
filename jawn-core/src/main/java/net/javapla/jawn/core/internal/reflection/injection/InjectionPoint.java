package net.javapla.jawn.core.internal.reflection.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jakarta.inject.Inject;
import net.javapla.jawn.core.TypeLiteral;
import net.javapla.jawn.core.internal.reflection.Materialise;

public final class InjectionPoint {
    
    private final Member member;
    final TypeLiteral<?> declaringType;
    
    InjectionPoint(TypeLiteral<?> declaringType, Constructor<?> constructor) {
        this.member = constructor;
        this.declaringType = declaringType;
    }
    
    InjectionPoint(TypeLiteral<?> declaringType, Method method) {
        this.member = method;
        this.declaringType = declaringType;
    }

    public Member getMember() {
        return member;
    }
    
    public TypeLiteral<?> getDeclaringType() {
        return declaringType;
    }
    
    public static <T> InjectionPoint forConstructor(Constructor<T> constructor) {
        return new InjectionPoint(TypeLiteral.get(constructor.getDeclaringClass()), constructor);
    }
    public static <T> InjectionPoint forConstructor(Constructor<T> constructor, TypeLiteral<? extends T> type) {
        return new InjectionPoint(type, constructor);
    }

    public static InjectionPoint forConstructorOf(Class<?> clzz) {
        return forConstructorOf(TypeLiteral.get(clzz));
    }
    
    public static InjectionPoint forConstructorOf(TypeLiteral<?> type) {
        Class<?> rawType = Materialise.getRawType(type.type);
        
        Constructor<?> injectableConstructor = null;
        for (Constructor<?> constructor : rawType.getDeclaredConstructors()) {
            
            Inject inject = constructor.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
            }
            
            injectableConstructor = constructor;
        }
        
        if (injectableConstructor != null) {
            return new InjectionPoint(type, injectableConstructor);
        }
        
        // no annotated constructor is found, so look for a no-arg constructor
        try {
            Constructor<?> noArgConstructor = rawType.getDeclaredConstructor();
            
            if ( Modifier.isPrivate(noArgConstructor.getModifiers()) &&
                !Modifier.isPrivate(rawType.getModifiers())) {
                // TODO log the missing constructor for 'type'
                throw new AssertionError("");
            }
            
            return new InjectionPoint(type, noArgConstructor);
        } catch (NoSuchMethodException e) {
            // TODO log the missing constructor for 'type'
            throw new AssertionError("", e);
        }
    }
    
    public static <T> InjectionPoint forMethod(Method method, TypeLiteral<T> type) {
        return new InjectionPoint(type, method);
    }
}
