package net.javapla.jawn.server.undertow;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.xnio.Options;

import com.google.inject.Inject;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.GracefulShutdownHandler;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;

public final class UndertowServer implements Server {
    
    private final HttpHandler dispatcher;
    private final Config conf;
    
    private Undertow server;
    private GracefulShutdownHandler shutdownHandler;
    
    @Inject
    UndertowServer(final HttpHandler dispatcher, final Config conf) {
        this.dispatcher = dispatcher;
        this.conf = conf;
    }

    @Override
    public void start(final ServerConfig.Impl serverConfig) throws Exception {
        shutdownHandler = new GracefulShutdownHandler(createHandler(dispatcher));
        
        
        final Builder builder = Undertow.builder()
            .setHandler(shutdownHandler)
            .addHttpListener(serverConfig.port(), serverConfig.host())
            
            // from undertow-edge benchmark
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
            
            // from ActFramework
            //.setServerOption(UndertowOptions.BUFFER_PIPELINED_DATA, true)
            .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
            ;
        
        
        conf.getBooleanOptionally("server.http2.enabled")
            .ifPresent(b -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, b));
        
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
        //NO-OP
    }
    
    @Override
    public Optional<Executor> executor() {
        return Optional.ofNullable(server.getWorker());
    }
    
    private static final io.undertow.server.HttpHandler createHandler(final HttpHandler dispatcher) {
        return new UndertowHandler(dispatcher);
    }
    
    private static void configureServerPerformance(Builder serverBuilder, ServerConfig.Impl config) {
        // TODO investigate serverBuilder.setWorkerThreads
        // Per default Builder#setWorkerThreads gets set to ioThreads * 8, but it does not get updated, when setting ioThreads,
        // so we need to set worker threads explicitly
        
        
        int undertow_minimum = 2;//may not be less than 2 because of the inner workings of Undertow
        int ioThreads;//, workerThreads;
        switch (config.performance()) {
            case HIGHEST:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors() * 2, undertow_minimum);
                serverBuilder.setIoThreads(ioThreads);
                //serverBuilder.setWorkerThreads(32);
                break;
            default:
            case MINIMUM:
                ioThreads = undertow_minimum;
                serverBuilder.setIoThreads(ioThreads);
                serverBuilder.setWorkerThreads(ioThreads);
                serverBuilder.setSocketOption(Options.BACKLOG, config.backlog());
                break;
            case CUSTOM:
                ioThreads = Math.max(config.ioThreads(), undertow_minimum);
                serverBuilder.setIoThreads(ioThreads);
                serverBuilder.setWorkerThreads(ioThreads * 8);
                serverBuilder.setSocketOption(Options.BACKLOG, config.backlog());
                break;
        }
        
    }

}
