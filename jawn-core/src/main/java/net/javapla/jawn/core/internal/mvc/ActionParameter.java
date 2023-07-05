package net.javapla.jawn.core.internal.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Optional;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.annotation.HeaderParam;
import net.javapla.jawn.core.annotation.PathParam;
import net.javapla.jawn.core.annotation.QueryParam;

public class ActionParameter {
    
    @FunctionalInterface
    private interface Strategy {
        Object apply(Context ctx, ActionParameter param) throws Exception;
    }
    
    static final HashMap<Type, Strategy> strategies = new HashMap<>(8);
    static {
        strategies.put(PathParam.class, (c, p) -> c.req().pathParam(p.name).value());
        strategies.put(QueryParam.class, (c, p) -> c.req().queryParam(p.name));
        strategies.put(HeaderParam.class, (c, p) -> c.req().header(p.name));
        //strategies.put(SessionParam.class, (c, p) -> c.session);
        
        strategies.put(Context.class, (c, p) -> c);
        strategies.put(Context.Request.class, (c, p) -> c.req());
        strategies.put(Context.Response.class, (c, p) -> c.resp());
        
        //strategies.put(Cookie.class, (c, p) -> c.);
    }
    
    
    final Parameter parameter;
    final String name;
    final Type type;
    final Type annotation;
    final boolean optional;
    final Strategy strategy;

    public ActionParameter(Parameter parameter, String name) {
        this.parameter = parameter;
        this.name = name;
        this.type = parameter.getParameterizedType();
        this.annotation = annotation();
        this.optional = parameter.getType() == Optional.class;
        
        Type strategyType = annotation == null ? type : annotation;
        
        this.strategy = strategies.getOrDefault(strategyType, (c, p) -> null);
    }
    
    // In case a parameter could be a constant, but if it could, the constant should be a field in the controller instead.
    // That seems to be a more reasonable pattern.
    /*public ActionParameter(Parameter parameter, String name, Object constant) {
        this(parameter, name);
        this.strategy = (c,p) -> constant;
    }*/
    
    private Type annotation() {
        Annotation[] annotations = parameter.getAnnotations();
        if (annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (strategies.containsKey(annotation.annotationType())) return annotation.annotationType();
            }
        }
        return null;
    }
    
    public Object apply(Context ctx) throws Exception {
        return this.strategy.apply(ctx, this);
    }

    @Override
    public String toString() {
        return MessageFormat.format("param[{0}] name[{1}] type[{2}] anno[{3}] opt[{4}]", parameter, name, type, annotation, optional);
    }
}
