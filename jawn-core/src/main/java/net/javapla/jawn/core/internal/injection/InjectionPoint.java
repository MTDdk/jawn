package net.javapla.jawn.core.internal.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jakarta.inject.Inject;
import net.javapla.jawn.core.Registry.Key;

public final class InjectionPoint {
    
    private final Member member;
    final Class<?> declaringType;
    final List<Dependency<?>> dependencies;
    
    InjectionPoint(Class<?> declaringType, Constructor<?> constructor) {
        this.member = constructor;
        this.declaringType = declaringType;
        this.dependencies = forMember(constructor, declaringType, constructor.getParameterAnnotations());
    }
    
    InjectionPoint(Class<?> declaringType, Method method) {
        this.member = method;
        this.declaringType = declaringType;
        this.dependencies = forMember(method, declaringType, method.getParameterAnnotations());
    }
    
    private List<Dependency<?>> forMember(Member member, Class<?> type, Annotation[][] parameterAnnotations) {
        LinkedList<Dependency<?>> dependencies = new LinkedList<>();
        int index = 0;
        
        List<Class<?>> parameterTypes = Types.getParameterTypes(type, member);
        for (Class<?> parameterType : parameterTypes) {
            Annotation[] annotations = parameterAnnotations[index];
            Annotation qualifier = qualifier(annotations);
            Key<?> key = qualifier != null ? Key.of(parameterType, qualifier) : Key.of(type);
            dependencies.add(newDependency(key, Nullability.allowsNull(annotations), index));
            index++;
        }
        
        return Collections.unmodifiableList(dependencies);
    }
    
    private static Annotation qualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(jakarta.inject.Qualifier.class)) {
                return annotation;
            }
        }
        return null;
    }
    
    // This metohd is necessary to create a Dependency<T> with proper generic type information
    private <T> Dependency<T> newDependency(Key<T> key, boolean allowsNull, int index) {
        return new Dependency<T>(this, key, allowsNull, index);
    }
    
    public Member getMember() {
        return member;
    }
    
    public Class<?> getDeclaringType() {
        return declaringType;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + member + ")";
    }
    
    public static <T> InjectionPoint forConstructor(Constructor<T> constructor) {
        return new InjectionPoint(constructor.getDeclaringClass(), constructor);
    }
    public static <T> InjectionPoint forConstructor(Constructor<T> constructor, Class<? extends T> type) {
        return new InjectionPoint(type, constructor);
    }

    /*public static InjectionPoint forConstructorOf(Class<?> clzz) {
        return forConstructorOf(TypeLiteral.get(clzz));
    }*/
    
    public static InjectionPoint forConstructorOf(Class<?> rawType) {
        //Class<?> rawType = Materialise.getRawType(type.type);
        
        Constructor<?> injectableConstructor = null;
        for (Constructor<?> constructor : rawType.getDeclaredConstructors()) {
            
            Inject inject = constructor.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
                // TODO throw exception instead when multiple @Inject are found?
            }
            
            injectableConstructor = constructor;
        }
        
        if (injectableConstructor != null) {
            return new InjectionPoint(rawType, injectableConstructor);
        }
        
        // no annotated constructor is found, so look for a no-arg constructor
        try {
            Constructor<?> noArgConstructor = rawType.getDeclaredConstructor();
            
            if ( Modifier.isPrivate(noArgConstructor.getModifiers()) &&
                !Modifier.isPrivate(rawType.getModifiers())) {
                // TODO log the missing constructor for 'type'
                throw new AssertionError("");
            }
            
            return new InjectionPoint(rawType, noArgConstructor);
        } catch (NoSuchMethodException e) {
            // TODO log the missing constructor for 'type'
            throw new AssertionError("", e);
        }
    }
    
    public static <T> InjectionPoint forMethod(Method method, Class<T> type) {
        return new InjectionPoint(type, method);
    }
}
