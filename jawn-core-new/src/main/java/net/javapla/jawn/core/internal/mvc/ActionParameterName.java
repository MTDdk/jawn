package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

import net.javapla.jawn.core.util.StringUtil;


public class ActionParameterName {

    // most of this should probably be put into reflection and/or ReflectionMetadata
    
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
}
