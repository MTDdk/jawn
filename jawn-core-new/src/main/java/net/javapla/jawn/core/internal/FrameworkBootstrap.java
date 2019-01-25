package net.javapla.jawn.core.internal;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.internal.reflection.ClassLocator;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.parsers.ParserEngineManagerImpl;
import net.javapla.jawn.core.parsers.XmlMapperProvider;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.Modes;

public final class FrameworkBootstrap {//TODO rename to FrameworkEngine
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final ArrayList<ModuleBootstrap> userPlugins;
    
    protected Injector injector;
    
    private LinkedList<Runnable> onStartup = new LinkedList<>();
    private LinkedList<Runnable> onShutdown = new LinkedList<>();

    
    public FrameworkBootstrap() {
        userPlugins = new ArrayList<>();
    }
    
    public synchronized void boot(final Modes mode, final ServerConfig serverConfig, final Function<Injector,List<Route>> routes) {
        if (injector != null) throw new RuntimeException(this.getClass().getSimpleName() + " already initialised");
        
        final Config frameworkConfig = readConfigurations(mode);
        final Router router = new Router(/*routes*/);
        
        final com.google.inject.Module jawnModule = binder -> {
            registerCoreModules(binder, mode, frameworkConfig, router, serverConfig);
            
            final ApplicationConfig pluginConfig = new ApplicationConfig() {
                @Override
                public Binder binder() {
                    return binder;
                }

                @Override
                public Modes mode() {
                    return mode;
                }

                @Override
                public void onStartup(Runnable task) {
                    onStartup.add(task);
                }

                @Override
                public void onShutdown(Runnable task) {
                    onShutdown.add(task);
                }
            };
            // Makes it possible for plugins to override framework-specific implementations
            readRegisteredPlugins(pluginConfig, "net.javapla.jawn.core.internal.server.undertow");//readRegisteredPlugins(pluginConfig, frameworkConfig.getOptionally(Constants.PROPERTY_APPLICATION_PLUGINS_PACKAGE).orElse("net.javapla.jawn.plugins.modules"));
            readRegisteredPlugins(pluginConfig, "net.javapla.jawn.core.internal.template.stringtemplate");
            
            // Makes it possible for users to override single framework-specific implementations
            userPlugins.stream().forEach(plugin -> plugin.bootstrap(pluginConfig));
        };
        final Stage stage = mode == Modes.DEV ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        final Injector localInjector = Guice.createInjector(stage, jawnModule);
        
        
        // compiling of routes needs element from the injector, so this is done after the creation
        router.compileRoutes(routes.apply(localInjector));
        
        injector = localInjector;
        
        // signal startup
        startup();
    }
    
    public void reboot___strap(final Function<Injector,List<Route>> routes) {
        Router router = injector.getInstance(Router.class);
        router.recompileRoutes(routes.apply(injector));
    }
    
    public Injector getInjector() {
        return injector;
    }
    
    public void register(final ModuleBootstrap plugin) {
        userPlugins.add(plugin);
    }
    
    public void onStartup(final Runnable r) {
        onStartup.add(r);
    }
    
    public void onShutdown(final Runnable r) {
        onShutdown.add(r);
    }
    
    private void startup() {
        onStartup.forEach(Runnable::run);
    }
    
    /**
     * signal the framework that we are closing down
     */
    public synchronized void shutdown() {

        logger.info("Shutting down ..");
        
        onShutdown.forEach(Runnable::run);
        
        if (injector != null) {
            
            // shutdown the database connection pool
            /*try {
                DatabaseConnection connection = injector.getInstance(DatabaseConnection.class);
                if (connection != null)
                    connection.close();
            } catch (ConfigurationException ignore) {
            } catch (Exception e) { e.printStackTrace(); }
            engine = null;
            */
            
            injector = null;
        }
    }
    
    protected void registerCoreModules(final Binder binder, final Modes mode, final Config config, final Router router, final ServerConfig serverConfig) {
        
        // supported languages are needed in the creation of the injector
        //properties.setSupportedLanguages(appConfig.getSupportedLanguages());
        
        //addModule(new DatabaseModule(connections, properties));
                
        // CoreModule
        binder.bind(Charset.class).toInstance(Charset.forName(config.getOrDie("application.charset")));
        binder.bind(Modes.class).toInstance(mode);
        binder.bind(Config.class).toInstance(config);
        binder.bind(DeploymentInfo.class).toInstance(new DeploymentInfo(config, serverConfig.contextPath()));
        
        // Marshallers
        binder.bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
        binder.bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
        binder.bind(ParserEngineManager.class).to(ParserEngineManagerImpl.class).in(Singleton.class);
        binder.bind(RendererEngineOrchestrator.class).in(Singleton.class);
        
        // Framework
        binder.bind(Router.class).toInstance(router);
        binder.bind(ResultRunner.class).in(Singleton.class);
        
        // ServerModule
        binder.bind(HttpHandler.class).to(HttpHandlerImpl.class).in(Singleton.class);
    }
    
    private Config readConfigurations(final Modes mode) {
        Config frameworkConfig = ConfigImpl.framework(mode);
        try {
            Config userConfig = ConfigImpl.user(mode);
            ((ConfigImpl) frameworkConfig).merge(userConfig);
        } catch (Up.IO ignore) {} //Resource 'jawn.properties' was not found
        
        return frameworkConfig;
    }
    
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

    
    public static final String FRAMEWORK_SPLASH = "\n" 
        + "     ____.  _____  __      _________   \n"
        + "    |    | /  _  \\/  \\    /  \\      \\  \n"
        + "    |    |/  /_\\  \\   \\/\\/   /   |   \\ \n"
        + "/\\__|    /    |    \\        /    |    \\ \n"
        + "\\________\\____|__  /\\__/\\  /\\____|__  /\n"
        + "  web framework  \\/      \\/         \\/ http://www.javapla.net\n";
}
