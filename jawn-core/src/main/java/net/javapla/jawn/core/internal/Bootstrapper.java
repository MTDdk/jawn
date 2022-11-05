package net.javapla.jawn.core.internal;

import java.util.ServiceLoader;
import java.util.stream.Stream;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Parser;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.Plugin.Application;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Renderer;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router;

public class Bootstrapper {
    
    private final ResponseRenderer renderer = new ResponseRenderer();

    
    
    public Bootstrapper() {
        
    }
    
    public synchronized Application boot(Stream<Route.Builder> routes) {
        
        RouterImpl router = new RouterImpl();
        
        Plugin.Application moduleConfig = new Plugin.Application() {

            @Override
            public Registry.ServiceRegistry registry() {
                return new InjectionRegistry();
            }

            @Override
            public Router router() {
                return router;
            }

            @Override
            public void renderer(MediaType type, Renderer renderer) {
                Bootstrapper.this.renderer.add(type, renderer);
            }
            
            @Override
            public void parser(MediaType type, Parser parser) {}

            @Override
            public void onStartup(Runnable task) {}

            @Override
            public void onShutdown(Runnable task) {}
            
        };
        
        readPlugins(moduleConfig);
        
        
        routes.map(bob -> bob.renderer(renderer.renderer(bob.produces())).build()).forEach(router::addRoute);
        
        return moduleConfig;
    }
    
    private void readPlugins(Plugin.Application moduleConfig) {
        // read template engines
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        plugins.forEach(plugin -> {
            plugin.install(moduleConfig);
        });
    }
}
