package net.javapla.jawn.core.internal.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import net.javapla.jawn.core.TypeLiteral;
import net.javapla.jawn.core.internal.reflection.Materialise;

public class Types {

    static List<Class<?>> getParameterTypes(Class<?> type, Member methodOrConstructor) {
        TypeLiteral<?> typeLiteral = TypeLiteral.get(type);
        
        Type[] genericParameterTypes;
        
        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            genericParameterTypes = method.getGenericParameterTypes();
        } else if (methodOrConstructor instanceof Constructor) {
            Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
            genericParameterTypes = constructor.getGenericParameterTypes();
        } else {
            throw new IllegalArgumentException("Not a method or constructor");
        }
        
        return resolveAll(typeLiteral, genericParameterTypes);
    }
    
    static List<Class<?>> resolveAll(TypeLiteral<?> typeLiteral, Type[] types) {
        Class<?>[] result = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = resolve(typeLiteral, types[i]);
        }
        return Arrays.asList(result);// TODO should be immutable
    }
    
    static Class<?> resolve(TypeLiteral<?> typeLiteral, Type type) {
         return Materialise.getRawType(resolveType(typeLiteral, type));
    }
    
    static Type resolveType(TypeLiteral<?> typeLiteral, Type toResolve) {
        // this implementation is made a little more complicated in an attempt to avoid object-creation
        while (true) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable<?> original = (TypeVariable<?>) toResolve;
                toResolve = resolveTypeVariable(typeLiteral.type, typeLiteral.rawType, original);
                if (toResolve == original) {
                    return toResolve;
                }

            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType original = (GenericArrayType) toResolve;
                Type componentType = original.getGenericComponentType();
                Type newComponentType = resolveType(typeLiteral,componentType);
                return componentType == newComponentType ? original : Materialise.arrayOf(newComponentType);

            } else if (toResolve instanceof ParameterizedType) {
                ParameterizedType original = (ParameterizedType) toResolve;
                Type ownerType = original.getOwnerType();
                Type newOwnerType = resolveType(typeLiteral,ownerType);
                boolean changed = newOwnerType != ownerType;

                Type[] args = original.getActualTypeArguments();
                for (int t = 0, length = args.length; t < length; t++) {
                    Type resolvedTypeArgument = resolveType(typeLiteral,args[t]);
                    if (resolvedTypeArgument != args[t]) {
                        if (!changed) {
                            args = args.clone();
                            changed = true;
                        }
                        args[t] = resolvedTypeArgument;
                    }
                }

                return changed
                    ? Materialise.newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
                        : original;

            } else if (toResolve instanceof WildcardType) {
                WildcardType original = (WildcardType) toResolve;
                Type[] originalLowerBound = original.getLowerBounds();
                Type[] originalUpperBound = original.getUpperBounds();

                if (originalLowerBound.length == 1) {
                    Type lowerBound = resolveType(typeLiteral,originalLowerBound[0]);
                    if (lowerBound != originalLowerBound[0]) {
                        return supertypeOf(lowerBound);
                    }
                } else if (originalUpperBound.length == 1) {
                    Type upperBound = resolveType(typeLiteral,originalUpperBound[0]);
                    if (upperBound != originalUpperBound[0]) {
                        return subtypeOf(upperBound);
                    }
                }
                return original;

            } else {
                return toResolve;
            }
        }
    }
    
    public static Type resolveTypeVariable(Type type, Class<?> rawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // we can't reduce this further
        if (declaredByRaw == null) {
            return unknown;
        }

        Type declaredBy = getGenericSupertype(type, rawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }
    
    /**
     * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
     * a class.
     */
    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }
    
    /**
     * Returns the generic supertype for {@code type}. For example, given a class {@code IntegerSet},
     * the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the result when
     * the supertype is {@code Collection.class} is {@code Collection<Integer>}.
     */
    public static Type getGenericSupertype(Type type, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return type;
        }

        // we skip searching through interfaces if unknown is an interface
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // check our supertypes
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // we can't resolve this further
        return toResolve;
    }
    
    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0; i < array.length; i++) {
            if (toFind.equals(array[i])) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }
    
    
    /**
     * Returns a type that represents an unknown type that extends {@code bound}. For example, if
     * {@code bound} is {@code CharSequence.class}, this returns {@code ? extends CharSequence}. If
     * {@code bound} is {@code Object.class}, this returns {@code ?}, which is shorthand for {@code ?
     * extends Object}.
     */
    public static WildcardType subtypeOf(Type bound) {
        return new Materialise.WildcardTypeImpl(new Type[] {bound}, Materialise.EMPTY_TYPE_ARRAY);
    }

    /**
     * Returns a type that represents an unknown supertype of {@code bound}. For example, if {@code
     * bound} is {@code String.class}, this returns {@code ? super String}.
     */
    public static WildcardType supertypeOf(Type bound) {
        return new Materialise.WildcardTypeImpl(new Type[] {Object.class}, new Type[] {bound});
    }
    
}
