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
import net.javapla.jawn.core.internal.reflection.ClassFactory;
import net.javapla.jawn.core.internal.reflection.ClassLocator;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.parsers.XmlMapperProvider;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

public final class FrameworkBootstrap /*implements Injection*/ {//TODO rename to FrameworkEngine
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final ArrayList<ModuleBootstrap> userPlugins;
    
    protected com.google.inject.Module frameworkModule;
    protected Injector injector;
    
    private LinkedList<Runnable> onStartup = new LinkedList<>();
    private LinkedList<Runnable> onShutdown = new LinkedList<>();

    
    public FrameworkBootstrap() {
        userPlugins = new ArrayList<>();
    }
    
    public synchronized void boot(final Modes mode, final ServerConfig.Impl serverConfig, final Function<Injector,List<Route>> routes) {
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
            readRegisteredPlugins(pluginConfig, frameworkConfig.getOptionally(Constants.PROPERTY_APPLICATION_PLUGINS_PACKAGE).orElse("net.javapla.jawn.plugins.modules"));
            readRegisteredPlugins(pluginConfig, "net.javapla.jawn.core.internal.image");
            
            // Makes it possible for users to override single framework-specific implementations
            userPlugins.stream().forEach(plugin -> plugin.bootstrap(pluginConfig));
        };
        
        //Module userModule = userModule(mode);
        
        final Stage stage = mode == Modes.DEV ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        final Injector localInjector = Guice.createInjector(stage, jawnModule/*, userModule*/);
        
        
        // compiling of routes needs element from the injector, so this is done after the creation
        router.compileRoutes(routes.apply(localInjector));
        
        frameworkModule = jawnModule;
        injector = localInjector;
        
        // signal startup
        startup();
    }
    
    /*public com.google.inject.Module userModule(final Modes mode) {
        final com.google.inject.Module userModule = binder -> {
            
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
            
            // For this to work, we need to reload the interface AND the implementor for each plugin.
            // Right now, if we reload the implementor it will affect the resulting injector, as the
            // "key", which is the interface, still has the same hashCode (or some other identity).
            // This might even not be enough, as each controller that uses the interface, most likely
            // also needs to be recompiled in order to use the correct "key"/interface within.
            userPlugins.stream().forEach(plugin -> {
                System.out.println("userModule 1 " + plugin.hashCode());
                if (mode == Modes.DEV) {
                    plugin = ClassFactory.createInstance(
                        ClassFactory.getCompiledClass(plugin.getClass().getName(), false), 
                        ModuleBootstrap.class
                    );
                    
                    //plugin = ClassFactory.createInstance(plugin.getClass(), ModuleBootstrap.class);
                    System.out.println("userModule 2 " + plugin.hashCode());
                }
                
                
                plugin.bootstrap(pluginConfig);
            });
        };
        
        return userModule;
    }*/
    
    public void reboot___strap(final Function<Injector,List<Route>> routes/*, com.google.inject.Module userModule*/) {
        /*
        var childInjector = injector.createChildInjector(userModule(Modes.DEV));//Guice.createInjector(frameworkModule, userModule(Modes.DEV, false));
        injector = childInjector;
        */
        
        Router router = injector.getInstance(Router.class);
        router.recompileRoutes(routes.apply(injector));
    }
    
    public Injector getInjector() {
        return injector;
    }
    
    /*@Override
    public <T> T require(Key<T> key) {
        System.out.println("injector.Key() " +injector.hashCode());
        System.out.println("injector.Key() " +key.hashCode());
        return injector.getInstance(key);
    }
    
    @Override
    public <T> T require(Class<T> type) {
        System.out.println("injector.Class() " +injector.hashCode());
        return injector.getInstance(type);
    }*/

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
        
        if (injector != null) {
            
            logger.info("Shutting down ..");
            
            onShutdown.forEach(run -> {
                try {
                    run.run();
                } catch (Exception e) {
                    logger.error("Failed a onShutdown task", e);
                }
            });
            
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
    
    protected void registerCoreModules(final Binder binder, final Modes mode, final Config config, final Router router, final ServerConfig.Impl serverConfig) {
        
        // supported languages are needed in the creation of the injector
        //properties.setSupportedLanguages(appConfig.getSupportedLanguages());
        
        //addModule(new DatabaseModule(connections, properties));
                
        // CoreModule
        binder.bind(Charset.class).toInstance(Charset.forName(config.getOrDie("application.charset")));
        binder.bind(Modes.class).toInstance(mode);
        binder.bind(Config.class).toInstance(config);
        binder.bind(DeploymentInfo.class).toInstance(new DeploymentInfo(config, serverConfig.context()));
        //binder.bind(Injection.class).toInstance(this);
        
        // Marshallers
        binder.bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
        binder.bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
        binder.bind(ParserEngineManager.class).in(Singleton.class);
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
                    T locatedImplementation = ClassFactory.createInstance(c, clazz);
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
