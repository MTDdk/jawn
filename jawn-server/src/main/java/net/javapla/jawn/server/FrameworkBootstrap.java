package net.javapla.jawn.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.javapla.jawn.core.ApplicationConfig;
import net.javapla.jawn.core.CoreModule;
import net.javapla.jawn.core.DynamicClassFactory;
import net.javapla.jawn.core.Filters;
import net.javapla.jawn.core.FrameworkEngine;
import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.database.DatabaseConnections;
import net.javapla.jawn.core.database.DatabaseModule;
import net.javapla.jawn.core.exceptions.ConfigurationException;
import net.javapla.jawn.core.spi.ApplicationBootstrap;
import net.javapla.jawn.core.spi.ApplicationDatabaseBootstrap;
import net.javapla.jawn.core.spi.ApplicationFilters;
import net.javapla.jawn.core.spi.ApplicationRoutes;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.ModeHelper;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;

public class FrameworkBootstrap {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    final PropertiesImpl properties;
    Injector injector;

    private ApplicationBootstrap config;

    public FrameworkBootstrap() {
        properties = new PropertiesImpl(ModeHelper.determineModeFromSystem());
    }
    
    public FrameworkBootstrap(PropertiesImpl conf) {
        properties = conf;
    }
    
    public synchronized void boot() {
        if (injector != null) throw new RuntimeException(FrameworkBootstrap.class.getSimpleName() + " already initialised");
        
        long startupTime = System.currentTimeMillis();
        
        // Read all the configuration from the user
        ApplicationConfig appConfig = new ApplicationConfig();
        Filters filters = new Filters();
        Router router = new Router(filters);
        DatabaseConnections connections = new DatabaseConnections();
        
        config = readConfiguration(appConfig, router, filters, connections);
        
        // supported languages are needed in the creation of the injector
        properties.setSupportedLanguages(appConfig.getSupportedLanguages());
        properties.set(Constants.DEFINED_ENCODING, appConfig.getCharacterEncoding());
        
        // create a single injector for both the framework and the user registered modules
        List<AbstractModule> userModules = appConfig.getRegisteredModules() == null ? null : Arrays.asList(appConfig.getRegisteredModules());
        Injector localInjector = initInjector(userModules, router, connections);
        
        // compiling of routes needs an injector, so this is done after the creation
        router.compileRoutes(localInjector);
        
        injector = localInjector;
        
        FrameworkEngine engine = injector.getInstance(FrameworkEngine.class);
        engine.onFrameworkStartup(); // signal startup
        
        logger.info("Bootstrap of framework started in " + (System.currentTimeMillis() - startupTime) + " ms");
    }
    
    public synchronized void shutdown() {
        if (config != null) {
            config.destroy();
        }
        if (injector != null) {
            FrameworkEngine engine = injector.getInstance(FrameworkEngine.class);
            engine.onFrameworkShutdown();
            injector = null;
            engine = null;
        }
    }
    
    public Injector getInjector() {
        return injector;
    }

    private Injector initInjector(final List<AbstractModule> userModules, Router router, DatabaseConnections connections) {
        // this class is a part of the server project
        // configure all the needed dependencies for the server
        // this includes injecting templatemanager
        
        List<AbstractModule> combinedModules = new ArrayList<>();
        
        combinedModules.add(new CoreModule());
        
        combinedModules.add(new ServerModule(properties, router));
        
        combinedModules.add(new DatabaseModule(connections, properties));
        
        // Makes it possible for users to override single framework-specific implementations
        if (userModules != null) {
            Module modules = Modules.override(combinedModules).with(userModules);
            return Guice.createInjector(Stage.PRODUCTION, modules);
//            combinedModules.addAll(userModules);
        }
        
        return Guice.createInjector(Stage.PRODUCTION, combinedModules);
    }
    
    private ApplicationBootstrap readConfiguration(ApplicationConfig configuration, Router router, Filters filters, DatabaseConnections connections) {
        
        Reflections reflections = new Reflections("app.config");
        
        // filters
        locate(reflections, ApplicationFilters.class, impl -> impl.filters(filters));
        
        // routes
        locate(reflections, ApplicationRoutes.class, impl -> impl.router(router));
        
        // database
        locate(reflections, ApplicationDatabaseBootstrap.class, impl -> impl.dbConnections(connections));
        
        // bootstrap
        return locate(reflections, ApplicationBootstrap.class, impl -> impl.bootstrap(configuration));
    }
    
    /**
     * Locates an implementation of the given type, and executes the consumer if 
     * a class of the given type is found
     * 
     * @param reflections
     * @param clazz
     * @param f
     * @return
     */
    private <T, U> T locate(Reflections reflections, Class<T> clazz, Consumer<T> f) {
        Set<Class<? extends T>> set = reflections.getSubTypesOf(clazz);
        if (!set.isEmpty()) {
            Class<? extends T> c = set.iterator().next();
            try {
                T locatedImplementation = DynamicClassFactory.createInstance(c, clazz);
                f.accept(locatedImplementation);
                logger.debug("Loaded configuration from: " + c);
                return locatedImplementation;
            } catch (IllegalArgumentException e) {
                throw e;
            } catch(ConfigurationException e){
                throw  e;
            } catch (Exception e) {
                logger.debug("Did not find custom configuration. Going with built in defaults: " + getCauseMessage(e));
            }
        } else {
            logger.debug("Did not find custom configuration for {}. Going with built in defaults ", clazz);
        }
        return null;
    }

    private String getCauseMessage(Throwable throwable) {
        List<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list.get(0).getMessage();
    }
}
