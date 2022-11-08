package net.javapla.jawn.core.internal.reflection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

/**
 * Reify: "To convert mentally into a concrete thing; to materialise"
 * @author MTD
 *
 */
public class Materialise {
    
    /**
     * Returns a type that is functionally equal but not necessarily equal according to {@link
     * Object#equals(Object) Object.equals()}. The returned type is {@link Serializable}.
     */
    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

        } else if (type instanceof CompositeType) {
            return type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(
                p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());

        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());

        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

        } else {
            // type is either serializable as-is or unsupported
            return type;
        }
    }
    
    /**
     * Returns a new parameterized type, applying {@code typeArguments} to
     * {@code rawType} and enclosed by {@code ownerType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType newParameterizedTypeWithOwner(
        Type ownerType, Type rawType, Type... typeArguments) {
      return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
    }

    
    
    /** Returns true if {@code type} is free from type variables. */
    private static boolean isFullySpecified(Type type) {
        if (type instanceof Class) {
            return true;

        } else if (type instanceof CompositeType) {
            return ((CompositeType) type).isFullySpecified();

        } else if (type instanceof TypeVariable) {
            return false;

        } else {
            return ((CompositeType) canonicalize(type)).isFullySpecified();
        }
    }
    
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            /*checkArgument(
                rawType instanceof Class,
                "Expected a Class, but <%s> is of type %s",
                type,
                type.getClass().getName());*/
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            // we could use the variable's bounds, but that'll won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else {
            throw new IllegalArgumentException(
                "Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <"
                    + type
                    + "> is of type "
                    + type.getClass().getName());
        }
    }
    
    /** Returns true if {@code a} and {@code b} are equal. */
    public static boolean equals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;

        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            // TODO: save a .clone() call
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return Objects.equals(pa.getOwnerType(), pb.getOwnerType())
                && pa.getRawType().equals(pb.getRawType())
                && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration().equals(vb.getGenericDeclaration())
                && va.getName().equals(vb.getName());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return false;
        }
    }
    
    public static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }
    
    private static int hashCodeOrZero(Object o) {
        return o != null ? o.hashCode() : 0;
    }
    static void checkNotPrimitive(Type type) {
        if (!(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive())) {
            throw new IllegalArgumentException("Not a primitive type: " + type);
        }
    }
    static void checkNotNull(Type type) {
        if (type == null) {
            throw new NullPointerException(
                "typeArguments[" + type + "]: " );
        }
    }
    
    public static class ParameterizedTypeImpl
    implements ParameterizedType, Serializable, CompositeType {
        private final Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            // require an owner type if the raw type needs it
            ensureOwnerType(ownerType, rawType);

            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments.clone();
            for (int t = 0; t < this.typeArguments.length; t++) {
                checkNotNull(this.typeArguments[t]);
                checkNotPrimitive(this.typeArguments[t]);
                this.typeArguments[t] = canonicalize(this.typeArguments[t]);
            }
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean isFullySpecified() {
            if (ownerType != null && !Materialise.isFullySpecified(ownerType)) {
                return false;
            }

            if (!Materialise.isFullySpecified(rawType)) {
                return false;
            }

            for (Type type : typeArguments) {
                if (!Materialise.isFullySpecified(type)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType
                && Materialise.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(30 * (typeArguments.length + 1));
            stringBuilder.append(typeToString(rawType));

            if (typeArguments.length == 0) {
                return stringBuilder.toString();
            }

            stringBuilder.append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < typeArguments.length; i++) {
                stringBuilder.append(", ").append(typeToString(typeArguments[i]));
            }
            return stringBuilder.append(">").toString();
        }

        private static void ensureOwnerType(Type ownerType, Type rawType) {
            if (rawType instanceof Class<?>) {
                /*Class rawTypeAsClass = (Class) rawType;
                checkArgument(
                    ownerType != null || rawTypeAsClass.getEnclosingClass() == null,
                    "No owner type for enclosed %s",
                    rawType);
                checkArgument(
                    ownerType == null || rawTypeAsClass.getEnclosingClass() != null,
                    "Owner type for unenclosed %s",
                    rawType);*/
            }
        }

        private static final long serialVersionUID = 0;
    }

    public static class GenericArrayTypeImpl
    implements GenericArrayType, Serializable, CompositeType {
        private final Type componentType;

        public GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean isFullySpecified() {
            return Materialise.isFullySpecified(componentType);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType && Materialise.equals(this, (GenericArrayType) o);
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public String toString() {
            return typeToString(componentType) + "[]";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * The WildcardType interface supports multiple upper bounds and multiple lower bounds. We only
     * support what the Java 6 language needs - at most one bound. If a lower bound is set, the upper
     * bound must be Object.class.
     */
    public static class WildcardTypeImpl implements WildcardType, Serializable, CompositeType {
        private final Type upperBound;
        private final Type lowerBound;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            //checkArgument(lowerBounds.length <= 1, "Must have at most one lower bound.");
            //checkArgument(upperBounds.length == 1, "Must have exactly one upper bound.");

            if (lowerBounds.length == 1) {
                checkNotNull(lowerBounds[0]);
                checkNotPrimitive(lowerBounds[0]);
                //checkArgument(upperBounds[0] == Object.class, "bounded both ways");
                this.lowerBound = canonicalize(lowerBounds[0]);
                this.upperBound = Object.class;

            } else {
                //checkNotNull(upperBounds[0], "upperBound");
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = canonicalize(upperBounds[0]);
            }
        }

        @Override
        public Type[] getUpperBounds() {
            return new Type[] {upperBound};
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBound != null ? new Type[] {lowerBound} : EMPTY_TYPE_ARRAY;
        }

        @Override
        public boolean isFullySpecified() {
            return Materialise.isFullySpecified(upperBound)
                && (lowerBound == null || Materialise.isFullySpecified(lowerBound));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && Materialise.equals(this, (WildcardType) other);
        }

        @Override
        public int hashCode() {
            // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            if (lowerBound != null) {
                return "? super " + typeToString(lowerBound);
            } else if (upperBound == Object.class) {
                return "?";
            } else {
                return "? extends " + typeToString(upperBound);
            }
        }

        private static final long serialVersionUID = 0;
    }

    /** A type formed from other types, such as arrays, parameterized types or wildcard types */
    private interface CompositeType {
        /** Returns true if there are no type variables in this type. */
        boolean isFullySpecified();
    }
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};

}
