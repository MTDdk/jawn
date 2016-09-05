package net.javapla.jawn.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.javapla.jawn.core.api.ApplicationBootstrap;
import net.javapla.jawn.core.api.ApplicationDatabaseBootstrap;
import net.javapla.jawn.core.api.ApplicationFilters;
import net.javapla.jawn.core.api.ApplicationRoutes;
import net.javapla.jawn.core.api.Filters;
import net.javapla.jawn.core.api.Router;
import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.database.DatabaseConnections;
import net.javapla.jawn.core.database.DatabaseModule;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.reflection.ClassLocator;
import net.javapla.jawn.core.reflection.DynamicClassFactory;
import net.javapla.jawn.core.routes.RouterImpl;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.ServerContext;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.PropertiesConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.util.Modules;

public class FrameworkBootstrap {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    protected final PropertiesImpl properties;
    protected final ApplicationConfig appConfig;
    private final List<Module> combinedModules;
    
    protected Injector injector;
    
    protected ApplicationBootstrap config;
    protected ApplicationBootstrap[] plugins;
    
//    private ArrayList<Runnable> onStartup = new ArrayList<>();

    
    public FrameworkBootstrap() {
        this(new PropertiesImpl(Modes.determineModeFromSystem()));
    }
    
    public FrameworkBootstrap(PropertiesImpl conf) {
        properties = conf;
        appConfig = new ApplicationConfig();
        combinedModules = new ArrayList<>();
    }
    
    public synchronized void boot() {
        if (injector != null) throw new RuntimeException(this.getClass().getSimpleName() + " already initialised");
        
        long startupTime = System.currentTimeMillis();
        
        configure();
        
        // read plugins
        ApplicationConfig pluginConfig = new ApplicationConfig();
        plugins = readRegisteredPlugins(pluginConfig);
        List<AbstractModule> pluginModules = pluginConfig.getRegisteredModules();
        
        // create a single injector for both the framework and the user registered modules
        List<AbstractModule> userModules = appConfig.getRegisteredModules();
        
        Injector localInjector = initInjector(userModules, pluginModules);
        
        
        // If any initialisation of filters needs to be done, like injecting ServletContext,
        // it can be done here.
        //initiateFilters(filters, localInjector/*, security*/);

        
        // compiling of routes needs element from the injector, so this is done after the creation
        initRouter(localInjector);
        
        injector = localInjector;
        
        FrameworkEngine engine = injector.getInstance(FrameworkEngine.class);
        engine.onFrameworkStartup(); // signal startup
        
//        onStartup.forEach(Runnable::run);
        
        logger.info("Bootstrap of framework started in " + (System.currentTimeMillis() - startupTime) + " ms");
    }
    
    public Injector getInjector() {
        return injector;
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
    
    /*public void addStartup(Runnable callback) {
        onStartup.add(callback);
    }*/
    
    protected void addModule(Module module) {
        this.combinedModules.add(module);
    }
    
    protected void configure() {
        // Read all the configuration from the user
        FiltersHandler filters = new FiltersHandler();
        RouterImpl router = new RouterImpl(filters, properties);
        DatabaseConnections connections = new DatabaseConnections();
        
        this.config = readConfigurations(appConfig, router, filters, connections);
        
        // supported languages are needed in the creation of the injector
        properties.setSupportedLanguages(appConfig.getSupportedLanguages());
        properties.set(Constants.DEFINED_ENCODING, appConfig.getCharacterEncoding());
        
        
        addModule(new CoreModule(properties, router));
        addModule(new DatabaseModule(connections, properties));
        addModule(new AbstractModule() {
            //ServerModule
            @Override
            protected void configure() {
              //bind(Context.class).to(JawnServletContext.class);
                bind(Context.class).to(ServerContext.class);
                bind(HttpHandler.class).to(HttpHandlerImpl.class).in(Singleton.class);
            }
        });
    }
    
    private Injector initInjector(final List<AbstractModule> userModules, List<AbstractModule> pluginModules) {
        // this class is a part of the core project
        // configure all the needed dependencies for the server
        // this includes injecting templatemanager
        
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
    
    private void initRouter(Injector localInjector) {
        //router.compileRoutes(localInjector.getInstance(ActionInvoker.class)/*localInjector*/);
        RouterImpl router = (RouterImpl)localInjector.getInstance(Router.class);
        ActionInvoker invoker = localInjector.getInstance(ActionInvoker.class);
        router.compileRoutes(invoker);
    }
    
    private ApplicationBootstrap readConfigurations(ApplicationConfig configuration, Router router, Filters filters, DatabaseConnections connections) {
        
        ClassLocator locatr = new ClassLocator(PropertiesConstants.CONFIG_PACKAGE);
        
        //TODO if multiple implementations were found - write something in the log
        
        // filters
        locate(locatr, ApplicationFilters.class, impl -> impl.filters(filters));
        
        // routes
        locate(locatr, ApplicationRoutes.class, impl -> impl.router(router));
        
        // database
        locate(locatr, ApplicationDatabaseBootstrap.class, impl -> impl.dbConnections(connections));
        
        // bootstrap
        return locate(locatr, ApplicationBootstrap.class, impl -> impl.bootstrap(configuration));
    }
    
    
    private ApplicationBootstrap[] readRegisteredPlugins(ApplicationConfig config) {
        try {
            ClassLocator locator = new ClassLocator(properties.get(Constants.PROPERTY_APPLICATION_PLUGINS_PACKAGE));
            return locateAll(locator,  ApplicationBootstrap.class, impl -> impl.bootstrap(config));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Locates an implementation of the given type, and executes the consumer if 
     * a class of the given type is found
     * 
     * @param reflections
     * @param clazz
     * @param bootstrapper
     * @return
     */
    private <T, U> T locate(final ClassLocator locator, Class<T> clazz, Consumer<T> bootstrapper) {
        Set<Class<? extends T>> set = locator.subtypeOf(clazz);
        
        if (!set.isEmpty()) {
            Class<? extends T> c = set.iterator().next();
            try {
                T locatedImplementation = DynamicClassFactory.createInstance(c, clazz);
                bootstrapper.accept(locatedImplementation);
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
    
    private <T, U> T[] locateAll(ClassLocator locator, Class<T> clazz, Consumer<T> bootstrapper) {
        Set<Class<? extends T>> set = locator.subtypeOf(clazz);
        if (!set.isEmpty()) {
            
            @SuppressWarnings("unchecked")
            T[] all =   (T[]) Array.newInstance(clazz, set.size());
            int index = 0;
            
            Iterator<Class<? extends T>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Class<? extends T> c = iterator.next();
                
                try {
                    T locatedImplementation = DynamicClassFactory.createInstance(c, clazz);
                    bootstrapper.accept(locatedImplementation);
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

}
