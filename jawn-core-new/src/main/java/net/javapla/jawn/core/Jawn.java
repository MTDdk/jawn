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
import net.javapla.jawn.core.internal.FrameworkBootstrap;
import net.javapla.jawn.core.internal.mvc.MvcRouter;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.internal.reflection.PackageWatcher;
import net.javapla.jawn.core.internal.reflection.ReflectionMetadata;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.ConvertUtil;
import net.javapla.jawn.core.util.Modes;

public class Jawn implements Route.Filtering<Jawn> {
    
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);
    
    private final FrameworkBootstrap bootstrap;
    private final LinkedList<Route.Builder> routes;
    private final RouteFilterPopulator filters;
    private final ServerConfig serverConfig;
    
    private Modes mode = Modes.DEV;

    public Jawn() {
        bootstrap = new FrameworkBootstrap();
        routes = new LinkedList<>();
        filters = new RouteFilterPopulator();
        serverConfig = new ServerConfig();
    }
    
    // ****************
    // Configuration
    // ****************
    protected Jawn mode(Modes mode) {
        if (mode != null)
            this.mode = mode;
        return this;
    }
    
    /**
     * <pre>
     * use((app) -> {
     *   app.binder().bind(RendererEngine.class).toInstance(new RendererEngine() {
     *     public void invoke(Context context, Object renderable) throws Exception {
     *       // code goes here
     *       // Ex.:
     *       if (renderable instanceof String) {
     *         context.resp().send(((String) renderable).getBytes(context.req().charset()));
     *       }
     *     }
     * 
     *     public MediaType[] getContentType() {
     *       return new MediaType[] { MediaType.valueOf("text/plain") };
     *     }
     *   });
     * });
     * </pre>
     * @param module
     * @return
     */
    protected Jawn use(final ModuleBootstrap module) {
        bootstrap.register(module);
        return this;
    }
    
    protected ServerConfig server() {
        return serverConfig;
    }
    
    //MVC route classes
    protected Jawn use(final Class<?> routeClass) {
        routes.addAll(MvcRouter.extract(routeClass));
        return this;
    }
    
    
    // ****************
    // Router
    // ****************
    protected Route.Filtering<Route.Builder> get(final String path, final Result result) {
        return get(path, () -> result);
    }
    protected Route.Filtering<Route.Builder> get(final String path, final Route.ZeroArgHandler handler) {
        Builder builder = new Route.Builder(HttpMethod.GET).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    protected Route.Filtering<Route.Builder> get(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.GET).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering<Route.Builder> post(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.POST).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering<Route.Builder> put(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.PUT).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering<Route.Builder> delete(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.DELETE).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering<Route.Builder> head(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.HEAD).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    protected Route.Filtering<Route.Builder> options(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.OPTIONS).path(path).handler(handler);
        routes.add(builder);
        return builder;
    }
    
    // ****************
    // Filters
    // ****************
    /** add a global filter */
    @Override
    public Jawn filter(final Route.Filter filter) {
        filters.filter(filter);
        return this;
    }
    
    /** add a global filter - can implement {@link Route.After} or {@link Route.Before} or {@link Route.Filter} */
    protected Jawn filter(final Class<?> filter) {
        filters.filter(filter);
        return this;
    }
    
    /** add a global filter */
    @Override
    public Jawn before(final Route.Before filter) {
        filters.filter(filter);
        return this;
    }
    
    /** add a global filter */
    @Override
    public Jawn after(final Route.After filter) {
        filters.filter(filter);
        return this;
    }
    
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
        
        // bootstrap
        bootstrap.boot(mode, serverConfig, this::buildRoutes);
        
        // start server
        try {
            Injector injector = bootstrap.getInjector();
            injector.getInstance(Server.class).start(serverConfig);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
            return;
        }
        
        logger.info(FrameworkBootstrap.FRAMEWORK_SPLASH);
        logger.info("Bootstrap of framework started in: " + (System.currentTimeMillis() - startupTime) + " ms");
        logger.info("Jawn: Environment:                 " + mode);
        logger.info("Jawn: Running on port:             " + serverConfig.port());
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
     * @param jawn
     *          A subclass of Jawn
     * @param args
     *          <ol>
     *          <li>Server port - Overwrites the default port and the port if it is assigned by {@link ServerConfig#port(int)}</li>
     *          <li>Mode of operation - DEV,TEST,PROD or their fully qualified names: development, test, production. See {@linkplain Modes}. Default is DEV</li>
     *          </ol>
     */
    public static final void run(final Class<? extends Jawn> jawn, final String ... args) {
        Jawn instance = DynamicClassFactory.createInstance(jawn);
        System.out.println(jawn.getPackageName());
        
        if (instance.mode == Modes.DEV) {
            // load the instance with a non-caching classloader
            final Jawn dynamicInstance = DynamicClassFactory
                .createInstance(
                    DynamicClassFactory.getCompiledClass(jawn.getName(), false), 
                    Jawn.class
                );
            
            // look for changes to reload
            final Consumer<Jawn> reloader = (newJawnInstance) -> dynamicInstance.bootstrap.reboot___strap(newJawnInstance::buildRoutes);
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
    
    private static final void run(final Jawn jawn, final String ... args) {
        jawn
        .parseArguments(args) // Read program arguments and overwrite server specifics
        .start();
    }

    private Jawn parseArguments(final String ... args) {
        if (args.length >= 1)
            server().port(ConvertUtil.toInteger(args[0], server().port()));
        if (args.length >= 2)
            mode(Modes.determineModeFromString(args[1]));
            
        return this;
    }
    
    /*private void checkState(boolean expression, String errorMessage) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }*/
    
    List<Route> buildRoutes(Injector injector) {
        filters.populate(routes, injector);
        return routes.stream().map(Route.Builder::build).collect(Collectors.toList());
    }
    
    static final class RouteFilterPopulator {
        private final LinkedList<Object> bagOFilters;
        
        RouteFilterPopulator() {
            bagOFilters = new LinkedList<>();
        }
        
        void filter(final Route.Filter f) {
            bagOFilters.add(f);
        }
        
        void filter(final Route.Before f) {
            bagOFilters.add(f);
        }
        
        void filter(final Route.After f) {
            bagOFilters.add(f);
        }
        
        void filter(final Class<?> f) {
            bagOFilters.add(f);
        }
        
        /**
         * Add global filters to all routes
         * The notion of route specific filters, is, that they are the innermost, and
         * global filters are wrapping around them
         * 
         * Should add the filters in a layered manner, and in the order
         * they are written in the code
         * 
         * Example:
         * jawn.filter(filter1);
         * jawn.filter(filter2);
         * 
         * Results in following execution order:
         * filter1.before -> filter2.before -> execute handler -> filter2.after -> filter1.after
         * 
         * Example2:
         * jawn.get("/",work).before(beforeFilter).after(afterFilter);
         * jawn.filter(filter1);
         * jawn.filter(filter2);
         * 
         * Execution order:
         * filter1.before -> filter2.before -> beforeFilter -> execute handler -> afterFilter -> filter2.after -> filter1.after
         * 
         * @param routes
         */
        void populate(final List<Route.Builder> routes, final Injector injector) {
            bagOFilters.forEach(item -> {
                if (item instanceof Route.Filter) { //filter is instanceof Before and After, so this has to be first
                    filter(routes, item);
                } else if (item instanceof Route.After) {
                    after(routes, item);
                } else if (item instanceof Route.Before) {
                    before(routes, item);
                } else if (item instanceof Class<?>) {
                    Class<?> d = (Class<?>)item;
                    
                    if (ReflectionMetadata.isAssignableFrom(d, Route.Filter.class)) {
                        filter(routes, injector.getInstance(d));
                    } else if (ReflectionMetadata.isAssignableFrom(d, Route.After.class)) {
                        after(routes, injector.getInstance(d));
                    } else if (ReflectionMetadata.isAssignableFrom(d, Route.Before.class)) {
                        before(routes, injector.getInstance(d));
                    }
                }
            });
        }
        
        private void before(final List<Route.Builder> routes, Object item) {
            routes.forEach(r -> r.globalBefore((Route.Before) item));
        }
        
        private void after(final List<Route.Builder> routes, Object item) {
            routes.forEach(r -> r.globalAfter((Route.After) item));
        }
        
        private void filter(final List<Route.Builder> routes, Object item) {
            routes.forEach(r -> r.globalFilter((Route.Filter) item));
        }
    }
}
