package net.javapla.jawn.core.internal.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import net.javapla.jawn.core.Body;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.annotation.HeaderParam;
import net.javapla.jawn.core.annotation.PathParam;
import net.javapla.jawn.core.annotation.QueryParam;
import net.javapla.jawn.core.internal.ValueParser;

public class ActionParameter {
    
    @FunctionalInterface
    private interface Strategy {
        Object apply(Context ctx, ActionParameter param) throws Exception;
        
        default Strategy optional() {
            return (c, p) -> Optional.ofNullable(apply(c, p));
        }
    }
    
    @FunctionalInterface
    private interface ValuedStrategy extends Strategy {
        Value apply(Context ctx, ActionParameter param) throws Exception;
    }
    
    static final HashMap<Type, ValuedStrategy> valued_strategies = new HashMap<>(8);
    static final HashMap<Type, Strategy> strategies = new HashMap<>(8);
    static {
        valued_strategies.put(PathParam.class, (c, p) -> c.req().pathParam(p.name));
        valued_strategies.put(QueryParam.class, (c, p) -> c.req().queryParam(p.name));
        valued_strategies.put(HeaderParam.class, (c, p) -> c.req().header(p.name));
        //strategies.put(SessionParam.class, (c, p) -> c.session);
        
        strategies.put(Context.class, (c, p) -> c);
        strategies.put(Context.Request.class, (c, p) -> c.req());
        strategies.put(Context.Response.class, (c, p) -> c.resp());
        strategies.put(Body.class, (c, p) -> c.req().body());
        
        //strategies.put(Cookie.class, (c, p) -> c.);
    }
    
    
    final Parameter parameter;
    final String name;
    final Type type;
    final Type annotation;
    final Strategy strategy;

    public ActionParameter(Parameter parameter, String name) {
        this.parameter = parameter;
        this.name = name;
        this.type = parameter.getParameterizedType();
        this.annotation = annotation();
        
        Type strategyType = annotation == null ? type : annotation;
        
        this.strategy = handleConversion( 
                            strategies.getOrDefault(strategyType, 
                                valued_strategies.get(strategyType)));
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
                if (valued_strategies.containsKey(annotation.annotationType())) return annotation.annotationType();
                if (strategies.containsKey(annotation.annotationType())) return annotation.annotationType();
            }
        }
        return null;
    }
    
    public Object apply(Context ctx) throws Exception {
        return this.strategy.apply(ctx, this);
    }
    
    Strategy handleConversion(Strategy strategy) {
        
        if (strategy == null) {
            // No default strategy found.
            // Return default values based on type
            if (type == int.class || type == long.class || type == double.class) {
                return (c, p) -> -1; // TODO should throw
            } else {
                return (c, p) -> null;
            }
        }
        
        if (strategy instanceof ValuedStrategy) {
            final ValuedStrategy s = (ValuedStrategy) strategy;
            final Function<Value, ?> converter = ValueParser.converter(type);
            strategy = (c, p) -> converter.apply( s.apply(c, p) );
        }
        
        // if the parameter is of type Optional, wrap it
        if (isOptional()) {
            strategy = strategy.optional();
        }
        
        return strategy;
    }
    
    boolean isOptional() {
        return parameter.getType() == Optional.class;
    }

    @Override
    public String toString() {
        return MessageFormat.format("param[{0}] name[{1}] type[{2}] anno[{3}] opt[{4}]", parameter, name, type, annotation, isOptional());
    }
}
