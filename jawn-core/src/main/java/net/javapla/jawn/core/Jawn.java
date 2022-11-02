package net.javapla.jawn.core;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.Module.Application;
import net.javapla.jawn.core.Server.ServerConfig;
import net.javapla.jawn.core.internal.Bootstrapper;
import net.javapla.jawn.core.internal.ResponseRenderer;
import net.javapla.jawn.core.internal.reflection.Reflection;

public class Jawn {
    
    protected static final Logger log = LoggerFactory.getLogger(Jawn.class);
    
    
    private final Bootstrapper booter = new Bootstrapper(); // Core?
    private final LinkedList<Route.Builder> routes = new LinkedList<>();
    private final Renderer renderer = new ResponseRenderer();
    
    public Jawn() {
        System.out.println("Jawning");
    }
    
    protected Route.Builder get(final String path) {
        return _route(HttpMethod.GET, path, (ctx) -> ctx.resp().respond(Status.OK));
    }
    protected Route.Builder get(final String path, final Route.Handler handler) {
        return _route(HttpMethod.GET, path, handler);
    }
    protected Route.Builder _route(HttpMethod method, final String path, final Route.Handler handler) {
        Route.Builder bob = new Route.Builder(method, path, handler);
        routes.add(bob);
        return bob;
    }
    
    
    protected void install(Server server) {
        //Server service = loader.findFirst().get();
        //registry.register(Server.class, server);
    }
    
    
    public void start() {
        System.out.println("starting");
        long startupTime = System.currentTimeMillis();
        
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        // bootstrap
        //bootstrap.boot(mode, serverConfig, sessionConfig.sessionStore, this::buildRoutes);
        Application moduleConfig = booter.boot(buildRoutes());
        
        
        // find server
        ServiceLoader<Server> loader = ServiceLoader.load(Server.class);
        Optional<Server> server = loader.findFirst();
        if (server.isEmpty()) {
            throw new IllegalStateException("Server not found");
        }
        
        // start server
        ServerConfig serverConfig = Server.ServerConfig.from(null);
        try {
            //registry.require(Server.class).start(serverConfig);
            server.get().start(serverConfig, moduleConfig);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
            return;
        }
        
        //log.info(FrameworkBootstrap.FRAMEWORK_SPLASH);
        log.info("Bootstrap of framework started in: " + (System.currentTimeMillis() - startupTime) + " ms");
        //log.info("Jawn: Environment:                 " + mode.name());
        log.info("Jawn: Running on port:             " + serverConfig.port());
    }
    
    public void stop() {
        System.out.println("STOP");
    }

    
    public static final void run(final String ... args) {
        System.out.println("running");
        // do some command line parsing of the args
        // port=8080 mode=prod
        Class<Jawn> caller = Reflection.callingClass(Jawn.class);
        if (caller == null) {
            log.error("Could not determine a class extending {}, and therefore not able to start a server", Jawn.class);
            return;
        }
        
        try {
            Jawn instance = caller.getDeclaredConstructor().newInstance();
            
            instance.start();
            
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }
    
    private Stream<Route> buildRoutes() {
        return routes.stream().map(bob -> bob.renderer(renderer).build());
    }
}
