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
        // Using thread pool for benchmark - this is not necessarily needed in ordinary setups
        // Normal use is just: 
        // Server server = new Server(PORT);
        QueuedThreadPool pool = new QueuedThreadPool(80,10);
        pool.setDetailedDump(false);
        Server server = new Server(pool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(config.getPort());
        server.setConnectors(new ServerConnector[] {connector});
        
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
}
