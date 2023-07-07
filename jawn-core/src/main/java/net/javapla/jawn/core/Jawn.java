package net.javapla.jawn.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import net.javapla.jawn.core.Plugin.Application;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.Server.ServerConfig;
import net.javapla.jawn.core.internal.Bootstrapper;
import net.javapla.jawn.core.internal.mvc.MvcCompiler;
import net.javapla.jawn.core.internal.reflection.ClassLocator;
import net.javapla.jawn.core.internal.reflection.Reflection;
import net.javapla.jawn.core.util.StringUtil;

public class Jawn {
    
    protected static final Logger log = LoggerFactory.getLogger(Jawn.class);
    static final Logger SYS_ERROR_LOG = LoggerFactory.getLogger("JawnError");
    
    
    private final Bootstrapper booter = new Bootstrapper(); // Core?
    private final LinkedList<Route.Builder> routes = new LinkedList<>();
    private final LinkedList<Object> mvcControllers = new LinkedList<>();
    private final ServerConfig serverConfig = new ServerConfig();
    
    
    
    public Jawn() {
        
    }
    
    /*protected Route.RouteBuilder get(final String path) {
        return _route(HttpMethod.GET, path, (ctx) -> ctx.resp().respond(Status.OK));
    }*/
    protected Route.RouteBuilder get(final String path, final Route.Handler handler) {
        return _route(HttpMethod.GET, path, handler);
    }
    /*protected Route.RouteBuilder get(final String path, final Route.NoResultHandler handler) {
        return _route(HttpMethod.GET, path, handler);
    }*/
    protected Route.RouteBuilder get(final String path, final Route.ZeroArgHandler handler) {
        return _route(HttpMethod.GET, path, handler);
    }
    protected Route.RouteBuilder get(final String path, Object result) {
        return _route(HttpMethod.GET, path, (ctx) -> result).returnType(result.getClass());
    }
    protected Route.RouteBuilder head(final String path, final Route.Handler handler) {
        return _route(HttpMethod.HEAD, path, handler);
    }
    protected Route.RouteBuilder post(final String path, final Route.Handler handler) {
        return _route(HttpMethod.POST, path, handler);
    }
    /*protected Route.RouteBuilder post(final String path, final Route.NoResultHandler handler) {
        return _route(HttpMethod.POST, path, handler);
    }*/
    protected Route.RouteBuilder put(final String path, final Route.Handler handler) {
        return _route(HttpMethod.PUT, path, handler);
    }
    protected Route.RouteBuilder put(final String path, final Route.ZeroArgHandler handler) {
        return _route(HttpMethod.PUT, path, handler);
    }
    protected Route.Builder _route(HttpMethod method, final String path, final Route.Handler handler) {
        Route.Builder bob = new Route.Builder(method, _pathPrefix(path), handler);
        routes.add(bob);
        return bob;
    }
    
    protected Jawn routes(Runnable routes) {
        routes.run();
        return this;
    }
    
    /**
     * path("/api/v1/", () -> {
     *   get("/{id}", ctx -> ... );
     *   get("/", ctx -> ... );
     *   post("/", ctx -> ... );
     * });
     * @return
     */
    protected Jawn path(final String rootPath, final Runnable routes) {
        pathPrefix.addLast(rootPath);
        routes(routes);
        pathPrefix.removeLast();
        return this;
    }
    private final LinkedList<String> pathPrefix = new LinkedList<>();
    private String _pathPrefix(String path) {
        return pathPrefix.stream().collect(Collectors.joining("","",path));
    }
    
    
    protected Route.Builder ws(final String path, WebSocket.Initialiser initialiser) {
        // Only GET is supported to start the handshake
        return _route(HttpMethod.GET, path, new WebSocket.WebSocketHandler(initialiser)).returnType(Context.class);
    }
    
    
/* ******************************************
 * MVC
 * ****************************************** */
    protected /*Route.RouteBuilder*/void controller(final Class<?> controller) {
        // TODO
        //List<Builder> list = MvcCompiler.compile(controller, booter.registry());
        //routes.addAll(list);
        mvcControllers.add(controller);
    }
    protected void controller(Object controller) {
        mvcControllers.add(controller);
    }
    protected void controllers(String packageToScan) {
        ClassLocator.list(packageToScan, booter.classLoader()).forEach(this::controller);
    }
    protected void controllers(Package packageToScan) {
        controllers(packageToScan.getName());
    }
    /**
     * Look for MVC controllers at the standard package by convention.
     * <p>
     * I.e.: The 'controllers' package next to this implementor of {@link Jawn}
     * 
     * <p>
     * This is just a short-hand for <code>controllers(this.getClass().getPackageName() + ".controllers");</code>
     */
    protected void controllers() {
        controllers(this.getClass().getPackageName() + ".controllers");
    }
    
    
    protected void install(Plugin plugin) {
        //Server service = loader.findFirst().get();
        //registry.register(Server.class, server);
        // TODO
        
        booter.install(plugin);
    }
    
    protected ServerConfig server() {
        return serverConfig;
    }
    
/* ******************************************
 * Registry
 * ****************************************** */
    protected <T> T require(Class<T> type) {
        return booter.registry().require(type);
    }
    
    // TODO
    /*protected void register(Object type) {
        
    }*/
    
    
/* ******************************************
 * Life cycle
 * ****************************************** */
    protected Jawn onStartup(Runnable task) {
        booter.onStartup(task);
        return this;
    }

    
    
    public void start() {
        long startupTime = System.currentTimeMillis();
        
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        
        // find server
        ServiceLoader<Server> loader = ServiceLoader.load(Server.class);
        Optional<Server> server = loader.findFirst();
        if (server.isEmpty()) {
            throw new IllegalStateException("Server not found");
        }
        
        
        // bootstrap
        //bootstrap.boot(mode, serverConfig, sessionConfig.sessionStore, this::buildRoutes);
        Application moduleConfig = booter.boot(this::buildRoutes);
        
        // TODO ServiceLoader.load Registries after everything else is loaded 
        
        // start server
        Config config = booter.config().hasPath("server") ? booter.config().getConfig("server") : null;
        serverConfig.config(config);
        try {
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
        // TODO
        // do some command line parsing of the args
        // port=8080 mode=prod
        Class<Jawn> caller = Reflection.callingClass(Jawn.class);
        if (caller == null) {
            log.error("Could not determine a class extending {}, and therefore not able to start a server", Jawn.class);
            return;
        }
        
        try {
            /*Jawn instance = null;
            if (args.length > 0) {
                try {
                    instance = caller.getDeclaredConstructor(String[].class).newInstance(args);
                } catch (NoSuchMethodException nothing) {}
            }
            
            if (instance == null)
                instance = caller.getDeclaredConstructor().newInstance();*/
            Jawn instance = caller.getDeclaredConstructor().newInstance();
            
            instance.start();
            
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            log.error("", e);
        }
    }
    
    static Map<String,String> commandLineParsing(String ... args) {
        if (args == null || args.length == 0) return Collections.emptyMap();
        
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        StringUtil.split(args, '=', map::put);
        
        return map;
    }
    
    private Stream<Route.Builder> buildRoutes(Registry registry) {
        mvcControllers.forEach(controller -> {
            if (controller instanceof Class<?>) {
                List<Builder> list = MvcCompiler.compile((Class<?>)controller, registry);
                routes.addAll(list);
            } else {
                List<Builder> list = MvcCompiler.compile(controller.getClass(), () -> controller, registry);
                routes.addAll(list);
            }
        });
        
        return routes.stream();
    }
}
