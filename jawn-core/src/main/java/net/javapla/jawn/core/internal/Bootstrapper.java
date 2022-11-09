package net.javapla.jawn.core.internal;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Parser;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.Plugin.Application;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.ReifiedGenerics;
import net.javapla.jawn.core.Renderer;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.internal.reflection.ClassSource;
import net.javapla.jawn.core.internal.reflection.RouteClassAnalyser;

public class Bootstrapper {
    
    private final ResponseRenderer renderer = new ResponseRenderer();
    private final ClassLoader classLoader;
    
    public Bootstrapper(ClassLoader classLoader) {
        this.classLoader = classLoader;
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
        
        installPlugins(moduleConfig);
        
        registerCoreClasses();
        
        
        parseRoutes(routes, router);
        
        return moduleConfig;
    }
    
    private void installPlugins(Plugin.Application moduleConfig) {
        // read template engines
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        plugins.forEach(plugin -> {
            plugin.install(moduleConfig);
        });
    }
    
    private void registerCoreClasses() {
        
    }
    
    private void parseRoutes(Stream<Route.Builder> routes, RouterImpl router) {
        ClassSource source = new ClassSource(classLoader);
        RouteClassAnalyser analyser = new RouteClassAnalyser(source);
        
        
        routes.map(bob -> {
            bob.renderer(renderer.renderer(bob.produces()));
            
            // pipeline
            pipeline(bob, source, analyser);
                
            return bob.build();
        }).forEach(router::addRoute);

        
        source.close();
    }
    
    private void pipeline(Route.Builder bob, ClassSource source, RouteClassAnalyser analyser) {
        Type returnType = bob.returnType();
        if (returnType == null) {
            returnType = analyser.returnType(bob.originalHandler);
        }
        
        Class<?> raw = ReifiedGenerics.rawType(returnType);
        
        bob.execution(execution(raw, bob));
    }
    
    private Route.Execution execution(Class<?> raw, final Route.Builder bob) {
        
        /* Bytes */
        if (byte[].class == raw) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    Object result = handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond((byte[])result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                    e.printStackTrace();
                }
            };
        }
        
        /* VOID */
        if (void.class == raw) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond(Status.OK);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }
        
        return defaultExecution(bob);
    }
    
    private Route.Execution defaultExecution(Route.Builder bob) {
        final Route.Handler handler = bob.handler();
        final Renderer renderer = bob.renderer();
        
        return ctx -> {
            
            try {
                Object result = handler.handle(ctx);
                
                if (!ctx.resp().isResponseStarted()) {
                    
                    if (result instanceof Context) {
                        ctx.resp().status(200);
                    } else {
                
                        byte[] rendered = renderer.render(ctx, result);
                        
                        if (rendered != null) {
                            System.out.println("Response has not been handled");
                            ctx.resp().respond(Status.NO_CONTENT);
                        }
                    }
                    
                }
            } catch (Exception e ) {
                e.printStackTrace();
            }
        };
    }
}
