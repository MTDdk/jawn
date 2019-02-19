package net.javapla.jawn.core;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import com.google.inject.util.Types;

public interface Value {
    
    

    <T> T to(ParameterizedType type) throws Up.ParsableError;
    
    default <T> T to(Class<T> type) throws Up.ParsableError {
        return to(Types.newParameterizedType(type));
    }
    
    <T> T to(ParameterizedType type, MediaType mtype) throws Up.ParsableError;
    
    default <T> T to(Class<T> type, MediaType mtype) throws Up.ParsableError {
        return to(Types.newParameterizedType(type), mtype);
    }
    
    /*@SuppressWarnings("unchecked")
    default <T> T to(ParameterizedType type) {
        return to((Class<T>) type.getRawType());
    }*/
    
    default String value() {
        return to(String.class);
    }
    
    default Optional<String> toOptional() {
        return toOptional(String.class);
    }
    
    @SuppressWarnings("unchecked")
    default <T> Optional<T> toOptional(final Class<T> type) {
        return (Optional<T>) to(Types.newParameterizedType(Optional.class, type));
    }

}
