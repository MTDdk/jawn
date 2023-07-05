package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import net.javapla.jawn.core.annotation.HeaderParam;
import net.javapla.jawn.core.annotation.Named;
import net.javapla.jawn.core.annotation.PathParam;
import net.javapla.jawn.core.annotation.QueryParam;
import net.javapla.jawn.core.annotation.SessionParam;
import net.javapla.jawn.core.internal.reflection.ClassMeta;
import net.javapla.jawn.core.util.StringUtil;

public class ActionParameterExtractor {
    
    private final Map<Executable,String[]> parameterNames;

    public ActionParameterExtractor(ClassMeta meta, Class<?> controller) {
        parameterNames = meta.extractParameterNames(controller);
    }

    public ActionParameter[] parameters(Method method) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) return null;
        
        ActionParameter[] params = new ActionParameter[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            params[i] = new ActionParameter(parameters[i], name(parameters[i]));
        }
        
        return params;
    }
    
    private String name(Parameter parameter) {
        String name = annotatedName(parameter);
        
        if (name != null) return name;
        
        if (parameter.isNamePresent()) return parameter.getName();
        
        // Annotations and classfile did not help us at all.
        // Bring in the big guns:
        
        // ASM
        Executable exe = parameter.getDeclaringExecutable();
        int paramIndex = 0;
        for (Parameter p : exe.getParameters()) {
            if (p.equals(parameter)) break;
            paramIndex++;
        }
        String[] names = parameterNames.get(exe);
        return names[paramIndex];
    }
    
    private String annotatedName(AnnotatedElement elm) {
        PathParam p = elm.getAnnotation(PathParam.class);
        if (p != null) return StringUtil.stringOrNull(p.value());
        
        QueryParam q = elm.getAnnotation(QueryParam.class);
        if (q != null) return StringUtil.stringOrNull(q.value());
        
        HeaderParam h = elm.getAnnotation(HeaderParam.class);
        if (h != null) return StringUtil.stringOrNull(h.value());
        
        SessionParam s = elm.getAnnotation(SessionParam.class);
        if (s != null) return StringUtil.stringOrNull(s.value());
        
        Named n = elm.getAnnotation(Named.class);
        if (n != null) return StringUtil.stringOrNull(n.value());
        
        return null;
    }
    
}
