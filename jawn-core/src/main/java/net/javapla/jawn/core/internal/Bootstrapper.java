package net.javapla.jawn.core.internal;

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
    
    
    public Bootstrapper(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.config = ConfigFactory.parseResources(classLoader, "jawn.conf");
    }
    
    public synchronized Application boot(Stream<Route.Builder> routes) {
        
        RouterImpl router = new RouterImpl();
        InjectionRegistry registry = new InjectionRegistry();
        
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
    
    private void installPlugins(Plugin.Application moduleConfig) {
        // read template engines
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        plugins.forEach(plugin -> {
            plugin.install(moduleConfig);
        });
    }
    
    private void registerCoreClasses(InjectionRegistry registry, Config config) {
        registry.register(Config.class, config);
    }
    
    private void parseRoutes(Stream<Route.Builder> routes, RouterImpl router) {
        ClassSource source = new ClassSource(classLoader);
        RouteClassAnalyser analyser = new RouteClassAnalyser(source);
        
        
        routes.map(bob -> {
            
            return Pipeline.compile(source, analyser, engine, bob);
            /*
            // Add parsers to later be available in Context.
            // Unfortunately we have to let them be chained 
            // through Route.Builder -> Route -> AbstractContext
            bob.parsers(engine);
            
            
            // if the route has multiple possible response types, 
            // we want to look at the request's ACCEPT-header to pick one of them for us.
            if (bob.produces().size() > 1) {
                bob.before(Route.RESPONSE_CONTENT_TYPE.apply(bob.produces()));
            } else {
                // If only a single option is available then always set response type accordingly.
                bob.before(ctx -> ctx.resp().contentType(bob.fallbackResponseType()));
            }
            
            // TODO insert Parsers somewhere, so they are reachable from handlers
            // which probably means they have to be reachable from Context
            
            //bob.renderer(engine.render(bob.fallbackResponseType()));
            
            // pipeline
            pipeline(bob, source, analyser);
                
            return bob.build();*/
        }).forEach(router::addRoute);

        
        source.close();
    }
    
    /*private void pipeline(Route.Builder bob, ClassSource source, RouteClassAnalyser analyser) {
        Type returnType = bob.returnType();
        if (returnType == null) {
            returnType = analyser.returnType(bob.originalHandler);
        }
        
        Class<?> raw = TypeLiteral.rawType(returnType);
        
        bob.execution(execution(raw, bob));
    }
    
    private Route.Execution execution(Class<?> raw, final Route.Builder bob) {
        
        // ** Bytes ** //
        if (byte[].class == raw) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    Object result = handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond((byte[])result);
                    }
                } catch (Exception e) {
                    ctx.error(e);
                }
            };
        }
        if (ByteBuffer.class.isAssignableFrom(raw)) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    Object result = handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond((ByteBuffer)result);
                    }
                } catch (Exception e) {
                    ctx.error(e);
                }
            };
        }
        
        // ** void ** //
        if (void.class == raw) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond(Status.OK);
                    }
                } catch (Exception e) {
                    ctx.error(e);
                }
            };
        }
        
        return defaultExecution(bob);
    }
    
    private Route.Execution defaultExecution(Route.Builder bob) {
        final Route.Handler handler = bob.handler();
        //final Renderer renderer = bob.renderer();
        
        return ctx -> {
            
            try {
                Object result = handler.handle(ctx);
                
                if (!ctx.resp().isResponseStarted()) {
                    if (result instanceof Context) {
                        ctx.resp().respond(Status.OK);
                    } else {
                        byte[] rendered = engine.render(ctx.resp().contentType()).render(ctx, result);
                        if (rendered != null) {
                            System.out.println("Response has not been handled");
                            ctx.resp().respond(Status.NO_CONTENT);
                        }
                    }
                }
            } catch (Exception e ) {
                ctx.error(e);
            }
        };
    }*/
}
