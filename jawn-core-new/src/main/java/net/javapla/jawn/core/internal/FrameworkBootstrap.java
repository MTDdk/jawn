package net.javapla.jawn.core.internal;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.util.Modules;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.reflection.ClassLocator;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.parsers.ParserEngineManagerImpl;
import net.javapla.jawn.core.parsers.XmlMapperProvider;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;
import net.javapla.jawn.core.renderers.RendererEngineOrchestratorImpl;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.Modes;

public class FrameworkBootstrap {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    protected final ApplicationConfig appConfig;
    private final List<Module> combinedModules;
    
    protected Injector injector;
    
    protected ModuleBootstrap[] plugins;

    private ArrayList<Runnable> onStartup = new ArrayList<>();
    private ArrayList<Runnable> onShutdown = new ArrayList<>();

    
    public FrameworkBootstrap() {
        appConfig = new ApplicationConfig();
        combinedModules = new ArrayList<>();
    }
    
    public synchronized void boot(final Modes mode, final List<Route.RouteHandler> routes) {
        if (injector != null) throw new RuntimeException(this.getClass().getSimpleName() + " already initialised");
        
        Config frameworkConfig = readConfigurations(mode);
        Router router = new Router(routes);
        
        registerModules(mode, frameworkConfig, router);
        
        // read plugins
        ApplicationConfig pluginConfig = new ApplicationConfig();
        plugins = readRegisteredPlugins(pluginConfig, "net.javapla.jawn.core.internal.server.undertow");//readRegisteredPlugins(pluginConfig, conf.get(Constants.PROPERTY_APPLICATION_PLUGINS_PACKAGE));
        List<AbstractModule> pluginModules = pluginConfig.getRegisteredModules();
        
        // create a single injector for both the framework and the user registered modules
        List<AbstractModule> userModules = appConfig.getRegisteredModules();
        
        Injector localInjector = initInjector(userModules, pluginModules);
        
        
        // If any initialisation of filters needs to be done, like injecting ServletContext,
        // it can be done here.
        //initiateFilters(filters, localInjector);
        
        
        // compiling of routes needs element from the injector, so this is done after the creation
        //initRouter(router, localInjector);
        
        injector = localInjector;
        
        //FrameworkEngine engine = injector.getInstance(FrameworkEngine.class);
        //engine.onFrameworkStartup(); // signal startup
        
        onStartup.forEach(Runnable::run);
    }
    
    public Injector getInjector() {
        return injector;
    }
    
    public ApplicationConfig config() {
        return appConfig;
    }
    
    public void onStartup(Runnable r) {
        onStartup.add(r);
    }
    
    public void onShutdown(Runnable r) {
        onShutdown.add(r);
    }
    
    public synchronized void shutdown() {
        if (plugins != null) {
            Arrays.stream(plugins).forEach(plugin -> plugin.destroy());
        }
        
        onShutdown.forEach(Runnable::run);
        
        /*if (injector != null) {
            
            // shutdown the database connection pool
            try {
                DatabaseConnection connection = injector.getInstance(DatabaseConnection.class);
                if (connection != null)
                    connection.close();
            } catch (ConfigurationException ignore) {
            } catch (Exception e) { e.printStackTrace(); }
            
            // signal the framework that we are closing down
            FrameworkEngine engine = injector.getInstance(FrameworkEngine.class);
            engine.onFrameworkShutdown();
            
            injector = null;
            engine = null;
        }*/
    }
    
    protected void addModule(Module module) {
        this.combinedModules.add(module);
    }
    
    protected void registerModules(final Modes mode, final Config config, final Router router) {
        
        // supported languages are needed in the creation of the injector
        //properties.setSupportedLanguages(appConfig.getSupportedLanguages());
        //properties.set(Constants.DEFINED_ENCODING, appConfig.getCharacterEncoding());
        
        //addModule(new CoreModule(properties, new DeploymentInfo(properties), router));
        //addModule(new DatabaseModule(connections, properties));
        //TODO to be removed once the server-undertow is updated
        addModule(new AbstractModule() {
            @Override
            protected void configure() {
                
                // CoreModule
                bind(Charset.class).toInstance(Charset.forName(config.getOrDie("application.charset")));
                bind(Modes.class).toInstance(mode);
                bind(Config.class).toInstance(config);
                
                // Marshallers
                bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
                bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
                bind(ParserEngineManager.class).to(ParserEngineManagerImpl.class).in(Singleton.class);
                bind(RendererEngineOrchestrator.class).to(RendererEngineOrchestratorImpl.class).in(Singleton.class);
                
                
                // Framework
                bind(Router.class).toInstance(router);
                bind(ResultRunner.class).in(Singleton.class);
                
                // ServerModule
                /*bind(Context.class).to(ContextImpl.class);*/
                bind(HttpHandler.class).to(HttpHandlerImpl.class).in(Singleton.class);
            }
        });
    }
    
    private Config readConfigurations(final Modes mode) {
        Config frameworkConfig = ConfigImpl.framework(mode);
        try {
            Config userConfig = ConfigImpl.user(mode);
            ((ConfigImpl) frameworkConfig).merge(userConfig);
        } catch (Err.IO ignore) {} //Resource 'jawn.properties' was not found
        
        return frameworkConfig;
    }
    
    private Injector initInjector(final List<AbstractModule> userModules, List<AbstractModule> pluginModules) {
        // this class is a part of the core project
        // configure all the needed dependencies for the server
        // this includes injecting templatemanager
        
        Module combined = Modules.combine(combinedModules);
        
        if ( ! pluginModules.isEmpty()) {
            // Makes it possible for plugins to override framework-specific implementations
            combined = Modules.override(combined).with(pluginModules);
        }
        
        if ( ! userModules.isEmpty()) {
            // Makes it possible for users to override single framework-specific implementations
            combined = Modules.override(combined).with(userModules);
        }
        
        //Stage stage = env.mode() == Mode.PRODUCTION ? Stage.PRODUCTION : Stage.DEVELOPMENT;
        return Guice.createInjector(/*stage*/Stage.PRODUCTION, combined);
    }
    
    /*private void initiateFilters(Filters filters, Injector injector) {
        filters.initialiseFilters(injector);
    }*/
    
    /*private void initRouter(Router router, Injector localInjector) {
        //router.compileRoutes(localInjector.getInstance(ActionInvoker.class));
        //RouterImpl router = (RouterImpl)localInjector.getInstance(Router.class);
        ActionInvoker invoker = localInjector.getInstance(ActionInvoker.class);
        ((RouterImpl)router).compileRoutes(invoker);
    }*/
    
    private ModuleBootstrap[] readRegisteredPlugins(ApplicationConfig config, String pluginsPackage) {
        try {
            ClassLocator locator = new ClassLocator(pluginsPackage);
            return locateAll(locator,  ModuleBootstrap.class, impl -> impl.bootstrap(config));
        } catch (IllegalArgumentException e) {
            logger.warn("Did not find any " + ModuleBootstrap.class.getSimpleName() + " implementations", e);
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
    private <T, U> T[] locateAll(final ClassLocator locator, final Class<T> clazz, final Consumer<T> bootstrapper) {
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
            }
            return all;
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
