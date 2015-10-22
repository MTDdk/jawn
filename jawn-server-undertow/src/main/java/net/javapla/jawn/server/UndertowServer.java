package net.javapla.jawn.server;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.util.Headers;

import java.io.File;

import javax.servlet.DispatcherType;

import org.xnio.Options;

import net.javapla.jawn.server.spi.JawnServer;
import net.javapla.jawn.server.spi.ServerConfig;

public class UndertowServer implements JawnServer {
    
    public void setupAndStartServer(ServerConfig config) throws Exception {
        String filtername = "jawnFilter";

        // Setup the server application
        DeploymentInfo deployment = Servlets.deployment()
            .setClassLoader(UndertowServer.class.getClassLoader())
            .setContextPath(config.getContextPath())
            .setDeploymentName("test.war");
            
        // security
        //configureServerAuthentication(deployment, config);
            
            
        // Add the framework
        deployment
            .addFilter(Servlets.filter(filtername, RequestDispatcher.class))
            .addFilterUrlMapping(filtername, "/*", DispatcherType.REQUEST)
            
            
            // The resource folder to read from
            .setResourceManager(new FileResourceManager(new File(config.getWebappPath()), 100));
        
        // Make the server use the framework
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        
        // Start the server
        HttpHandler start = manager.start();
        Builder serverBuilder = Undertow.builder()
            .addHttpListener(config.getPort(), config.getHost())
            
            // from undertow-edge benchmark
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
            .setHandler(Handlers.header(start, Headers.SERVER_STRING, "Undertow"));
        
        configureServerPerformance(serverBuilder, config);
            
        serverBuilder.build().start();
    }
    
    /*private void configureServerAuthentication(DeploymentInfo deployment, ServerConfig config) {
        if (config.useAuthentication()) {
            deployment
                .addListener(Servlets.listener(EnvironmentLoaderListener.class))
                .addFilter(Servlets.filter("shiro", ShiroFilter.class))
                .addFilterUrlMapping("shiro",config.getAuthenticationFilterUrlMapping(),DispatcherType.REQUEST);
        }
    }*/
    
    private void configureServerPerformance(Builder serverBuilder, ServerConfig config) {
        // TODO investigate serverBuilder.setWorkerThreads
        // Per default Builder#setWorkerThreads gets set to ioThreads * 8, but it does not get updated, when setting ioThreads,
        // so we need to set worker threads explicitly
        
        
        int undertow_minimum = 2;//may not be less than 2 because of the inner workings of Undertow
        switch (config.getServerPerformance()) {
            case HIGHEST:
                serverBuilder.setSocketOption(Options.BACKLOG, 10000);
                serverBuilder.setIoThreads(Math.max(Runtime.getRuntime().availableProcessors() * 2, undertow_minimum));
                break;
            case HIGH:
                serverBuilder.setSocketOption(Options.BACKLOG, 1000);
                serverBuilder.setIoThreads(Math.max(Runtime.getRuntime().availableProcessors(), undertow_minimum));
                break;
            case MEDIUM:
                serverBuilder.setSocketOption(Options.BACKLOG, 128);
                serverBuilder.setIoThreads(Math.max(Runtime.getRuntime().availableProcessors() / 2, undertow_minimum));
                break;
            case MINIMAL:
                serverBuilder.setSocketOption(Options.BACKLOG, 20);
                serverBuilder.setIoThreads(undertow_minimum); 
                break;
            case CUSTOM:
                serverBuilder.setSocketOption(Options.BACKLOG, config.getBacklog());
                serverBuilder.setIoThreads(Math.max(config.getIoThreads(), undertow_minimum));
                break;
        }
    }
}
