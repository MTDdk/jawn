package net.javapla.jawn.core.internal;

import java.util.LinkedList;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.javapla.jawn.core.internal.injection.Injector;
import net.javapla.jawn.core.internal.reflection.ClassSource;
import net.javapla.jawn.core.internal.reflection.RouteClassAnalyser;

public class Bootstrapper {
    protected static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);
    
    private final ParserRenderEngine engine = new ParserRenderEngine();
    
    private final ClassLoader classLoader;
    private final ClassSource source;
    private final Config config;
    private final /*InjectionRegistry*/ Injector registry;
    
    private final LinkedList<Plugin> userPlugins = new LinkedList<>();
    private final LinkedList<Runnable> onStartup = new LinkedList<>();
    private final LinkedList<Runnable> onShutdown = new LinkedList<>();

    
    
    public Bootstrapper(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.source = new ClassSource(classLoader);
        this.config = ConfigFactory.load(classLoader);
        this.registry = new Injector();//new InjectionRegistry();
    }
    public Bootstrapper() {
        this(ClassLoader.getSystemClassLoader());
    }
    
    public synchronized Application boot(Function<Registry, Stream<Route.Builder>> routes) {
        
        RouterImpl router = new RouterImpl();
        
        
        Plugin.Application moduleConfig = new Plugin.Application() {

            @Override
            public Registry/*.ServiceRegistry*/ registry() {
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
        
        // signal startup
        startup();
        
        return moduleConfig;
    }
    
    public Config config() {
        return config;
    }
    
    public Registry registry() {
        return registry;
    }
    
    public ClassLoader classLoader() {
        return classLoader;
    }
    
    public void onStartup(Runnable task) {
        onStartup.add(task);
    }
    public void onShutdown(Runnable task) {
        onShutdown.add(task);
    }
    
    public void install(Plugin plugin) {
        userPlugins.add(plugin);
    }
    
    private void startup() {
        onShutdown(() -> source.close());
        
        onStartup.forEach(run -> {
            try {
                run.run();
            } catch (Exception e) {
                log.error("Failed an onStartup task", e);
            }
        });
    }
    
    private void installPlugins(Plugin.Application moduleConfig) {
        
        try {
            // read template engines
            ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
            plugins.forEach(plugin -> {
                plugin.install(moduleConfig);
            });
            
            // then register/overwrite with user plugins
            userPlugins.forEach(plugin -> {
                plugin.install(moduleConfig);
            });
        } catch (ServiceConfigurationError error) {
            log.debug("installPlugins",error);
        }
        
    }
    
    private void registerCoreClasses(Injector/*InjectionRegistry*/ registry, Config config) {
        registry.register(Config.class, config);
        registry.register(ClassSource.class, source);
    }
    
    private void parseRoutes(Function<Registry, Stream<Route.Builder>> routes, RouterImpl router) {
        // TODO use ClassSource as field in order to remove bytecode in case we want to autoreload classes
            
        RouteClassAnalyser analyser = new RouteClassAnalyser(source);
        
        routes.apply(registry).map(bob -> {
            
            return Pipeline.compile(analyser, engine, bob);
            
        }).forEach(router::addRoute);
    }
}
