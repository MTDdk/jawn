package net.javapla.jawn;

import javax.servlet.ServletContextEvent;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

//README not done
public class FrameworkServletListener extends GuiceServletContextListener {

    private volatile FrameworkBootstrap bootstrap;
    private PropertiesImpl properties;
    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        
        super.contextInitialized(servletContextEvent);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }
        super.contextDestroyed(servletContextEvent);
    }

    @Override
    protected Injector getInjector() {
        FrameworkBootstrap localbootstrap = bootstrap;
        if (localbootstrap == null) {
            synchronized(this) {
                localbootstrap = bootstrap;
                if (localbootstrap == null) {
                    properties = new PropertiesImpl(ModeHelper.determineModeFromSystem());
                    bootstrap = createFramework(properties);
                    localbootstrap = bootstrap;
                }
            }
        }
        
        return localbootstrap.getInjector();
    }

    private FrameworkBootstrap createFramework(PropertiesImpl properties) {
        return new FrameworkBootstrap(properties);
    }
}
