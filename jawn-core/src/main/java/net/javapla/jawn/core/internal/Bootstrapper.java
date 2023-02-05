package net.javapla.jawn.core.internal;

import java.util.LinkedList;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Parser;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.Plugin.Application;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Renderer;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.internal.reflection.ClassSource;
import net.javapla.jawn.core.internal.reflection.RouteClassAnalyser;

public class Bootstrapper {
    
    private final ParserRenderEngine engine = new ParserRenderEngine();
    
    private final ClassLoader classLoader;
    private final Config config;
    private final InjectionRegistry registry;
    
    private final LinkedList<Plugin> userPlugins = new LinkedList<>();

    
    
    public Bootstrapper(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.config = ConfigFactory.parseResources(classLoader, "jawn.conf");
        this.registry = new InjectionRegistry();
    }
    
    public synchronized Application boot(Stream<Route.Builder> routes) {
        
        RouterImpl router = new RouterImpl();
        
        
        Plugin.Application moduleConfig = new Plugin.Application() {

            @Override
            public Registry.ServiceRegistry registry() {
                return registry;
            }

            @Override
            public Router router() {
                return router;
            }
            
            @Override
            public Config config() {
                return config;
            }

            @Override
            public void renderer(MediaType type, Renderer renderer) {
                Bootstrapper.this.engine.add(type, renderer);
            }
            
            @Override
            public void parser(MediaType type, Parser parser) {
                Bootstrapper.this.engine.add(type, parser);
            }

            @Override
            public void onStartup(Runnable task) {}

            @Override
            public void onShutdown(Runnable task) {}
            
        };
        
        registerCoreClasses(registry, config);
        
        installPlugins(moduleConfig);
        
        
        parseRoutes(routes, router);
        
        return moduleConfig;
    }
    
    public Config config() {
        return config;
    }
    
    public Registry registry() {
        return registry;
    }
    
    public void install(Plugin plugin) {
        userPlugins.add(plugin);
    }
    
    private void installPlugins(Plugin.Application moduleConfig) {
        // read template engines
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        plugins.forEach(plugin -> {
            plugin.install(moduleConfig);
        });
        
        // then register/overwrite with user plugins
        userPlugins.forEach(plugin -> {
            plugin.install(moduleConfig);
        });
    }
    
    private void registerCoreClasses(InjectionRegistry registry, Config config) {
        registry.register(Config.class, config);
    }
    
    private void parseRoutes(Stream<Route.Builder> routes, RouterImpl router) {
        try (ClassSource source = new ClassSource(classLoader)) {
            
            RouteClassAnalyser analyser = new RouteClassAnalyser(source);
            
            routes.map(bob -> {
                
                return Pipeline.compile(source, analyser, engine, bob);
                
            }).forEach(router::addRoute);
        }
    }
}
