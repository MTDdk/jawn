package net.javapla.jawn.impl;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.i18n.Lang;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class FrameworkModule extends AbstractModule {

    private final PropertiesImpl properties;
    private final Router router;

    public FrameworkModule(PropertiesImpl properties, Router router) {
        this.properties = properties;
        this.router = router;
    }
    
    @Override
    protected void configure() {
        bind(PropertiesImpl.class).toInstance(properties);
        bind(Lang.class).in(Singleton.class);
        
        bind(Router.class).toInstance(router);
        
        bind(Context.class).to(ContextImpl.class);
        
        
    }

}
