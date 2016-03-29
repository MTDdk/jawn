package net.javapla.jawn.core.reflection;

import java.util.ArrayList;
import java.util.List;

public class ReflectionMetadata {

    public static final String getClassName(Class<?> cls) {
        return cls.getName();
    }

    public static final String getSuperclassName(Class<?> cls) {
        Class<?> superclass = cls.getSuperclass();
        return superclass != null ? superclass.getName() : "";
    }
    
    public static final List<String> getInterfacesNames(Class<?> cls) {
        Class<?>[] classes = cls.getInterfaces();
        List<String> names = new ArrayList<String>(classes != null ? classes.length : 0);
        if (classes != null) for (Class<?> cls1 : classes) names.add(cls1.getName());
        return names;
    }
    
    public static final boolean isAssignableFrom(Class<?> cls, Class<?> superCls) {
        String superClsName = superCls.getName();
        if (cls.getName().equals(superClsName)) return true;
        if (superClsName.equals(getSuperclassName(cls))) return true;
        
        for (String interfaceName : getInterfacesNames(cls)) {
            if (interfaceName.equals(superClsName)) {
                return true;
            }
        }
        return false;
    }
}
