package net.javapla.jawn.server;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
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

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;

public class UndertowServer implements JawnServer {
    
    public void setupAndStartServer(ServerConfig config) throws Exception {
        String filtername = "jawnFilter";

        // Setup the server application
        DeploymentInfo deployment = Servlets.deployment()
            .setClassLoader(UndertowServer.class.getClassLoader())
            .setContextPath(config.getContextPath())
            .setDeploymentName("test.war");
            
        // security
        configureServerAuthentication(deployment, config);
            
            
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
            .setHandler(Handlers.header(start, Headers.SERVER_STRING, "Undertow"));
        
        configureServerPerformance(serverBuilder, config);
            
        serverBuilder.build().start();
    }
    
    private void configureServerAuthentication(DeploymentInfo deployment, ServerConfig config) {
        if (config.useAuthentication()) {
            deployment
                .addListener(Servlets.listener(EnvironmentLoaderListener.class))
                .addFilter(Servlets.filter("shiro", ShiroFilter.class))
                .addFilterUrlMapping("shiro",config.getAuthenticationFilterUrlMapping(),DispatcherType.REQUEST);
        }
    }
    
    private void configureServerPerformance(Builder serverBuilder, ServerConfig config) {
        switch (config.getServerPerformance()) {
            case HIGHEST:
                serverBuilder.setIoThreads(Runtime.getRuntime().availableProcessors() * 2);
                break;
            case HIGH:
                serverBuilder.setIoThreads(Runtime.getRuntime().availableProcessors());
                break;
            case MEDIUM:
                serverBuilder.setIoThreads(Math.max(Runtime.getRuntime().availableProcessors() / 2, 2));
                break;
            case MINIMAL:
                serverBuilder.setIoThreads(2);//may not be less than 2 because of the inner workings of Undertow 
                break;
            case CUSTOM:
                serverBuilder.setIoThreads(Math.max(config.getIoThreads(), 2));
                break;
        }
    }
}
