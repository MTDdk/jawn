package net.javapla.jawn.core.internal.reflection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ReflectionMetadata {
    private ReflectionMetadata() {}
    
    /**
     * Minimised version of {@link #callingClass(Class)}
     * 
     * @return Just returns the direct caller without any evaluation to which class it might be
     */
    public static final String callingClassName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        return stack[2].getClassName();
    }
    
    @SuppressWarnings("unchecked")
    public static final <T> Class<T> callingClass(Class<T> assignableFrom) {
        /* 
         * https://stackoverflow.com/a/34948763
         *  - index 0 = Thread
         *  - index 1 = this
         *  - index 2 = direct caller, can be self.
         *  - index 3 ... n = classes and methods that called each other to get to the index 2 and below.
         */
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stack.length; i++) {
            
            Class<?> compiledClass = ClassFactory.getCompiledClass(stack[i].getClassName(), false);
            if (assignableFrom.isAssignableFrom(compiledClass)) {
                return (Class<T>) compiledClass;
            }
        }
        
        return null;
    }
    
    public static final Optional<Class<?>> getSuperclass(Class<?> cls) {
        return Optional.ofNullable(cls.getSuperclass());
    }

    /*public static final String getClassName(Class<?> cls) {
        return cls.getName();
    }

    /*public static final String getSuperclassName(Class<?> cls) {
        return getSuperclass(cls).map(c -> c.getName()).orElse("");
    }*/
    
    public static final List<String> getInterfacesNames(Class<?> cls) {
        Class<?>[] classes = cls.getInterfaces();
        return Arrays.asList(classes).stream().map(Class::getName).collect(Collectors.toList());
    }
    
    public static final boolean isAssignableFrom(Class<?> cls, Class<?> superCls) {
        String superClsName = superCls.getName();
        
        // are the two equal?
        if (cls.getName().equals(superClsName)) return true;
        
        // does it have the superclass in its hierarchy?
        Optional<Class<?>> superclass = getSuperclass(cls);
        while (superclass.isPresent()) {
            if (superClsName.equals(superclass.get().getName())) return true;
            superclass = ReflectionMetadata.getSuperclass(superclass.get());
        }
        
        // does it have it as an interface?
        for (String interfaceName : getInterfacesNames(cls)) {
            if (interfaceName.equals(superClsName)) {
                return true;
            }
        }
        
        return superCls.isAssignableFrom(cls);
    }
}
