package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.Modes;


public class Jawn {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final JawnConfigurations properties;
    private final DeploymentInfo deploymentInfo;
    private final FrameworkBootstrap bootstrapper;
    private final ArrayList<Runnable> onStartup = new ArrayList<>();
    private final ArrayList<Runnable> onShutdown = new ArrayList<>();
    
    private final ServerConfig serverConfig = new ServerConfig();
    
    
    public Jawn() {
        properties = new JawnConfigurations(Modes.determineModeFromSystem());
        deploymentInfo = new DeploymentInfo(properties);
        bootstrapper = new FrameworkBootstrap(properties, deploymentInfo);
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
