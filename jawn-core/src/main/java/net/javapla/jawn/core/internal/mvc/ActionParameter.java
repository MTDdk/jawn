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
import net.javapla.jawn.core.Session;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.mvc.Body;
import net.javapla.jawn.core.mvc.PathParam;
import net.javapla.jawn.core.mvc.QueryParam;

// parameter for a request
public class ActionParameter {
    
    @FunctionalInterface
    private interface CalculateValue {
        Object apply(Context ctx, ActionParameter param) throws Exception;
    }
    private interface Valuable {
        Value apply(Context ctx, ActionParameter param) throws Exception;
    }
    
    private static final Type bodyType = Body.class;
    //private static final Type paramType = Param.class;
    private static final Type pathParamType = PathParam.class;
    private static final Type queryParamType = QueryParam.class;
    //private static final Type formDataType = FormData.class;
    
    private static final HashMap<Type, CalculateValue> converters = new HashMap<>();
    static {
        
        /**
         * @Body
         */
        converters.put(bodyType, (ctx, param) -> { return ctx.req().body((Class<?>) param.type); } );
        /**
         * @Param
         */
        //converters.put(paramType, param() );//(ctx, param) -> { return ctx.param(param.name).to(param.type); });
        /**
         * @PathParam
         */
        converters.put(pathParamType, wrap((ctx, param)->  ctx.req().pathParam(param.name))  );//(ctx, param)-> { return ctx.req().pathParam(param.name).to(param.type); });
        /**
         * @QueryParam
         */
        converters.put(queryParamType, wrap((ctx, param) -> { if(param.list || param.set) return ctx.req().queryParams(param.name); else return ctx.req().queryParam(param.name); } ));//(ctx, param)-> { return wrap(ctx.req().queryParam(param.name));/*.to(param.type);*/ });
        /**
         * @FormParam
         */
        //converters.put(formDataType, wrap((ctx, param)->  ctx.req().formData() );
        
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
        converters.put(Session.class, (ctx, param) -> ctx.session());
        converters.put(Types.newParameterizedType(Optional.class, Session.class), (ctx, param) -> ctx.sessionOptionally());
        /**
         * Files
         */
        
    }
    
    final Parameter parameter;
    final String name;
    final Type/*Literal<?>*/ type;
    final boolean optional, list, set, value;
    final CalculateValue strategy;

    public ActionParameter(final Parameter parameter, final String name) {
        this.parameter = parameter;
        this.name = name;
        this.type = parameter.getParameterizedType();
        this.optional = parameter.getType() == Optional.class;
        this.list = parameter.getType() == List.class;
        this.set = parameter.getType() == Set.class;
        this.value = parameter.getType() == Value.class;
        
        
        // look for annotations first
        final Type strategyType;
        if (parameter.getAnnotation(Body.class) != null) {
            strategyType = bodyType;
//        } else if (parameter.getAnnotation(Param.class) != null) {
//            strategyType = paramType; // not necessary as Context#param is the default strategy
        } else if (parameter.getAnnotation(PathParam.class) != null) {
            strategyType = pathParamType;
        } else if (parameter.getAnnotation(QueryParam.class) != null) {
            strategyType = queryParamType;
        } else {
            strategyType = this.type; // Context#param is the default strategy
        }
        
        this.strategy = converters.getOrDefault(strategyType, param());
    }
    
    public Object value(final Context ctx) throws Throwable {
        return strategy.apply(ctx, this);
    }
    
    @Override
    public String toString() {
        return MessageFormat.format("param[{0}] name[{1}] type[{2}] opt[{3}]", parameter, name, type, optional);
    }

    private static final CalculateValue param() {
        return wrap((ctx, param) -> ctx.param(param.name) );
        
        /*return (ctx, param) -> {
            Value value = ctx.param(param.name);
            
            if (value.isPresent()) {
                
                if (param.optional) {
                    return value.toOptional(childOrContainer(param.type));
                } else if (param.list) {
                    return value.toList(childOrContainer(param.type));
                } else if (param.set) {
                    return value.toSet(childOrContainer(param.type));
                }
                
                return value.to(childOrContainer(param.type));
                
            } else {
                
                if (param.optional) {
                    return Optional.empty();
                } else if (param.list) {
                    return List.of();
                } else if (param.set) {
                    return Set.of();
                }
            }
            
            return null;
        };*/
    }
    
    private static final CalculateValue wrap(Valuable work) {
        return (ctx, param) -> {
            
            Value value = work.apply(ctx, param);
            
            if (value.isPresent()) {
                
                if (param.optional) {
                    return value.asOptional(childOrContainer(param.type));
                } else if (param.list) {
                    return value.asList(childOrContainer(param.type));
                } else if (param.set) {
                    return value.asSet(childOrContainer(param.type));
                } else if (param.value) {
                    return value;
                }
                
                return value.as(childOrContainer(param.type));
                
            } else {
                
                if (param.optional) {
                    return Optional.empty();
                } else if (param.list) {
                    return List.of();
                } else if (param.set) {
                    return Set.of();
                } else if (param.value) {
                    return value;
                }
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
