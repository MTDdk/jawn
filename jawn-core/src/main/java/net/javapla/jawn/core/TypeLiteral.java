package net.javapla.jawn.core;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import net.javapla.jawn.core.internal.reflection.Materialise;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to represent generic types,
 * so this class does. Forces clients to create a subclass of this class which enables retrieval of
 * the type information even at runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can create an empty
 * anonymous inner class:
 *
 * <p>{@code TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};}
 *
 * <p>Along with modeling generic types, this class can resolve type parameters. For example, to
 * figure out what type {@code keySet()} returns on a {@code Map<Integer, String>}, use this code:
 *
 * <pre>{@code
 * TypeLiteral<Map<Integer, String>> mapType
 *     = new TypeLiteral<Map<Integer, String>>() {};
 * TypeLiteral<?> keySetType
 *     = mapType.getReturnType(Map.class.getMethod("keySet"));
 * System.out.println(keySetType); // prints "Set<Integer>"
 * }</pre>
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 * 
 * Borrowed from {@code com.google.inject.TypeLiteral}
 * 
 * @see https://cr.openjdk.java.net/~briangoetz/valhalla/erasure.html
 */
public final class TypeLiteral<T> {
    
    public final Class<? super T> rawType;
    public final Type type;
    public final int hashCode;
    
    /**
     * Constructs a new type literal. Derives represented class from type
     * parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     */
    @SuppressWarnings("unchecked")
    public TypeLiteral() {
      this.type = getSuperclassTypeParameter(getClass());
      this.rawType = (Class<? super T>) Materialise.getRawType(type);
      this.hashCode = type.hashCode();
    }
    
    /** Unsafe. Constructs a type literal manually. */
    @SuppressWarnings("unchecked")
    TypeLiteral(Type type) {
        this.type = Materialise.canonicalize(type);
        this.rawType = (Class<? super T>) Materialise.getRawType(this.type);
        this.hashCode = this.type.hashCode();
    }
    
    /** Unsafe. Constructs a type literal manually. */
    private TypeLiteral(Class<T> type) {
        this.type = type;
        this.rawType = type;
        this.hashCode = type.hashCode();
      }
    
    
    /**
     * Returns the type from super class's type parameter in {@link $Types#canonicalize
     * canonical form}.
     *
     * @param subclass Subclass.
     * @return Type.
     */
    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return Materialise.canonicalize(parameterized.getActualTypeArguments()[0]);
    }
    
    
    /**
     * Gets type literal for the given {@code Class} instance.
     *
     * @param type Java type.
     * @param <T> Generic type.
     * @return Gets type literal for the given {@code Class} instance.
     */
    public static <T> TypeLiteral<T> get(Class<T> type) {
        return new TypeLiteral<>(type);
    }
    
    /**
     * Get raw type (class) from given type.
     *
     * @param type Type.
     * @return Raw type.
     */
    public static Class<?> rawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        return new TypeLiteral<>(type).rawType;
    }

    /**
     * Gets type literal for the parameterized type represented by applying {@code typeArguments} to
     * {@code rawType}.
     *
     * @param rawType Raw type.
     * @param typeArguments Parameter types.
     * @return Gets type literal for the parameterized type represented by applying
     *    {@code typeArguments} to {@code rawType}.
     */
    public static TypeLiteral<?> getParameterized(Type rawType,
                                                      Type... typeArguments) {
        return new TypeLiteral<>(Materialise.newParameterizedTypeWithOwner(null, rawType, typeArguments));
    }
}
