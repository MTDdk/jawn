package net.javapla.jawn.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public interface Value {
    
    
    default boolean booleanValue() {
        return to(boolean.class);
    }
    
    default boolean booleanValue(final boolean fallback) {
        return toOptional(Boolean.class).orElse(fallback);
    }
    
    default double doubleValue() {
        return to(double.class);
    }
    
    default double doubleValue(final double fallback) {
        return toOptional(Double.class).orElse(fallback);
    }
    
    default int intValue() {
        return to(int.class);
    }
    
    default int intValue(final int fallback) {
        return toOptional(Integer.class).orElse(fallback);
    }
    
    default long longValue() {
        return to(long.class);
    }
    
    default long longValue(final long fallback) {
        return toOptional(Long.class).orElse(fallback);
    }
    
    default String value() {
        return to(String.class);
    }
    
    default <T extends Enum<T>> T toEnum(final Class<T> type) {
        return to(type);
    }

    <T> T to(Class<T> type);
    
    <T> T to(Class<T> type, MediaType mtype) throws Up.ParsableError;
    
    <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType) throws Up.ParsableError;
    
    <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType, MediaType mtype) throws Up.ParsableError;
    
    default List<String> toList() {
        return toList(String.class);
    }
    
    @SuppressWarnings("unchecked")
    default <T> List<T> toList(final Class<T> type) {
        return toCollection(type, List.class);
    }
    
    default Set<String> toSet() {
        return toSet(String.class);
    }
    
    @SuppressWarnings("unchecked")
    default <T> Set<T> toSet(final Class<T> type) {
        return toCollection(type, Set.class);
    }
    
    default SortedSet<String> toSortedSet() {
        return toSortedSet(String.class);
    }
    
    @SuppressWarnings("unchecked")
    default <T> SortedSet<T> toSortedSet(final Class<T> type) {
        return toCollection(type, SortedSet.class);
    }
    
    default Optional<String> toOptional() {
        return toOptional(String.class);
    }
    
    default <T> Optional<T> toOptional(final Class<T> type) {
        if (type.isPrimitive()) throw new IllegalArgumentException("Primitive types are not allowed in type parameters: " + type);
        try {
            return Optional.ofNullable(to(type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    boolean isPresent();
}
