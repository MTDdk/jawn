package net.javapla.jawn.server;

import com.google.inject.AbstractModule;

import net.javapla.jawn.core.FrameworkBootstrap;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.server.api.HttpHandler;

public class ServerBootstrap extends FrameworkBootstrap {
    
    private HttpHandler server;


    public ServerBootstrap() {
    }
    
    public ServerBootstrap(HttpHandler server) {
        this.server = server;
    }
    
    
    @Override
    protected void configure() {
        // initialize core framework
        super.configure();
        
        // add server injectables
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                //bind(Context.class).to(JawnServletContext.class);
                bind(Context.class).to(ServerContext.class);
                if (server != null)
                    bind(HttpHandler.class).toInstance(server);
            }
        });
    }
    
}
