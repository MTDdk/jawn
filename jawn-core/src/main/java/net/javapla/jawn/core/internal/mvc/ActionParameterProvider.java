package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.javapla.jawn.core.internal.reflection.ClassMeta;
import net.javapla.jawn.core.mvc.Param;
import net.javapla.jawn.core.mvc.PathParam;
import net.javapla.jawn.core.mvc.QueryParam;
import net.javapla.jawn.core.util.StringUtil;

public class ActionParameterProvider {

    
    private ClassMeta classMeta;

    public ActionParameterProvider(final ClassMeta meta) {
        this.classMeta = meta;
    }
    
    public List<ActionParameter> parameters(final Method action) {
        // TODO this can probably get cached
        Parameter[] parameters = action.getParameters();
        if (parameters.length == 0) return Collections.emptyList();
        
        ArrayList<ActionParameter> list = new ArrayList<>();
        for (Parameter p : parameters) {
            list.add(new ActionParameter(p, name(p)));
        }
        
        list.trimToSize();
        return list;
    }
    
    public String name(final Parameter parameter) {
        String name = nameFor(parameter);
        if (name != null) {
            return name;
        }
        
        // we could not locate a name for the parameter by standard reflection
        
        // Using ASM
        Executable exe = parameter.getDeclaringExecutable();
        Parameter[] params = exe.getParameters();
        int index = 0; // TODO while?
        for (; index < params.length; index++) {
            if (params[index].equals(parameter)) break;
        }
        
        String[] names = classMeta.parameterNames(exe);
        return names[index];
    }
    
    public static String nameFor(final Parameter param) {
        String name = findAnnotatedName(param);
        return name == null ? (param.isNamePresent() ? param.getName() : null) : name;
    }

    private static String findAnnotatedName(final AnnotatedElement elem) {
        PathParam path = elem.getAnnotation(PathParam.class);
        if (path != null) return StringUtil.stringOrNull(path.value());
        
        QueryParam query = elem.getAnnotation(QueryParam.class);
        if (query != null) return StringUtil.stringOrNull(query.value());
        
        Param param = elem.getAnnotation(Param.class);
        if (param != null) return StringUtil.stringOrNull(param.value());
        
        
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
