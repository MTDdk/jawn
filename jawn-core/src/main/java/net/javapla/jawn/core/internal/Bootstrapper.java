package net.javapla.jawn.core.internal;

import java.util.stream.Stream;

import net.javapla.jawn.core.Module;
import net.javapla.jawn.core.Module.Application;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router;

public class Bootstrapper {
    
    private final ResponseRenderer renderer = new ResponseRenderer();

    
    
    public Bootstrapper() {
        
    }
    
    public synchronized Application boot(Stream<Route.Builder> routes) {
        
        RouterImpl router = new RouterImpl();
        
        routes.map(bob -> bob.renderer(renderer.renderer(bob.produces())).build()).forEach(router::addRoute);
        
        Module.Application moduleConfig = new Module.Application() {

            @Override
            public Registry.ServiceRegistry registry() {
                return new InjectionRegistry();
            }

            @Override
            public Router router() {
                return router;
            }

            @Override
            public void onStartup(Runnable task) {}

            @Override
            public void onShutdown(Runnable task) {}
            
        };
        
        
        
        return moduleConfig;
    }
}
