package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.Route.RouteHandler;
import net.javapla.jawn.core.internal.FrameworkBootstrap;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.Modes;

public class Jawn {
    
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);
    
    private final FrameworkBootstrap bootstrap;
    private final ArrayList<RouteHandler> routes;
    
    private Modes mode = Modes.DEV;

    public Jawn() {
        bootstrap = new FrameworkBootstrap();
        routes = new ArrayList<>();
    }
    
    // ****************
    // Configuration
    // ****************
    protected Jawn mode(Modes mode) {
        if (mode != null)
            this.mode = mode;
        return this;
    }
    
    protected Jawn use(final ModuleBootstrap module) {
        bootstrap.register(module);
        return this;
    }
    
    
    // ****************
    // Router
    // ****************
    protected Jawn get(final String path, final Result result) {
        return get(path, () -> result);
    }
    protected Jawn get(final String path, final Route.ZeroArgHandler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.GET).path(path).handler(handler).build());
        return this;
    }
    protected Jawn get(final String path, final Route.Handler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.GET).path(path).handler(handler).build());
        return this;
    }
    
    protected Jawn post(final String path, final Route.Handler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.POST).path(path).handler(handler).build());
        return this;
    }
    
    protected Jawn put(final String path, final Route.Handler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.PUT).path(path).handler(handler).build());
        return this;
    }
    
    protected Jawn delete(final String path, final Route.Handler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.DELETE).path(path).handler(handler).build());
        return this;
    }
    
    protected Jawn head(final String path, final Route.Handler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.HEAD).path(path).handler(handler).build());
        return this;
    }
    
    protected Jawn options(final String path, final Route.Handler handler) {
        routes.add((RouteHandler) new Route.Builder(HttpMethod.OPTIONS).path(path).handler(handler).build());
        return this;
    }
    
    
    public void start() {
        long startupTime = System.currentTimeMillis();
        
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        bootstrap.boot(mode, routes);
        
        /*JawnConfigurations properties = new JawnConfigurations(mode);
        bootstrapper.boot(properties, filters, new RouterImpl(builders, filters, properties), databaseConnections);*/
        Injector injector = bootstrap.getInjector();
        try {
            injector.getInstance(Server.class).start(/*serverConfig*/);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
            return;
        }
        
        logger.info(FrameworkBootstrap.FRAMEWORK_SPLASH);
        logger.info("Bootstrap of framework started in: " + (System.currentTimeMillis() - startupTime) + " ms");
        logger.info("Jawn: Environment:                 " + mode);
        logger.info("Jawn: Running on port:             " + 8080);
    }
    
    /**
     * Asynchronously shutdowns the server
     */
    public void stop() {
        CompletableFuture.runAsync(() -> {
            try {
                bootstrap.getInjector().getInstance(Server.class).stop();
            } catch (Exception ignore) {
                // Ignore NPE. At this point the server REALLY should be possible to find
            }
            bootstrap.shutdown();
        });
    }
    
    /**
     * 
     * @param jawn
     *          A subclass of Jawn
     * @param args
     *          <ol>
     *          <li>Server port - Overwrites the default port and the port if it is assigned by {@link ServerConfig#port(int)}</li>
     *          <li>Mode of operation - DEV,TEST,PROD or their fully qualified names: development, test, production. See {@linkplain Modes}. Default is DEV</li>
     *          </ol>
     */
    public static final void run(final Jawn jawn, final String ... args) {
        jawn
        .parseArguments(args) // Read program arguments and overwrite server specifics
        .start();
    }
    public static final void run(final Supplier<Jawn> jawn, final String ... args) {
        run(jawn.get(), args);
    }
    public static final void run(final Class<? extends Jawn> jawn, final String ... args) {
        run(DynamicClassFactory.createInstance(jawn), args);
    }

    //TODO
    private Jawn parseArguments(final String ... args) {
//        if (args.length >= 1)
//            server().port(ConvertUtil.toInteger(args[0], server().port()));
//        if (args.length >= 2)
//            env(Modes.determineModeFromString(args[1]));
            
        return this;
    }
    
    /*private void checkState(boolean expression, String errorMessage) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }*/
}
