package net.javapla.jawn.core.internal.reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReflectionMetadata {
    
    public static final Optional<Class<?>> getSuperclass(Class<?> cls) {
        return Optional.ofNullable(cls.getSuperclass());
    }

    public static final String getClassName(Class<?> cls) {
        return cls.getName();
    }

    public static final String getSuperclassName(Class<?> cls) {
        return getSuperclass(cls).map(c -> c.getName()).orElse("");
    }
    
    public static final List<String> getInterfacesNames(Class<?> cls) {
        Class<?>[] classes = cls.getInterfaces();
        List<String> names = new ArrayList<String>(classes != null ? classes.length : 0);
        if (classes != null) for (Class<?> cls1 : classes) names.add(cls1.getName());
        return names;
    }
    
    public static final boolean isAssignableFrom(Class<?> cls, Class<?> superCls) {
        String superClsName = superCls.getName();
        
        // are the two equal?
        if (cls.getName().equals(superClsName)) return true;
        
        // does it have the superclass in its hierarchy?
        Optional<Class<?>> superclass = getSuperclass(cls);
        while (superclass.isPresent()) {
            if (superClsName.equals(superclass.get().getName())) return true;
        }
        
        // does it have it as an interface?
        for (String interfaceName : getInterfacesNames(cls)) {
            if (interfaceName.equals(superClsName)) {
                return true;
            }
        }
        return false;
    }
}
