package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.util.Modes;


public class Jawn {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final PropertiesImpl properties;
    private final FrameworkBootstrap bootstrapper;
    private final ArrayList<Runnable> onStartup = new ArrayList<>();
    private final ArrayList<Runnable> onShutdown = new ArrayList<>();
    
    
    public Jawn() {
        properties = new PropertiesImpl(Modes.determineModeFromSystem());
        bootstrapper = new FrameworkBootstrap(properties);
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
        //TODO clearly, this needs to be changed
        System.setProperty("JAWN_ENV", mode.toString()); 
        properties.set(mode);
        return this;
    }
    
    public void start() {
        bootstrapper.boot();
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        onStartup.forEach(Runnable::run);
        
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(PropertiesImpl.class).getMode());
    }
    
    public void stop() {
        onShutdown.forEach(Runnable::run);
        
        Injector injector = bootstrapper.getInjector();
        try {
            injector.getInstance(Server.class).stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bootstrapper.shutdown();
    }
    
    
    public static final void run(final Supplier<Jawn> jawn) {
        jawn.get().start();
    }

}
