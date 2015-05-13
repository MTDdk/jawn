package net.javapla.jawn.server;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.util.Headers;

import java.io.File;

import javax.servlet.DispatcherType;

import net.javapla.jawn.server.spi.JawnServer;
import net.javapla.jawn.server.spi.ServerConfig;

public class UndertowServer implements JawnServer {
    
    public void setupAndStartServer(ServerConfig config) throws Exception {
        String filtername = "jawnFilter";

        // Setup the server application
        DeploymentInfo deployment = Servlets.deployment()
            .setClassLoader(UndertowServer.class.getClassLoader())
            .setContextPath(config.getContextPath())
            .setDeploymentName("test.war")
            
            // Add the framework
            .addFilter(Servlets.filter(filtername, RequestDispatcher.class))
            .addFilterUrlMapping(filtername, "/*", DispatcherType.REQUEST)
            
            // The resource folder to read from
            .setResourceManager(new FileResourceManager(new File(config.getWebappPath()), 100));
        
        // Make the server use the framework
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        
        // Start the server
        HttpHandler start = manager.start();
        Undertow.builder()
            .addHttpListener(config.getPort(), "localhost")
            
            // from undertow-edge benchmark
            .setHandler(Handlers.header(start, Headers.SERVER_STRING, "Undertow"))
            .setIoThreads(Runtime.getRuntime().availableProcessors() * 2) 
            .build()
            .start();
    }
}
