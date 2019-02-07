package net.javapla.jawn.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;

import net.javapla.jawn.core.internal.FrameworkBootstrap;
import net.javapla.jawn.core.internal.mvc.AssetRouter;
import net.javapla.jawn.core.internal.mvc.MvcRouter;
import net.javapla.jawn.core.internal.reflection.ClassFactory;
import net.javapla.jawn.core.internal.reflection.ClassLocator;
import net.javapla.jawn.core.internal.reflection.PackageWatcher;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.ConvertUtil;
import net.javapla.jawn.core.util.Modes;

public class Jawn implements Route.Filtering, Injection {
    
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);
    
    private final FrameworkBootstrap bootstrap;
    //private final HashMap<Route.Builder, RouteFilterPopulator> routesAndFilters;
    private final LinkedList<SingleRouteFiltering> routesAndFilters;
    private final RouteFilterPopulator globalFilters;
    private final Assets.Impl assets;
    //private final HashMap<Class<?>, RouteFilterPopulator> mvcFilters;
    //private final HashMap<String, Pair<Class<?>,RouteFilterPopulator>> mvcFilters;
    private final HashMap<String, ControllerFiltering> mvcFilters;
    private final ServerConfig.Impl serverConfig;
    
    private Modes mode = Modes.DEV;

    public Jawn() {
        bootstrap = new FrameworkBootstrap();
        //routesAndFilters = new HashMap<>();
        routesAndFilters = new LinkedList<>();
        globalFilters = new RouteFilterPopulator();
        assets = new Assets.Impl();
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
    protected Route.Filtering mvc(final Class<?> routeClass) {
        //return mvcFilters.computeIfAbsent(routeClass, c -> new RouteFilterPopulator());
        //return mvcFilters.computeIfAbsent(routeClass.getName(), s -> new Pair<Class<?>, RouteFilterPopulator>(routeClass, new RouteFilterPopulator())).two;
        return mvcFilters.computeIfAbsent(routeClass.getName(), s -> new ControllerFiltering(routeClass));
    }
    /*private class Pair<C,F> {
        final C one;
        final F two;
        Pair(C one, F two) {
            this.one = one;
            this.two = two;
        }
        Pair<C,F> clone(C one) {
            return new Pair<C,F>(one, this.two);
        }
    }*/
    
    /**
     * Look for MVC controllers within this package.
     * @param path
     */
    protected void controllers(final String packagePath) {
        ClassLocator locator = new ClassLocator(packagePath);
        locator.foundClasses().stream().forEach(this::mvc);
    }
    
    protected void controllers(final Package path) {
        controllers(path.getName());
    }
    
    protected Assets assets() {
        return assets;
    }
    
    
    // ****************
    // Router
    // ****************
    protected Route.Filtering get(final String path, final Handler handler) {
        return _get(path, handler);
    }
    protected Route.Filtering get(final String path, final Route.ZeroArgHandler handler) {
        return _get(path, handler);
    }
    protected Route.Filtering get(final String path, final Result result) {
        return get(path, () -> result);
    }
    private Route.Filtering _get(final String path,  final Handler handler) {
        return _addRoute(HttpMethod.GET, path, handler);
    }
    
    protected Route.Filtering post(final String path, final Result result) {
        return post(path, () -> result);
    }
    protected Route.Filtering post(final String path, final Route.ZeroArgHandler handler) {
        return _post(path, handler);
    }
    protected Route.Filtering post(final String path, final Handler handler) {
        return _post(path, handler);
    }
    private Route.Filtering _post(final String path,  final Handler handler) {
        return _addRoute(HttpMethod.POST, path, handler);
    }
    
    protected Route.Filtering put(final String path, final Handler handler) {
        return _put(path, handler);
    }
    protected Route.Filtering put(final String path, final Route.ZeroArgHandler handler) {
        return _put(path, handler);
    }
    protected Route.Filtering put(final String path, final Result result) {
        return put(path, () -> result);
    }
    private Route.Filtering _put(final String path, final Handler handler) {
        return _addRoute(HttpMethod.PUT, path, handler);
    }
    
    protected Route.Filtering delete(final String path, final Handler handler) {
        return _delete(path, handler);
    }
    protected Route.Filtering delete(final String path, final Route.ZeroArgHandler handler) {
        return _delete(path, handler);
    }
    protected Route.Filtering delete(final String path, final Result result) {
        return delete(path, () -> result);
    }
    private Route.Filtering _delete(final String path, final Handler handler) {
        return _addRoute(HttpMethod.DELETE, path, handler);
    }
    
    protected Route.Filtering head(final String path, final Handler handler) {
        return _head(path, handler);
    }
    protected Route.Filtering head(final String path, final Route.ZeroArgHandler handler) {
        return _head(path, handler);
    }
    protected Route.Filtering head(final String path, final Result result) {
        return head(path, () -> result);
    }
    private Route.Filtering _head(final String path, final Handler handler) {
        return _addRoute(HttpMethod.HEAD, path, handler);
    }
    
    protected Route.Filtering options(final String path, final Handler handler) {
        return _options(path, handler);
    }
    protected Route.Filtering options(final String path, final Route.ZeroArgHandler handler) {
        return _options(path, handler);
    }
    protected Route.Filtering options(final String path, final Result result) {
        return options(path, () -> result);
    }
    private Route.Filtering _options(final String path, final Handler handler) {
        return _addRoute(HttpMethod.OPTIONS, path, handler);
    }
    
    private Route.Filtering _addRoute(HttpMethod method, String path, Handler handler) {
        //return routesAndFilters.computeIfAbsent(new Route.Builder(method).path(path).handler(handler), c -> new RouteFilterPopulator());
        SingleRouteFiltering filtering = new SingleRouteFiltering(new Route.Builder(method).path(path).handler(handler));
        routesAndFilters.add(filtering);
        return filtering;
    }
    
    // ****************
    // Filters
    // ****************
    /** add a global filter */
    @Override
    public Jawn filter(final Route.Filter filter) {
        globalFilters.filter(filter);
        return this;
    }
    
    /** add a global filter - can implement {@link Route.After} or {@link Route.Before} or {@link Route.Filter} */
    @Override
    public Jawn filter(final Class<?> filter) {
        globalFilters.filter(filter);
        return this;
    }
    
    /** add a global filter */
    @Override
    public Jawn before(final Route.Before filter) {
        globalFilters.before(filter);
        return this;
    }
    
    /** add a global filter */
    @Override
    public Jawn after(final Route.After filter) {
        globalFilters.after(filter);
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
        if (!serverConfig.context().isEmpty())
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
        Jawn instance = ClassFactory.createInstance(jawn);
        
        if (instance.mode == Modes.DEV) {
            // load the instance with a non-caching classloader
            final Jawn dynamicInstance = ClassFactory
                .createInstance(
                    ClassFactory.getCompiledClass(jawn.getName(), false), 
                    Jawn.class
                );
            
            // look for changes to reload
            final BiConsumer<Jawn, Class<?>> reloader = (newJawnInstance, reloadedClass) -> {
                
                // Reload the observed class.
                // Only classes explicitly stated within the Jawn-instance
                // get automatically reloaded by the same DynamicClassLoader as Jawn when in DEV.
                // It is known that if a class is explicit stated in the Jawn-instance
                // AND is a part of a controller package, the class will be reloaded multiple times..
                // I guess we will have to live with this
                if (reloadedClass != null && newJawnInstance.mvcFilters.containsKey(reloadedClass.getName())) {
                    ControllerFiltering filtering = newJawnInstance.mvcFilters.get(reloadedClass.getName());
                    filtering.replace(reloadedClass);
                }
                dynamicInstance.bootstrap.reboot___strap(newJawnInstance::buildRoutes);
            };
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
        /*routesAndFilters.entrySet().forEach(entry -> {
            routes.add(entry.getValue().populate(entry.getKey(), injector));
        });*/
        routesAndFilters.forEach(popu -> routes.add(popu.populate(injector)));
        
        // populate routes from mvc
//        mvcFilters.entrySet().forEach(entry -> {
//            Class<?> cl = entry.getKey();
//            routes.addAll(entry.getValue().populateAsGlobals(MvcRouter.extract(cl), injector));
//        });
        mvcFilters.values().forEach(pair -> {
            //routes.addAll(pair.two.populateAsGlobals(MvcRouter.extract(pair.one), injector));
            routes.addAll(pair.populate(injector));
        });
        
        // add global filters to the routes
        globalFilters.globals(routes, injector);
        
        // add assets (without the filters)
        routes.addAll(AssetRouter.assets(injector.getInstance(DeploymentInfo.class), assets));
        
        // build
        return routes.stream().map(Route.Builder::build).collect(Collectors.toList());
    }
    
    static class SingleRouteFiltering extends RouteFilterPopulator {
        final Route.Builder route;
        SingleRouteFiltering(Route.Builder route) {
            this.route = route;
        }
        
        /**
         * Populate for a single route
         * 
         * @param injector To instantiate filters that have been added as classes
         */
        Route.Builder populate(final Injector injector) {
            return populate(route, injector);
        }
    }
    
    static class ControllerFiltering extends RouteFilterPopulator {
        Class<?> controller;
        
        ControllerFiltering(final Class<?> controller) {
            this.controller = controller;
        }
        
        void replace(Class<?> c) {
            controller = c;
        }
        
        List<Route.Builder> populate(final Injector injector) {
            List<Route.Builder> list = MvcRouter.extract(controller);
            for (Route.Builder builder : list) {
                populate(builder, injector);
            }
            return list;
        }
    }
    
    static class RouteFilterPopulator implements Route.Filtering {
        protected final LinkedList<Object> bagOFilters;
        
        RouteFilterPopulator() {
            bagOFilters = new LinkedList<>();
        }
        
        @Override
        public RouteFilterPopulator filter(final Class<?> f) {
            bagOFilters.add(f);
            return this;
        }
        
        @Override
        public RouteFilterPopulator filter(final Route.Filter filter) {
            bagOFilters.add(filter);
            return this;
        }

        @Override
        public RouteFilterPopulator before(final Route.Before handler) {
            bagOFilters.add(handler);
            return this;
        }

        @Override
        public RouteFilterPopulator after(final Route.After handler) {
            bagOFilters.add(handler);
            return this;
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
         * @param injector To instantiate filters that have been added as classes
         * @return the <code>routes</code>
         */
        List<Route.Builder> globals(final List<Route.Builder> routes, final Injector injector) {
            bagOFilters.forEach(item -> {
                /*if (item instanceof Route.Filter) { //filter is instanceof Before and After, so this has to be first
                    filter(routes, item);
                } else if (item instanceof Route.After) {
                    after(routes, item);
                } else if (item instanceof Route.Before) {
                    before(routes, item);
                } else */if (item instanceof Class<?>) {
                    Class<?> d = (Class<?>)item;
                    
                    /*if (ReflectionMetadata.isAssignableFrom(d, Route.Filter.class)) {
                        filter(routes, injector.getInstance(d));
                    } else if (ReflectionMetadata.isAssignableFrom(d, Route.After.class)) {
                        after(routes, injector.getInstance(d));
                    } else if (ReflectionMetadata.isAssignableFrom(d, Route.Before.class)) {
                        before(routes, injector.getInstance(d));
                    }*/
                    Object g = injector.getInstance(d);
                    routes.forEach(r -> r.g(g));
                    
                } else {
                    routes.forEach(r -> r.g(item));
                }
            });
            
            return routes;
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
        
        /**
         * Populate for a single route
         * 
         * @param injector To instantiate filters that have been added as classes
         */
        Route.Builder populate(final Route.Builder route, final Injector injector) {
            bagOFilters.forEach(item -> {
                /*if (item instanceof Route.Filter) { //filter is instanceof Before and After, so this has to be first
                    route.filter((Route.Filter) item);
                } else if (item instanceof Route.After) {
                    route.after((Route.After) item);
                } else if (item instanceof Route.Before) {
                    route.before((Route.Before) item);
                } else*/ if (item instanceof Class<?>) {
                    Class<?> d = (Class<?>)item;
                    
                    /*if (ReflectionMetadata.isAssignableFrom(d, Route.Filter.class)) {
                        route.filter((Route.Filter) injector.getInstance(d));
                    } else if (ReflectionMetadata.isAssignableFrom(d, Route.After.class)) {
                        route.after((Route.After) injector.getInstance(d));
                    } else if (ReflectionMetadata.isAssignableFrom(d, Route.Before.class)) {
                        route.before((Route.Before) injector.getInstance(d));
                    }*/
                    route.f(injector.getInstance(d));
                } else {
                    route.f(item);
                }
                
            });
            
            return route;
        }
        
    }
    
}
