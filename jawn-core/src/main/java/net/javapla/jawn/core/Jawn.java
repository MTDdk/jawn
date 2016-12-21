package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.Router;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.routes.ResponseFunction;
import net.javapla.jawn.core.routes.RouterImpl;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.Modes;


public class Jawn {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final JawnConfigurations properties;
    private final DeploymentInfo deploymentInfo;
    private final FrameworkBootstrap bootstrapper;
    
    private final FiltersHandler filters;
    private final Router router;
    
    private final ArrayList<Runnable> onStartup = new ArrayList<>();
    private final ArrayList<Runnable> onShutdown = new ArrayList<>();
    
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
    
    
    public Jawn onStartup(Runnable callback) {
        Objects.requireNonNull(callback);
        onStartup.add(callback);
        return this;
    }
    public Jawn onShutdown(Runnable callback) {
        Objects.requireNonNull(callback);
        onShutdown.add(callback);
        return this;
    }
    
    public Jawn env(Modes mode) {
        Objects.requireNonNull(mode);
        //TODO clearly, this needs to be changed to not set a property like this
        System.setProperty("JAWN_ENV", mode.toString()); 
        properties.set(mode);
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
    
    public void start() {
        bootstrapper.boot();
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).start(serverConfig);
        } catch (Exception e) {
            e.printStackTrace(); //TODO break when server cannot be found
        }
        
        onStartup.forEach(Runnable::run);
        
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(JawnConfigurations.class).getMode());
    }
    
    public void stop() {
        onShutdown.forEach(Runnable::run);
        
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).stop();
        } catch (Exception ignore) {
            //at this point the server REALLY should be possible to find
        }
        bootstrapper.shutdown();
    }
    
    
    public static final void run(final Supplier<Jawn> jawn) {
        jawn.get().start();
    }

}
