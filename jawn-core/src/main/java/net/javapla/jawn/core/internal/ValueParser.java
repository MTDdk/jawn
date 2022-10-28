package net.javapla.jawn.core.internal;

import java.util.Collection;

import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;

public abstract class ValueParser {
    
    private ValueParser() {}

    @SuppressWarnings("unchecked")
    public static <T> T to(Value value, Class<T> type) {
        return (T) value(value, type/*, type*/);
    }
    
    /*public static Object to(Value value, Type type) {
        return value(value, parameterizedType0(type), type);
    }*/
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> Object value(Value value, Class<T> raw/*, Type type*/) {
        if (raw == Value.class) return value;
        
        // is this really enough as long as we are comparing java.lang.* ?
        if (raw == String.class) return value.value();
        if (raw == int.class || raw == Integer.class) return value.asInt();
        if (raw == long.class || raw == Long.class) return value.asLong();
        if (raw == boolean.class || raw == Boolean.class) return value.asBoolean();
        if (raw == double.class || raw == Double.class) return value.asDouble();
        
        if (Enum.class.isAssignableFrom(raw)) return value.asEnum((Class<? extends Enum>) raw);
        
        // Should not be necessary as long as we have Value#toList + #toSet
        /*if (List.class.isAssignableFrom(raw)) {
            return toCollection(value, parameterizedType0(type), new ArrayList<T>(2));
        }
        if (Set.class.isAssignableFrom(raw)) {
            return toCollection(value, parameterizedType0(type), new LinkedHashSet<T>(2));
        }*/
        
        
        throw Up.ParseError("No parser for type " + raw);
    }
    
    @SuppressWarnings("unchecked")
    public static <C extends Collection<T>, T> C toCollection(Value value, Class<T> type, Collection<T> collection) {
        for (Value v : value) {
            if (v.isPresent())
                collection.add((T) value(v, type/*, type*/));
        }
        
        return (C) collection;
    }
    
    /*private static Class<?> parameterizedType0(Type type) {
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
    }*/
}