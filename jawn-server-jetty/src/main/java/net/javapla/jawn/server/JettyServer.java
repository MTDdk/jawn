package net.javapla.jawn.server;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import net.javapla.jawn.server.RequestDispatcher;
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
        
        switch(config.getServerPerformance()) {
            case HIGHEST:
                pool.setMaxThreads(Runtime.getRuntime().availableProcessors() * 8);
                pool.setMinThreads(Runtime.getRuntime().availableProcessors() * 8);
                break;
            case HIGH:
                pool.setMaxThreads(Runtime.getRuntime().availableProcessors() * 2);
                pool.setMinThreads(Math.min(Runtime.getRuntime().availableProcessors(), 4));
                break;
            case MEDIUM:
                pool.setMaxThreads(Math.min(Runtime.getRuntime().availableProcessors(), 4));
                pool.setMinThreads(4);
                break;
            case MINIMAL:
                pool.setMaxThreads(4);// A minimum of 4 threads are needed for Jetty to run
                pool.setMinThreads(4);
                break;
            case CUSTOM:
                pool.setMaxThreads(config.getIoThreads());
                pool.setMinThreads(config.getIoThreads());
                break;
        }
        
        Server server = new Server(pool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(config.getPort());
        server.setConnectors(new ServerConnector[] {connector});
        
        return server;
    }
}
