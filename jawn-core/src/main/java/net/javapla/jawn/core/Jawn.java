package net.javapla.jawn.core;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.Router;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.routes.Route.ResponseFunction;
import net.javapla.jawn.core.routes.RouterImpl;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.ConvertUtil;
import net.javapla.jawn.core.util.Modes;


public class Jawn {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final JawnConfigurations properties;
    private final DeploymentInfo deploymentInfo;
    private final FrameworkBootstrap bootstrapper;
    
    private final FiltersHandler filters;
    private final Router router;
    
    private final ServerConfig serverConfig = new ServerConfig();
    
    
    public Jawn() {
        properties = new JawnConfigurations(Modes.determineModeFromSystem());
        deploymentInfo = new DeploymentInfo(properties);
        
        
        filters = new FiltersHandler();
        router = new RouterImpl(filters, properties);
        
        bootstrapper = new FrameworkBootstrap(properties, deploymentInfo, router);
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
        properties.set(mode);
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
    
    public Jawn lang() {
        //README: should some kind of language settings be set here?
        //bootstrapper.config().setSupportedLanguages(null);
        return this;
    }

    // ****************
    // GET
    // ****************
    public Jawn get(String path, ResponseFunction func) {
        router.GET().route(path).with(func);
        return this;
    }
    public Jawn get(String path, Class<? extends Controller> controller) {
        router.GET().route(path).to(controller);
        return this;
    }
    public Jawn get(String path, Class<? extends Controller> controller, String action) {
        router.GET().route(path).to(controller, action);
        return this;
    }
    
    // ****************
    // POST
    // ****************
    public Jawn post(String path, ResponseFunction func) {
        router.POST().route(path).with(func);
        return this;
    }
    public Jawn post(String path, Class<? extends Controller> controller) {
        router.POST().route(path).to(controller);
        return this;
    }
    public Jawn post(String path, Class<? extends Controller> controller, String action) {
        router.POST().route(path).to(controller, action);
        return this;
    }
    
    // ****************
    // PUT
    // ****************
    public Jawn put(String path, ResponseFunction func) {
        router.PUT().route(path).with(func);
        return this;
    }
    public Jawn put(String path, Class<? extends Controller> controller) {
        router.PUT().route(path).to(controller);
        return this;
    }
    public Jawn put(String path, Class<? extends Controller> controller, String action) {
        router.PUT().route(path).to(controller, action);
        return this;
    }
    
    // ****************
    // DELETE
    // ****************
    public Jawn delete(String path, ResponseFunction func) {
        router.DELETE().route(path).with(func);
        return this;
    }
    public Jawn delete(String path, Class<? extends Controller> controller) {
        router.DELETE().route(path).to(controller);
        return this;
    }
    public Jawn delete(String path, Class<? extends Controller> controller, String action) {
        router.DELETE().route(path).to(controller, action);
        return this;
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
    public Jawn filter(Filter filter, Class<? extends Controller> controller, String ... actions) {
        filters.add(filter).to(controller).forActions(actions);
        return this;
    }
        
    // ****************
    // Server
    // ****************
    public ServerConfig server() {
        return serverConfig;
    }
    
    public void start(final String ... args) {
        long startupTime = System.currentTimeMillis();
        
        bootstrapper.boot();
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).start(serverConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        logger.info("Bootstrap of framework started in " + (System.currentTimeMillis() - startupTime) + " ms");
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(JawnConfigurations.class).getMode());
    }
    
    public void stop() {
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).stop();
        } catch (Exception ignore) {
            // Ignore NPE. At this point the server REALLY should be possible to find
        }
        bootstrapper.shutdown();
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
    public static final void run(final Supplier<Jawn> jawn, final String ... args) {
        jawn
            .get()
            .parseArguments(args) // Read program arguments and overwrite server specifics
            .start(args);
    }

    private Jawn parseArguments(final String ... args) {
        switch (args.length) {
            case 2:
                env(Modes.determineModeFromString(args[1]));
            case 1:
                server().port(ConvertUtil.toInteger(args[0], server().port()));
            case 0: break;
        }
        
        return this;
    }
}
