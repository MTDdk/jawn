package net.javapla.jawn.core.internal.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Closeables;

import net.javapla.jawn.core.internal.reflection.ClassFactory;
import net.javapla.jawn.core.util.StringUtil;


public class ActionParameterName {

    public static String name(final Parameter parameter) {
        String name = nameFor(parameter);
        if (name != null) {
            return name;
        }
        
        // we could not locate a name for the parameter by standard reflection
        
        Executable exe = parameter.getDeclaringExecutable();
        Parameter[] params = exe.getParameters();
        int index = 0; // TODO while?
        for (; index < params.length; index++) {
            if (params[index].equals(parameter)) break;
        }
        System.out.println(index);
        System.out.println(Arrays.toString(params));
        System.out.println(params[index]);
        
        return null;
    }
    
    public static String nameFor(final Parameter param) {
        String name = findAnnotatedName(param);
        return name == null ? (param.isNamePresent() ? param.getName() : null) : name;
    }

    private static String findAnnotatedName(final AnnotatedElement elem) {
        javax.inject.Named named = elem.getAnnotation(javax.inject.Named.class);
        if (named == null) {
            com.google.inject.name.Named gnamed = elem.getAnnotation(com.google.inject.name.Named.class);
            if (gnamed == null) {
//                Header header = elem.getAnnotation(Header.class);
//                if (header == null) {
                    return null;
//                }
//                return StringUtil.stringOrNull(header.value());
            }
            return gnamed.value();
        }
        return StringUtil.stringOrNull(named.value());
    }
    
    private static Map<String, Object> extractMetadata(final Class<?> owner) {
        InputStream stream = null;
        try {
            Map<String, Object> md = new HashMap<>();
            stream = owner.getResource(classfile(owner)).openStream();
            
            
            //new ClassReader(stream).accept(visitor(md), 0);
            return md;
        } catch (Exception ex) {
            // won't happen, but...
            throw new IllegalStateException("Can't read class: " + owner.getName(), ex);
        } finally {
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String classfile(final Class<?> owner) {
        StringBuilder sb = new StringBuilder();
        Class<?> dc = owner.getDeclaringClass();
        while (dc != null) {
            sb.insert(0, dc.getSimpleName()).append("$");
            dc = dc.getDeclaringClass();
        }
        sb.append(owner.getSimpleName());
        sb.append(".class");
        return sb.toString();
    }
}
