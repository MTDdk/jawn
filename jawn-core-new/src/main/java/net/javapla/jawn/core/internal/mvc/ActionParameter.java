package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Optional;

import com.google.inject.util.Types;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.mvc.Body;

// parameter for a request
public class ActionParameter {
    
    @FunctionalInterface
    private interface CalculateValue {
        Object apply(Context ctx, ActionParameter param);
    }
    
    private static final Type/*Literal<Body>*/ bodyType = /*TypeLiteral.get(*/Body.class/*)*/;
    
    private static final HashMap<Type, CalculateValue> converters = new HashMap<>();
    static {
        
        converters.put(bodyType, (ctx, param) -> { return null; } );
        
        /**
         * Request
         */
        converters.put(/*TypeLiteral.get*/(Context.Request.class), (ctx, param) -> ctx.req());
        /**
         * Response
         */
        converters.put(/*TypeLiteral.get*/(Context.Response.class), (ctx, param) -> ctx.resp());
        /**
         * Context
         */
        converters.put(/*TypeLiteral.get*/(Context.class), (ctx, param) -> ctx);
        /**
         * Cookie
         */
        converters.put(Cookie.class, (ctx, param) -> ctx.req().cookies().get(param.name));// case sensitive
        converters.put(Types.collectionOf(Cookie.class), (ctx, param) -> ctx.req().cookies().values());
        converters.put(Types.newParameterizedType(Optional.class, Cookie.class), (ctx, param) -> Optional.ofNullable(ctx.req().cookies().get(param.name)));
        /**
         * Session
         */
        /**
         * Files
         */
        
    }
    
    final Parameter parameter;
    final String name;
    final Type/*Literal<?>*/ type;
    final boolean optional;
    final CalculateValue strategy;

    public ActionParameter(final Parameter parameter, final String name) {
        this.parameter = parameter;
        this.name = name;
        this.type = parameter.getParameterizedType();//TypeLiteral.get(parameter.getParameterizedType());
        this.optional = parameter.getType() == Optional.class;
        
        // look for annotations first
        final Type/*Literal<?>*/ strategyType;
        if (parameter.getAnnotation(Body.class) != null) {
            strategyType = bodyType;
        } else {
            strategyType = this.type;
        }
        
        this.strategy = converters.getOrDefault(strategyType, null);
    }
    
    public Object value(final Context ctx) {
        return strategy.apply(ctx, this);
    }
    
    @Override
    public String toString() {
        return MessageFormat.format("p[{0}] n[{1}] t[{2}] o[{3}]", parameter, name, type, optional);
    }
    
}
