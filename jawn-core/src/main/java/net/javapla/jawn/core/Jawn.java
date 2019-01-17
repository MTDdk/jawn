package net.javapla.jawn.core;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;

import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.database.DatabaseConnections;
import net.javapla.jawn.core.database.DatabaseConnections.DatabaseConnectionBuilder;
import net.javapla.jawn.core.routes.Route.ResponseFunction;
import net.javapla.jawn.core.routes.Route.VoidResponseFunction;
import net.javapla.jawn.core.routes.Route.ZeroArgResponseFunction;
import net.javapla.jawn.core.routes.RouteBuilder;
import net.javapla.jawn.core.routes.RouterImpl;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.ConvertUtil;
import net.javapla.jawn.core.util.Modes;


public class Jawn {
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);

//    private final JawnConfigurations properties;
//    private final DeploymentInfo deploymentInfo;
    private final FrameworkBootstrap bootstrapper;
    
    private final FiltersHandler filters;
    private final ArrayList<RouteBuilder> builders;
    private final DatabaseConnections databaseConnections;
    //private final Router router;
    
    private final ServerConfig serverConfig = new ServerConfig();
    
    private Modes mode = Modes.determineModeFromSystem();
    
    
    public Jawn() {
//        properties = new JawnConfigurations(Modes.determineModeFromSystem());
//        deploymentInfo = new DeploymentInfo(properties);
        
        
        filters = new FiltersHandler();
        builders = new ArrayList<>();
        databaseConnections = new DatabaseConnections();
        //router = new RouterImpl(filters, properties);
        
        bootstrapper = new FrameworkBootstrap(/*properties, deploymentInfo*//*, router*/);
    }
    
    /*public Jawn(final String contextPath) {
        
    }*/
    
    /**
     * Run the tasks as a part of the start up process.
     * 
     * With the current implementation, the Runnables will be executed
     * just before starting the server.
     * 
     * @param callback
     * @return this, for chaining
     */
    public Jawn onStartup(Runnable callback) {
        Objects.requireNonNull(callback);
        bootstrapper.onStartup(callback);
        return this;
    }
    
    /**
     * Run the tasks as a part of the shut down process.
     * 
     * With the current implementation, the Runnables will be executed
     * right after stopping the server, but before closing any
     * connection pool to a {@link DatabaseConnection}.
     * 
     * @param callback
     * @return this, for chaining
     */
    public Jawn onShutdown(Runnable callback) {
        Objects.requireNonNull(callback);
        bootstrapper.onShutdown(callback);
        return this;
    }
    
    public Jawn env(Modes mode) {
        Objects.requireNonNull(mode);
        //TODO clearly, this needs to be changed to not set a property like this
        System.setProperty("JAWN_ENV", mode.toString()); 
        //properties.set(mode);
        this.mode = mode;
        return this;
    }
    
    public Jawn use(AbstractModule module) {
        bootstrapper.config().registerModules(module);
        return this;
    }
    
    public Jawn encoding(String encoding) {
        bootstrapper.config().setCharacterEncoding(encoding);
        return this;
    }
    public Jawn encoding(Charset encoding) {
        bootstrapper.config().setCharacterEncoding(encoding.displayName());
        return this;
    }
    
    /*public Jawn contextPath(String contextPath) {
        bootstrapper.config().setContextPath(contextPath);
        return this;
    }*/
    
    public DatabaseConnectionBuilder database(Modes mode) {
        return databaseConnections.environment(mode);
    }

    // ****************
    // GET
    // ****************
    public Jawn get(String path, Result response) {
        builders.add(RouteBuilder.get().route(path).with(response));
        return this;
    }
    public Jawn get(String path, ZeroArgResponseFunction func) {
        builders.add(RouteBuilder.get().route(path).with(func));
        return this;
    }
    public Jawn get(String path, VoidResponseFunction func) {
        builders.add(RouteBuilder.get().route(path).with(func));
        return this;
    }
    public Jawn get(String path, ResponseFunction func) {
        builders.add(RouteBuilder.get().route(path).with(func));
        return this;
    }
    public Jawn get(String path, Class<? extends Controller> controller) {
        builders.add(RouteBuilder.get().route(path).to(controller));
        return this;
    }
    public Jawn get(String path, Class<? extends Controller> controller, String action) {
        builders.add(RouteBuilder.get().route(path).to(controller, action));
        return this;
    }
    //TESTING
    public <C extends Controller> Jawn get(String path, Class<C> controller, Consumer<C> action) {
        builders.add(RouteBuilder.get().route(path).to(controller, action));
        return this;
    }
    
    // ****************
    // POST
    // ****************
    public Jawn post(String path, Result response) {
        builders.add(RouteBuilder.post().route(path).with(response));
        return this;
    }
    public Jawn post(String path, ZeroArgResponseFunction func) {
        builders.add(RouteBuilder.post().route(path).with(func));
        return this;
    }
    public Jawn post(String path, VoidResponseFunction func) {
        builders.add(RouteBuilder.post().route(path).with(func));
        return this;
    }
    public Jawn post(String path, ResponseFunction func) {
        builders.add(RouteBuilder.post().route(path).with(func));
        return this;
    }
    public Jawn post(String path, Class<? extends Controller> controller) {
        builders.add(RouteBuilder.post().route(path).to(controller));
        return this;
    }
    public Jawn post(String path, Class<? extends Controller> controller, String action) {
        builders.add(RouteBuilder.post().route(path).to(controller, action));
        return this;
    }
    //TESTING
    public <C extends Controller> Jawn post(String path, Class<C> controller, Consumer<C> action) {
        builders.add(RouteBuilder.post().route(path).to(controller, action));
        return this;
    }
    
    // ****************
    // PUT
    // ****************
    public Jawn put(String path, Result response) {
        builders.add(RouteBuilder.put().route(path).with(response));
        return this;
    }
    public Jawn put(String path, ZeroArgResponseFunction func) {
        builders.add(RouteBuilder.put().route(path).with(func));
        return this;
    }
    public Jawn put(String path, VoidResponseFunction func) {
        builders.add(RouteBuilder.put().route(path).with(func));
        return this;
    }
    public Jawn put(String path, ResponseFunction func) {
        builders.add(RouteBuilder.put().route(path).with(func));
        return this;
    }
    public Jawn put(String path, Class<? extends Controller> controller) {
        builders.add(RouteBuilder.put().route(path).to(controller));
        return this;
    }
    public Jawn put(String path, Class<? extends Controller> controller, String action) {
        builders.add(RouteBuilder.put().route(path).to(controller, action));
        return this;
    }
    //TESTING
    public <C extends Controller> Jawn put(String path, Class<C> controller, Consumer<C> action) {
        builders.add(RouteBuilder.put().route(path).to(controller, action));
        return this;
    }
    
    // ****************
    // DELETE
    // ****************
    public Jawn delete(String path, Result response) {
        builders.add(RouteBuilder.delete().route(path).with(response));
        return this;
    }
    public Jawn delete(String path, ZeroArgResponseFunction func) {
        builders.add(RouteBuilder.delete().route(path).with(func));
        return this;
    }
    public Jawn delete(String path, VoidResponseFunction func) {
        builders.add(RouteBuilder.delete().route(path).with(func));
        return this;
    }
    public Jawn delete(String path, ResponseFunction func) {
        builders.add(RouteBuilder.delete().route(path).with(func));
        return this;
    }
    public Jawn delete(String path, Class<? extends Controller> controller) {
        builders.add(RouteBuilder.delete().route(path).to(controller));
        return this;
    }
    public Jawn delete(String path, Class<? extends Controller> controller, String action) {
        builders.add(RouteBuilder.delete().route(path).to(controller, action));
        return this;
    }
    //TESTING
    public <C extends Controller> Jawn delete(String path, Class<C> controller, Consumer<C> action) {
        builders.add(RouteBuilder.delete().route(path).to(controller, action));
        return this;
    }
    
    //TODO
    // ****************
    // OPTION
    // ****************
    
    // ****************
    // HEAD
    // ****************
    public Jawn head(String path, Result response) {
        builders.add(RouteBuilder.head().route(path).with(response));
        return this;
    }
    public Jawn head(String path, ZeroArgResponseFunction func) {
        builders.add(RouteBuilder.head().route(path).with(func));
        return this;
    }
    public Jawn head(String path, VoidResponseFunction func) {
        builders.add(RouteBuilder.head().route(path).with(func));
        return this;
    }
    public Jawn head(String path, ResponseFunction func) {
        builders.add(RouteBuilder.head().route(path).with(func));
        return this;
    }
    public Jawn head(String path, Class<? extends Controller> controller) {
        builders.add(RouteBuilder.head().route(path).to(controller));
        return this;
    }
    public Jawn head(String path, Class<? extends Controller> controller, String action) {
        builders.add(RouteBuilder.head().route(path).to(controller, action));
        return this;
    }
    
    // ****************
    // OPTIONS
    // ****************
    public Jawn options(String path, Result response) {
        builders.add(RouteBuilder.options().route(path).with(response));
        return this;
    }
    public Jawn options(String path, ZeroArgResponseFunction func) {
        builders.add(RouteBuilder.options().route(path).with(func));
        return this;
    }
    public Jawn options(String path, VoidResponseFunction func) {
        builders.add(RouteBuilder.options().route(path).with(func));
        return this;
    }
    public Jawn options(String path, ResponseFunction func) {
        builders.add(RouteBuilder.options().route(path).with(func));
        return this;
    }
    public Jawn options(String path, Class<? extends Controller> controller) {
        builders.add(RouteBuilder.options().route(path).to(controller));
        return this;
    }
    public Jawn options(String path, Class<? extends Controller> controller, String action) {
        builders.add(RouteBuilder.options().route(path).to(controller, action));
        return this;
    }
    
    // *******************
    // Scannable packages
    // *******************
    public Jawn controllerPackage(String packagePath) {
        
        return this;
    }
    
    
    
    // ****************
    // Injection
    // ****************
    public <T> T require(Class<T> type) {
        checkState(bootstrapper.getInjector() != null, "App has not started yet");
        return bootstrapper.getInjector().getInstance(type);
    }
    public <T> T require(Key<T> type) {
        checkState(bootstrapper.getInjector() != null, "App has not started yet");
        return bootstrapper.getInjector().getInstance(type);
    }
    
    
    // ****************
    // Filters
    // ****************
    public Jawn filter(Filter filter) {
        filters.add(filter);
        return this;
    }
    public Jawn filter(Filter filter, Class<? extends Controller> controller) {
        filters.add(filter).to(controller);
        return this;
    }
    public Jawn filter(Class<? extends Filter> filter) {
        filters.add(filter);
        return this;
    }
    /*public Jawn filter(Filter filter, Class<? extends Controller> controller, String ... actions) {
        filters.add(filter).to(controller).forActions(actions);
        return this;
    }*/
        
    // ****************
    // Server
    // ****************
    public ServerConfig server() {
        return serverConfig;
    }
    
    public void start() {
        long startupTime = System.currentTimeMillis();
        
        JawnConfigurations properties = new JawnConfigurations(mode);
        bootstrapper.boot(properties, filters, new RouterImpl(builders, filters, properties), serverConfig, databaseConnections);
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).start(serverConfig);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
            return;
        }
        
        logger.info("Bootstrap of framework started in " + (System.currentTimeMillis() - startupTime) + " ms");
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(JawnConfigurations.class).getMode());
        logger.info("Java-web-planet: running on port: " + serverConfig.port());
    }
    
    /**
     * Asynchronously shutdowns the server
     */
    public void stop() {
        CompletableFuture.runAsync(() -> {
            try {
                bootstrapper.getInjector().getInstance(Server.class).stop();
            } catch (Exception ignore) {
                // Ignore NPE. At this point the server REALLY should be possible to find
            }
            bootstrapper.shutdown();
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

    private Jawn parseArguments(final String ... args) {
        if (args.length >= 1)
            server().port(ConvertUtil.toInteger(args[0], server().port()));
        if (args.length >= 2)
            env(Modes.determineModeFromString(args[1]));
            
        return this;
    }
    
    private void checkState(boolean expression, String errorMessage) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }
}
