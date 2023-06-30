package net.javapla.jawn.core.internal.injection;

public interface Provider<T> {
    
    /**
     * Provides an instance of {@code T}.
     *
     * @throws OutOfScopeException when an attempt is made to access a scoped object while the scope
     *     in question is not currently active
     * @throws ProvisionException if an instance cannot be provided. Such exceptions include messages
     *     and throwables to describe why provision failed.
     */
    T get();

}
