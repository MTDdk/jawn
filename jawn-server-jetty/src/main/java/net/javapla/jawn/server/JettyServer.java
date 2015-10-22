package net.javapla.jawn.server;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import net.javapla.jawn.server.spi.JawnServer;
import net.javapla.jawn.server.spi.ServerConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;


public class JettyServer implements JawnServer {
    
    public void setupAndStartServer(ServerConfig config) throws Exception {
        Server server = configureServerPerformance(config);
        
        // Setup the server application
        WebAppContext contextHandler = new WebAppContext();
        
        // set the security filter up before anything else
        //setupShiro(contextHandler, config);
        
        contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false"); //disables directory listing
        contextHandler.setContextPath(config.getContextPath());
        contextHandler.setBaseResource(Resource.newResource(config.getWebappPath()));
        contextHandler.setParentLoaderPriority(true);
        
        // Add the framework
        contextHandler.addFilter(RequestDispatcher.class, "/*", EnumSet.allOf(DispatcherType.class));
        
        // Make the server use the framework
        server.setHandler(contextHandler);
        
        server.start();
        server.join();
    }
    
    private Server configureServerPerformance(ServerConfig config) {
        // Using thread pool for benchmark - this is not necessarily needed in ordinary setups
        // Normal use is just: 
        // Server server = new Server(PORT);
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setDetailedDump(false);
        
        Server server = new Server(pool);
        ServerConnector connector = new ServerConnector(server);
        
        switch(config.getServerPerformance()) {
            case HIGHEST:
                connector.setAcceptQueueSize(10000);
                pool.setMaxThreads(Runtime.getRuntime().availableProcessors() * 8);
                pool.setMinThreads(Runtime.getRuntime().availableProcessors() * 8);
                break;
            case HIGH:
                connector.setAcceptQueueSize(1000);
                pool.setMaxThreads(Runtime.getRuntime().availableProcessors() * 2);
                pool.setMinThreads(Math.min(Runtime.getRuntime().availableProcessors(), 4));
                break;
            case MEDIUM:
                connector.setAcceptQueueSize(128);
                pool.setMaxThreads(Math.min(Runtime.getRuntime().availableProcessors(), 4));
                pool.setMinThreads(4);
                break;
            case MINIMAL:
                connector.setAcceptQueueSize(20);
                pool.setMaxThreads(4);// A minimum of 4 threads are needed for Jetty to run
                pool.setMinThreads(4);
                break;
            case CUSTOM:
                connector.setAcceptQueueSize(config.getBacklog());
                pool.setMaxThreads(config.getIoThreads());
                pool.setMinThreads(config.getIoThreads());
                break;
        }
        
        connector.setPort(config.getPort());
        server.setConnectors(new ServerConnector[] {connector});
        
        return server;
    }

    /*private void setupShiro(ServletContextHandler contextHandler, ServerConfig config) throws Exception {
        if (config.useAuthentication()) {
            contextHandler.setLogger(new Slf4jLog());
    
            //Shiro
            EnvironmentLoaderListener listener = new EnvironmentLoaderListener();
            contextHandler.callContextInitialized(listener, new ServletContextEvent(contextHandler.getServletContext()));
            contextHandler.addFilter(ShiroFilter.class,config.getAuthenticationFilterUrlMapping(),EnumSet.allOf(DispatcherType.class));
        }
    }*/
}
