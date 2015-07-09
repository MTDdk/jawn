package net.javapla.jawn.security;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        
        
        // Instantiates the security manager with a reference to a DataSource
        // If the DataSource is not set, then it will just use built-in jawn_default_security.ini
        bind(JawnSecurityManagerFactory.class).in(Scopes.SINGLETON);
        
    }

}
