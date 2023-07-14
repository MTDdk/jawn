package net.javapla.jawn.core.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.function.Function;

import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;

public abstract class ValueParser {
    
    private ValueParser() {}

    @SuppressWarnings("unchecked")
    public static <T> T to(Value value, Class<T> type) {
        //return (T) value(value, type/*, type*/);
        
        return (T) converter(type).apply(value);
    }
    
    public static Object to(Value value, Type type) {
        //return value(value, parameterizedType0(type));
        
        return converter(parameterizedType0(type)).apply(value);
    }
    
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    private static <T> Object value(Value value, Class<T> raw/*, Type type*/) {
//        if (raw == Value.class) return value;
//        
//        // is this really enough as long as we are comparing java.lang.* ?
//        if (raw == String.class) return value.value();
//        if (raw == int.class || raw == Integer.class) return value.asInt();
//        if (raw == long.class || raw == Long.class) return value.asLong();
//        if (raw == boolean.class || raw == Boolean.class) return value.asBoolean();
//        if (raw == double.class || raw == Double.class) return value.asDouble();
//        
//        if (Enum.class.isAssignableFrom(raw)) return value.asEnum((Class<? extends Enum>) raw);
//        
//        // Should not be necessary as long as we have Value#toList + #toSet
//        /*if (List.class.isAssignableFrom(raw)) {
//            return toCollection(value, parameterizedType0(type), new ArrayList<T>(2));
//        }
//        if (Set.class.isAssignableFrom(raw)) {
//            return toCollection(value, parameterizedType0(type), new LinkedHashSet<T>(2));
//        }*/
//        
//        
//        throw Up.ParseError("No parser for type " + raw);
//    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Function<Value, ?> converter(Class<?> t) {
        if (t == Value.class) return Function.identity();
        
        if (t == String.class) return v -> v.value();
        
        if (t == int.class) return v -> v.asInt();
        if (t == Integer.class) return v -> v.isPresent() ? v.asInt() : null;
        
        if (t == long.class) return v -> v.asLong();
        if (t == Long.class) return v -> v.isPresent() ? v.asLong() : null;
        
        if (t == boolean.class) return v -> v.asBoolean();
        if (t == Boolean.class) return v -> v.isPresent() ? v.asBoolean() : null;
        
        if (t == double.class) return v -> v.asDouble();
        if (t == Double.class) return v -> v.isPresent() ? v.asDouble() : null;
        
        if (Enum.class.isAssignableFrom(t)) return v -> v.asEnum((Class<? extends Enum>) t);
        
        throw Up.ParseError("No parser for type " + t);
    }
    
    public static Function<Value, ?> converter(Type t) {
        return converter(parameterizedType0(t));
    }
    
    @SuppressWarnings("unchecked")
    public static <C extends Collection<T>, T> C toCollection(Value value, Class<T> type, Collection<T> collection) {
        for (Value v : value) {
            if (v.isPresent())
                //collection.add((T) value(v, type/*, type*/));
                collection.add((T) converter(type).apply(v));
        }
        
        return (C) collection;
    }
    
    private static Class<?> parameterizedType0(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType0(parameterizedType.getActualTypeArguments()[0]);
        } else if (type instanceof WildcardType) {
            return parameterizedType0(((WildcardType) type).getUpperBounds()[0]);
        } else {
            // We expect a parameterized type like: List/Set/Optional, but there is no type information
            // fallback to String
            return String.class;
        }
    }
    
}