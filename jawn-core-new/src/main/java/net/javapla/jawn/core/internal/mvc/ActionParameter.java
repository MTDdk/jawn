package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.inject.util.Types;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.mvc.Body;

// parameter for a request
public class ActionParameter {
    
    @FunctionalInterface
    private interface CalculateValue {
        Object apply(Context ctx, ActionParameter param) throws Exception;
    }
    
    private static final Type bodyType = Body.class;
    
    private static final HashMap<Type, CalculateValue> converters = new HashMap<>();
    static {
        
        /**
         * @Body
         */
        converters.put(bodyType, (ctx, param) -> { return ctx.req().body((Class<?>) param.type); } );
        
        
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
    final boolean optional, list, set;
    final CalculateValue strategy;

    public ActionParameter(final Parameter parameter, final String name) {
        this.parameter = parameter;
        this.name = name;
        this.type = parameter.getParameterizedType();
        this.optional = parameter.getType() == Optional.class;
        this.list = parameter.getType() == List.class;
        this.set = parameter.getType() == Set.class;
        
        
        // look for annotations first
        final Type strategyType;
        if (parameter.getAnnotation(Body.class) != null) {
            strategyType = bodyType;
        } else {
            strategyType = this.type;
        }
        
        this.strategy = converters.getOrDefault(strategyType, param());
    }
    
    public Object value(final Context ctx) throws Throwable {
        return strategy.apply(ctx, this);
    }
    
    @Override
    public String toString() {
        return MessageFormat.format("p[{0}] n[{1}] t[{2}] o[{3}]", parameter, name, type, optional);
    }

    private static final CalculateValue param() {
        return (ctx, param) -> {
            Value value = ctx.param(param.name);
            
            if (value.isPresent()) {
                
                //if (clazz(param.type) == Collection.class) {
                /*System.out.println(param.parameter.getType());
                Class<?> clazz = clazz(param.type);
                System.out.println(clazz);
                if (ReflectionMetadata.isAssignableFrom(param.parameter.getType()clazz, Collection.class)) {
                    System.out.println("type collections");
                    if (clazz == List.class) {
                        return value.toList(childOrContainer(param.type));
                    }
                }*/
                
                if (param.optional) {
                    return value.toOptional(childOrContainer(param.type));
                } else if (param.list) {
                    return value.toList(childOrContainer(param.type));
                } else if (param.set) {
                    return value.toSet(childOrContainer(param.type));
                }
                
                return value.to(childOrContainer(param.type));
            }
            
            return null;
        };
    }
    
    /*private static final Class<?> clazz(Type type) {
        if (type instanceof ParameterizedType) {
//            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
//            if (types != null) return (Class<?>) types[0];
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return (Class<?>) type;
    }*/
    
    private static final Class<?> childOrContainer(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types != null) return (Class<?>) types[0];
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return (Class<?>) type;
    }
}
