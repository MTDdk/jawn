package net.javapla.jawn.core.internal.reflection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ReflectionMetadata {
    private ReflectionMetadata() {}
    
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
