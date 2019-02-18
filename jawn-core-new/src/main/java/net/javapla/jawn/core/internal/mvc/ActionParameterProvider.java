package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.javapla.jawn.core.internal.reflection.ClassMeta;

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
        String name = ActionParameterName.nameFor(parameter);
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
}
