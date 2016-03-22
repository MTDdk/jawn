package net.javapla.jawn.server;

import com.google.inject.AbstractModule;

import net.javapla.jawn.core.FrameworkBootstrap;
import net.javapla.jawn.core.http.Context;

public class ServerBootstrap extends FrameworkBootstrap {
    
    @Override
    protected void configure() {
        // initialize core framework
        super.configure();
        
        // add server injectables
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).to(JawnServletContext.class);
            }
        });
    }
    
}
