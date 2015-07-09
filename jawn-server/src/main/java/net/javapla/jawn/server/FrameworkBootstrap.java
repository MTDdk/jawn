package net.javapla.jawn.server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.javapla.jawn.core.ApplicationConfig;
import net.javapla.jawn.core.CoreModule;
import net.javapla.jawn.core.DynamicClassFactory;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.FrameworkEngine;
import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.database.DatabaseConnections;
import net.javapla.jawn.core.database.DatabaseModule;
import net.javapla.jawn.core.spi.ApplicationBootstrap;
import net.javapla.jawn.core.spi.ApplicationDatabaseBootstrap;
import net.javapla.jawn.core.spi.ApplicationFilters;
import net.javapla.jawn.core.spi.ApplicationRoutes;
import net.javapla.jawn.core.spi.Filters;
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
    private ApplicationBootstrap[] plugins;

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
//        SecurityFilterFactory security = new SecurityFilterFactory();
        FiltersHandler filters = new FiltersHandler(/*security*/);
        Router router = new Router(filters);
        DatabaseConnections connections = new DatabaseConnections();
        
        config = readConfiguration(appConfig, router, filters, connections);
        
        // supported languages are needed in the creation of the injector
        properties.setSupportedLanguages(appConfig.getSupportedLanguages());
        properties.set(Constants.DEFINED_ENCODING, appConfig.getCharacterEncoding());
        
        // create a single injector for both the framework and the user registered modules
        List<AbstractModule> userModules = appConfig.getRegisteredModules();//appConfig.getRegisteredModules() == null ? null : Arrays.asList(appConfig.getRegisteredModules());
        
        // read plugins
        ApplicationConfig pluginConfig = new ApplicationConfig();
        plugins = readRegisteredPlugins(pluginConfig);
        List<AbstractModule> pluginModules = pluginConfig.getRegisteredModules();
        
        Injector localInjector = initInjector(userModules, router, connections, pluginModules);
        
        
        // If any initialisation of filters needs to be done, like injecting ServletContext,
        // it can be done here.
        //initiateFilters(filters, localInjector/*, security*/);

        
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
        if (plugins != null) {
            Arrays.stream(plugins).forEach(plugin -> plugin.destroy());
        }
        
        if (injector != null) {
            
            // shutdown the database connection pool
            try {
                DatabaseConnection connection = injector.getInstance(DatabaseConnection.class);
                if (connection != null)
                    connection.close();
            } catch (Exception ignore) { ignore.printStackTrace(); }
            
            // signal the framework that we are closing down
            FrameworkEngine engine = injector.getInstance(FrameworkEngine.class);
            engine.onFrameworkShutdown();
            
            injector = null;
            engine = null;
        }
    }
    
    public Injector getInjector() {
        return injector;
    }

    private Injector initInjector(final List<AbstractModule> userModules, Router router, DatabaseConnections connections, List<AbstractModule> pluginModules) {
        // this class is a part of the server project
        // configure all the needed dependencies for the server
        // this includes injecting templatemanager
        
        List<AbstractModule> combinedModules = new ArrayList<>();
        
        combinedModules.add(new CoreModule());
        
        combinedModules.add(new ServerModule(properties, router));
        
        combinedModules.add(new DatabaseModule(connections, properties));
        
        /*combinedModules.add(new AbstractModule() {
            @Override
            protected void configure() {
                //Get all the constants from Filters and bind them
                //This way we go through all filter classes, lets guice handle their instantiation
                //and only the class will need to be stated when defining a new filter
                //TODO
                bindConstant().annotatedWith(Names.named("something_like_userrole")).to("like_admin");
                //this kind of binding constants might not be viable, as multiple security filters
                //need different permissions
            }
        });*/
        
        Module combined = Modules.combine(combinedModules);
        
        if ( ! pluginModules.isEmpty()) {
            // Makes it possible for plugins to override framework-specific implementations
            combined = Modules.override(combined).with(pluginModules);
        }
        
        if ( ! userModules.isEmpty()) {
            // Makes it possible for users to override single framework-specific implementations
            combined = Modules.override(combined).with(userModules);
        }
        
        return Guice.createInjector(Stage.PRODUCTION, combined);
    }
    
    private ApplicationBootstrap readConfiguration(ApplicationConfig configuration, Router router, Filters filters, DatabaseConnections connections) {
        
        Reflections reflections = new Reflections("app.config");
        
        //TODO if multiple implementations were found - write something in the log
        
        // filters
        locate(reflections, ApplicationFilters.class, impl -> impl.filters(filters));
        
        // routes
        locate(reflections, ApplicationRoutes.class, impl -> impl.router(router));
        
        // database
        locate(reflections, ApplicationDatabaseBootstrap.class, impl -> impl.dbConnections(connections));
        
        // bootstrap
        return locate(reflections, ApplicationBootstrap.class, impl -> impl.bootstrap(configuration));
    }
    
    private ApplicationBootstrap[] readRegisteredPlugins(ApplicationConfig config) {
        Reflections reflections = new Reflections("net.javapla.jawn.plugins.modules");
        return locateAll(reflections, ApplicationBootstrap.class, impl -> impl.bootstrap(config));
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
            } catch (Exception e) {
                logger.debug("Error reading custom configuration. Going with built in defaults. The error was: " + getCauseMessage(e));
            }
        } else {
            logger.debug("Did not find custom configuration for {}. Going with built in defaults ", clazz);
        }
        return null;
    }
    
    private <T, U> T[] locateAll(Reflections reflections, Class<T> clazz, Consumer<T> f) {
        Set<Class<? extends T>> set = reflections.getSubTypesOf(clazz);
        if (!set.isEmpty()) {
            
            @SuppressWarnings("unchecked")
            T[] all =   (T[]) Array.newInstance(clazz, set.size());
            int index = 0;
            
            Iterator<Class<? extends T>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Class<? extends T> c = iterator.next();
                
                try {
                    T locatedImplementation = DynamicClassFactory.createInstance(c, clazz);
                    f.accept(locatedImplementation);
                    logger.debug("Loaded configuration from: " + c);
                    all[index++] = locatedImplementation;
                    
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    logger.debug("Error reading custom configuration. Going with built in defaults. The error was: " + getCauseMessage(e));
                }
                
                return all;
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
    
    private void initiateFilters(FiltersHandler filters, Injector localInjector/*, SecurityFilterFactory security*/) {
        // Get current database connection (if any)
//        DatabaseConnection connection = localInjector.getInstance(DatabaseConnection.class);
//        filters.setDatabaseConnection(connection);
        
        
        // Create and inject the security framework
//        Context context = localInjector.getInstance(Context.class); //README Context is NOT fully ready at this point, as it needs request,response
//        security.initialiseSecurityManager(connection, context);
//        SecurityFilterFactory security = new SecurityFilterFactory(connection, context);
//        filters.setSecurityFilterFactory(security);
    }
}
