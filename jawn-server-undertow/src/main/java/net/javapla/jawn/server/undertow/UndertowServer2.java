package net.javapla.jawn.server.undertow;

import org.xnio.Options;

import com.google.inject.Inject;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.handlers.GracefulShutdownHandler;
import net.javapla.jawn.server.api.HttpHandler;
import net.javapla.jawn.server.api.Server;
import net.javapla.jawn.server.api.ServerConfig;

public class UndertowServer2 implements Server {
    
    
    private final Undertow server;
    private final GracefulShutdownHandler shutdownHandler;
    
    @Inject
    public UndertowServer2(HttpHandler dispatcher) {
        int undertow_minimum = 2;//may not be less than 2 because of the inner workings of Undertow
        
        Builder builder = Undertow.builder()
                .setIoThreads(Math.max(Runtime.getRuntime().availableProcessors() / 2, undertow_minimum))
                .setSocketOption(Options.BACKLOG, ServerConfig.BACKLOG_DEFAULT) // should probably be 1024
                .setWorkerThreads(20) // minimum
                ;
        
        builder.addHttpListener(8080, "0.0.0.0");
        
        shutdownHandler = new GracefulShutdownHandler(createHandler(dispatcher));
        builder.setHandler(shutdownHandler);
        
        this.server = builder.build(); 
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        shutdownHandler.shutdown();
        shutdownHandler.awaitShutdown(5000);
        server.stop();
    }

    @Override
    public void join() throws InterruptedException {
        //NOOP
    }
    
    private static final io.undertow.server.HttpHandler createHandler(final HttpHandler dispatcher ) {
        return new UndertowHandler(dispatcher);
    }

}
