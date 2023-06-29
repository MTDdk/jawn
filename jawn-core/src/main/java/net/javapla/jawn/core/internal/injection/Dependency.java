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
        return new D(injectionPoint, key, nullable, parameterIndex).toString();
    }

    record D<T>(InjectionPoint injectionPoint, Key<T> key, boolean nullable, int index) {}
}
