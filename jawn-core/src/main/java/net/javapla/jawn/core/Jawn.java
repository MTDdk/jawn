package net.javapla.jawn.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.server.Server;


public class Jawn {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected final Injector injector;

    protected final FrameworkBootstrap bootstrapper;
    
    public Jawn() {
        bootstrapper = new FrameworkBootstrap();
        bootstrapper.boot();
        injector = bootstrapper.getInjector();
        
        try {
            injector.getInstance(Server.class).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(PropertiesImpl.class).getMode());
    }

}
