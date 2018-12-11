package net.javapla.jawn.core;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.internal.FrameworkBootstrap;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.util.Modes;

public class Jawn {
    
    protected static final Logger logger = LoggerFactory.getLogger(Jawn.class);
    
    private final FrameworkBootstrap bootstrap;
    
    private Modes mode = Modes.DEV;

    public Jawn() {
        bootstrap = new FrameworkBootstrap();
    }
    
    // ****************
    // Configuration
    // ****************
    protected Jawn mode(Modes mode) {
        if (mode != null)
            this.mode = mode;
        return this;
    }
    
    
    // ****************
    // Router
    // ****************
    public Jawn get(final String path) {
        
        return this;
    }
    
    
    public void start() {
        long startupTime = System.currentTimeMillis();
        
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        bootstrap.boot(mode);
        
        /*JawnConfigurations properties = new JawnConfigurations(mode);
        bootstrapper.boot(properties, filters, new RouterImpl(builders, filters, properties), databaseConnections);*/
        Injector injector = bootstrap.getInjector();
        try {
            injector.getInstance(Server.class).start(/*serverConfig*/);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
            return;
        }
        
        logger.info("Bootstrap of framework started in " + (System.currentTimeMillis() - startupTime) + " ms");
//        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(JawnConfigurations.class).getMode());
//        logger.info("Java-web-planet: running on port: " + serverConfig.port());
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
    public static final void run(final Class<? extends Jawn> jawn, final String ... args) {
        run(DynamicClassFactory.createInstance(jawn), args);
    }

    //TODO
    private Jawn parseArguments(final String ... args) {
//        if (args.length >= 1)
//            server().port(ConvertUtil.toInteger(args[0], server().port()));
//        if (args.length >= 2)
//            env(Modes.determineModeFromString(args[1]));
            
        return this;
    }
    
    private void checkState(boolean expression, String errorMessage) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }
}
