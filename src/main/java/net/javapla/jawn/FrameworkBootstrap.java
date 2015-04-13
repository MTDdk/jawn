package net.javapla.jawn;

import java.util.ArrayList;
import java.util.List;

import net.javapla.jawn.application.FrameworkConfig;
import net.javapla.jawn.db.DatabaseConnections;
import net.javapla.jawn.db.DatabaseModule;
import net.javapla.jawn.exceptions.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class FrameworkBootstrap {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    final PropertiesImpl properties;
    Injector injector;

    private FrameworkConfig config;

    public FrameworkBootstrap(PropertiesImpl conf) {
        properties = conf;
    }
    
    public synchronized void boot() {
        if (injector != null) throw new RuntimeException(FrameworkBootstrap.class.getSimpleName() + " already initialised");
        
        long startupTime = System.currentTimeMillis();
        
        // Read all the configuration from the user
        ConfigApp appConfig = new ConfigApp();
        Filters filters = new Filters();
        Router router = new Router(filters);
        DatabaseConnections connections = new DatabaseConnections();
        
        config = readConfiguration(appConfig, router, filters, connections);
        
        // supported languages are needed in the creation of the injector
        properties.setSupportedLanguages(appConfig.getSupportedLanguages()); 
        
        // create a single injector for both the framework and the user registered modules
        Injector localInjector = initInjector(Lists.newArrayList(appConfig.getRegisteredModules()), router, connections);
        
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
        
        combinedModules.add(new CoreModule(properties, router));
        
        combinedModules.add(new DatabaseModule(connections, properties));
        
        combinedModules.addAll(userModules);
        
        return Guice.createInjector(Stage.PRODUCTION, combinedModules);
    }
    
    private FrameworkConfig readConfiguration(ConfigApp configuration, Router router, Filters filters, DatabaseConnections connections) {
        
        String configClassName = "app.config.ApplicationConfiguration";//TODO reconsider naming
        
        try {
            FrameworkConfig localConfig = DynamicClassFactory.createInstance(configClassName, FrameworkConfig.class, false);
            
            localConfig.bootstrap(configuration);
            localConfig.filters(filters);
            localConfig.router(router);
            localConfig.dbConnections(connections);
            
            logger.debug("Loaded configuration from: " + configClassName);
            return localConfig;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch(ConfigurationException e){
            throw  e;
        } catch (Exception e) {
            logger.debug("Did not find custom configuration. Going with built in defaults: " + getCauseMessage(e));
        }
        
        return null;
    }
    
  //TODO: refactor to some util class. This is stolen...ehrr... borrowed from Apache ExceptionUtils
    static String getCauseMessage(Throwable throwable) {
        List<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list.get(0).getMessage();
    }
}
