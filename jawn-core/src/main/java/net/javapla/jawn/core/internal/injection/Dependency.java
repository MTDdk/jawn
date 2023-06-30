package net.javapla.jawn.core.internal.injection;

import net.javapla.jawn.core.Registry.Key;

public final class Dependency<T> {
    
    final InjectionPoint injectionPoint;
    final Key<T> key;
    final boolean nullable;
    final int parameterIndex;
    
    Dependency(InjectionPoint injectionPoint, Key<T> key, boolean nullable, int index) {
        this.injectionPoint = injectionPoint;
        this.key = key;
        this.nullable = nullable;
        this.parameterIndex = index;
    }
    
    @Override
    public String toString() {
        return String.format(getClass().getSimpleName() + "[injectionPoint=%s, key=%s, nullable=%s, parameterIndex=%d]", injectionPoint, key, nullable, parameterIndex);
    }

}
