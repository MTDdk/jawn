package net.javapla.jawn.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.Route.RouteHandler;
import net.javapla.jawn.core.internal.FrameworkBootstrap;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.internal.reflection.PackageWatcher;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.Modes;

public class Jawn {
    
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);
    
    private final FrameworkBootstrap bootstrap;
    private final LinkedList<Route.Builder> routes;
    private final LinkedList<Route.Filter> filters;
    private final LinkedList<Route.Before> beforeFilters;
    private final LinkedList<Route.After> afterFilters;
    
    private Modes mode = Modes.DEV;

    public Jawn() {
        bootstrap = new FrameworkBootstrap();
        routes = new LinkedList<>();
        filters = new LinkedList<>();
        beforeFilters = new LinkedList<>();
        afterFilters = new LinkedList<>();
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
    protected Route.Filtering get(final String path, final Result result) {
        return get(path, () -> result);
    }
    protected Route.Filtering get(final String path, final Route.ZeroArgHandler handler) {
        Builder builder = new Route.Builder(HttpMethod.GET).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    protected Route.Filtering get(final String path, final Route.Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.GET).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering post(final String path, final Route.Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.POST).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering put(final String path, final Route.Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.PUT).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering delete(final String path, final Route.Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.DELETE).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering head(final String path, final Route.Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.HEAD).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering options(final String path, final Route.Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.OPTIONS).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    // ****************
    // Filters
    // ****************
    /** add a global filter */
    protected Jawn filter(final Route.Filter filter) {
        filters.add(filter);
        return this;
    }
    /*protected Jawn filter(final Class<? extends Route.Filter> filter) {
        filters.add(filter);
        return this;
    }*/
    
    /** add a global filter */
    protected Jawn before(final Route.Before filter) {
        beforeFilters.add(filter);
        return this;
    }
    
    /** add a global filter */
    protected Jawn after(final Route.After filter) {
        afterFilters.add(filter);
        return this;
    }
    
    /*protected Jawn filter(final String path, final Class<? extends Route.Chain> filter) {
     * this does not quite make sense.. use a proper handler instead
        filters.add(filter);
        return this;
    }*/
    
    
    // ****************
    // Life Cycle
    // ****************
    protected Jawn onStartup(final Runnable task) {
        bootstrap.onStartup(task);
        return this;
    }
    
    protected Jawn onShutdown(final Runnable task) {
        bootstrap.onShutdown(task);
        return this;
    }
    
    public void start() {
        long startupTime = System.currentTimeMillis();
        
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        bootstrap.boot(mode, routePopulator());
        
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
    private static final void run(final Jawn jawn, final String ... args) {
        //jawn.getClass().getPackageName()
        // TODO use this information as application.base_package if not specified
        // if jawn.properties has application.base_package then that takes precendence
        
        //TODO when in DEV: start a WatchService for all classes within application.base_package.
        // Whenever a .java file changes, recompile it and put into play (if possible),
        // or just always recompile the Jawn-instance (which hopefully will trigger the
        // usage of the newly recompiled class)
        // ...
        // this might need to be done by creating the entire Jawn-instance in a new ClassLoader
        // that we control, which should delegate to this main ClassLoader whenever the wanted class
        // is not within application.base_package
        
        
        jawn
            .parseArguments(args) // Read program arguments and overwrite server specifics
            .start();
    }
    /*private static final void run(final Supplier<Jawn> jawn, final String ... args) {
        run(jawn.get(), args);
    }*/
    public static final void run(final Class<? extends Jawn> jawn, final String ... args) {
        Jawn instance = DynamicClassFactory.createInstance(jawn);
        
        if (instance.mode == Modes.DEV) {
            // load the instance with a non-caching classloader
            final Jawn dynamicInstance = DynamicClassFactory
                .createInstance(
                    DynamicClassFactory.getCompiledClass(jawn.getName(), false), 
                    Jawn.class
                );
            
            // look for changes to reload
            final Consumer<Jawn> reloader = (newJawnInstance) -> dynamicInstance.bootstrap.reboot___strap(newJawnInstance.routePopulator());
            PackageWatcher watcher = new PackageWatcher(jawn, reloader);
            
            // start the watcher
            try {
                watcher.start();
            } catch (IOException | InterruptedException e) {
                logger.error("Starting " + PackageWatcher.class, e);
            }
            
            // clean up when shutting the whole thing down
            dynamicInstance.onShutdown(() -> {
                try {
                    watcher.close();
                } catch (IOException e) {
                    logger.error("Closing " + PackageWatcher.class, e);
                }
            });
            
            instance = dynamicInstance;
        }
        
        run(instance, args);
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
    
    private List<RouteHandler> routePopulator() {
        // add global filters to all routes
        // currently, they are added AFTER already added filters
        // README create populator class for this behaviour
        filters.forEach(f -> routes.forEach(r -> r.filter(f)));
        beforeFilters.forEach(f -> routes.forEach(r -> r.before(f)));
        afterFilters.forEach(f -> routes.forEach(r -> r.after(f)));
        
        return routes.stream().map(Route.Builder::build).collect(Collectors.toList());
    }
}
