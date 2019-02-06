package net.javapla.jawn.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;

import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.internal.FrameworkBootstrap;
import net.javapla.jawn.core.internal.mvc.AssetRouter;
import net.javapla.jawn.core.internal.mvc.MvcRouter;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.internal.reflection.PackageWatcher;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.ConvertUtil;
import net.javapla.jawn.core.util.Modes;

public class Jawn implements Route.Filtering/*<Jawn>*/, Injection {
    
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);
    
    private final FrameworkBootstrap bootstrap;
    //private final LinkedList<Route.Builder> routes;
    private final HashMap<Route.Builder, RouteFilterPopulator> builders;
    private final Assets.Impl assets;
    private final RouteFilterPopulator filters;
    private final HashMap<Class<?>, RouteFilterPopulator> mvcFilters;
    private final ServerConfig.Impl/*ServerConfig*/ serverConfig;
    
    private Modes mode = Modes.DEV;

    public Jawn() {
        bootstrap = new FrameworkBootstrap();
        //routes = new LinkedList<>();
        builders = new HashMap<>();
        assets = new Assets.Impl();
        filters = new RouteFilterPopulator();
        mvcFilters = new HashMap<>();
        serverConfig = new ServerConfig.Impl();
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
     * @return this
     */
    protected Jawn use(final ModuleBootstrap module) {
        bootstrap.register(module);
        return this;
    }
    
    protected ServerConfig server() {
        return serverConfig;
    }
    
    //MVC route classes
    protected Route.Filtering/*<RouteFilterPopulator>*/ mvc(final Class<?> routeClass) {
        //List<Builder> classRoutes = MvcRouter.extract(routeClass);
        //routes.addAll(classRoutes);
        //return new MvcFiltering(classRoutes);
        
        return mvcFilters.computeIfAbsent(routeClass, c -> new RouteFilterPopulator());
    }
    
    protected Assets assets() {
        return assets;
    }
    
    
    // ****************
    // Router
    // ****************
    protected Route.Filtering/*<Route.Builder>*/ get(final String path, final Result result) {
        return get(path, () -> result);
    }
    protected Route.Filtering/*<Route.Builder>*/ get(final String path, final Route.ZeroArgHandler handler) {
        Builder builder = new Route.Builder(HttpMethod.GET).path(path).handler(handler);
        
        //routes.add(builder);
        //return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
    }
    protected Route.Filtering/*<Route.Builder>*/ get(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.GET).path(path).handler(handler);
//        routes.add(builder);
//        return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
    }
    
    protected Route.Filtering/*<Route.Builder>*/ post(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.POST).path(path).handler(handler);
//        routes.add(builder);
//        return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
    }
    
    protected Route.Filtering/*<Route.Builder>*/ put(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.PUT).path(path).handler(handler);
//        routes.add(builder);
//        return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
    }
    
    protected Route.Filtering/*<Route.Builder>*/ delete(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.DELETE).path(path).handler(handler);
//        routes.add(builder);
//        return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
    }
    
    protected Route.Filtering/*<Route.Builder>*/ head(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.HEAD).path(path).handler(handler);
//        routes.add(builder);
//        return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
    }
    
    protected Route.Filtering/*<Route.Builder>*/ options(final String path, final Handler handler) {
        Builder builder = new Route.Builder(HttpMethod.OPTIONS).path(path).handler(handler);
//        routes.add(builder);
//        return builder;
        return builders.computeIfAbsent(builder, c -> new RouteFilterPopulator());
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
    
    protected Jawn before(final Class<?> filter) {
        filters.filter(filter);
        return this;
    }
    
    /** add a global filter */
    @Override
    public Jawn after(final Route.After filter) {
        filters.filter(filter);
        return this;
    }
    
    protected Jawn after(final Class<?> filter) {
        filters.filter(filter);
        return this;
    }
    
    // ****************
    // Injection
    // ****************
    @Override
    public <T> T require(Key<T> key) {
        checkState(bootstrap.getInjector() != null, "App has not started yet");
        return bootstrap.getInjector().getInstance(key);
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
        logger.info("Jawn: With context path:           " + serverConfig.context());
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
            server().port(ConvertUtil.toInteger(args[0], serverConfig.port()));
        if (args.length >= 2)
            mode(Modes.determineModeFromString(args[1]));
            
        return this;
    }
    
    private void checkState(boolean expression, String errorMessage) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }
    
    List<Route> buildRoutes(Injector injector) {
        LinkedList<Route.Builder> routes = new LinkedList<>();
        
        // populate ordinary routes
        builders.entrySet().forEach(entry -> {
            entry.getValue().populate(entry.getKey(), injector);
            routes.add(entry.getKey());
        });
        
        // populate routes from mvc
        mvcFilters.entrySet().forEach(entry -> {
            List<Builder> mvcRoutes = MvcRouter.extract(entry.getKey());
            entry.getValue().populate(mvcRoutes, injector);
            routes.addAll(mvcRoutes);
            System.out.println(mvcRoutes);
        });
        
        // add global filters to the routes
        filters.populate(routes, injector);
        
        // add assets
        routes.addAll(AssetRouter.assets(injector.getInstance(DeploymentInfo.class), assets));
        
        return routes.stream().map(Route.Builder::build).collect(Collectors.toList());
    }
    
    /*public static final class MvcFiltering implements Route.Filtering<MvcFiltering> {
        
        private final List<Builder> classRoutes;

        MvcFiltering(final List<Route.Builder> routes) {
            classRoutes = routes;
        }

        @Override
        public MvcFiltering filter(Filter filter) {
            classRoutes.forEach(route -> route.filter(filter));
            return this;
        }

        @Override
        public MvcFiltering before(Before handler) {
            classRoutes.forEach(route -> route.before(handler));
            return this;
        }

        @Override
        public MvcFiltering after(After handler) {
            classRoutes.forEach(route -> route.after(handler));
            return this;
        }
        
    }*/
}
