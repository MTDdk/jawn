package net.javapla.jawn.server.undertow;

import org.xnio.Options;

import com.google.inject.Inject;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.GracefulShutdownHandler;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;

public class UndertowServer implements Server {
    
    
    private Undertow server;
    private final GracefulShutdownHandler shutdownHandler;
    private final Builder builder;
    
    @Inject
    public UndertowServer(HttpHandler dispatcher) {
        
        shutdownHandler = new GracefulShutdownHandler(createHandler(dispatcher));
        
        builder = Undertow.builder()
            .setHandler(shutdownHandler);
    }

    @Override
    public void start(ServerConfig serverConfig) throws Exception {
        builder
            .addHttpListener(serverConfig.getPort(), serverConfig.getHost())
            
            // from undertow-edge benchmark
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
            ;
        
        configureServerPerformance(builder, serverConfig);
        
        server = builder.build();
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
    
    private static void configureServerPerformance(Builder serverBuilder, ServerConfig config) {
        // TODO investigate serverBuilder.setWorkerThreads
        // Per default Builder#setWorkerThreads gets set to ioThreads * 8, but it does not get updated, when setting ioThreads,
        // so we need to set worker threads explicitly
        
        
        int undertow_minimum = 2;//may not be less than 2 because of the inner workings of Undertow
        int ioThreads;
        switch (config.getServerPerformance()) {
            case HIGHEST:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors() << 1, undertow_minimum);
                serverBuilder.setBufferSize(1024 * 16);
                break;
            case HIGH:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors(), undertow_minimum);
                break;
            default:
            case MEDIUM:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors() / 2, undertow_minimum);
                break;
            case MINIMAL:
                ioThreads = undertow_minimum;
                break;
            case CUSTOM:
                ioThreads = Math.max(config.getIoThreads(), undertow_minimum);
                break;
        }
        
        serverBuilder.setIoThreads(ioThreads);
        serverBuilder.setWorkerThreads(ioThreads * 8);
        serverBuilder.setSocketOption(Options.BACKLOG, config.getBacklog());
    }

}
