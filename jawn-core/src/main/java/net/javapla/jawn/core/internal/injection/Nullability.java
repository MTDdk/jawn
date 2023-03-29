package net.javapla.jawn.core.internal.injection;

import java.lang.annotation.Annotation;

public class Nullability {

    private Nullability() {}

    public static boolean allowsNull(Annotation[] annotations) {
        for (Annotation a : annotations) {
            Class<? extends Annotation> type = a.annotationType();
            if ("Nullable".equals(type.getSimpleName())) {
                return true;
            }
        }
        return false;
    }
}
