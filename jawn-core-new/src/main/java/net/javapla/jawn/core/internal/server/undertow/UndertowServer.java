package net.javapla.jawn.core.internal.server.undertow;

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
    public void start(/*, ServerConfig serverConfig*/) throws Exception {
        shutdownHandler = new GracefulShutdownHandler(createHandler(dispatcher, conf));
        
        
        final Builder builder = Undertow.builder()
            .setHandler(shutdownHandler)
            .addHttpListener(8080, "0.0.0.0")//serverConfig.port(), serverConfig.host())
            
            // from undertow-edge benchmark
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
            
            // from ActFramework
            //.setServerOption(UndertowOptions.BUFFER_PIPELINED_DATA, true)
            //.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
            ;
        
        
        conf.getBooleanOptionally("server.http2.enabled")
            .ifPresent(b -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, b));
        
        configureServerPerformance(builder/*, serverConfig*/);
        
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
    
    private static final io.undertow.server.HttpHandler createHandler(final HttpHandler dispatcher, final Config config ) {
        return new UndertowHandler(dispatcher, config);
    }
    
    private static void configureServerPerformance(Builder serverBuilder/*, ServerConfig config*/) {
        // TODO investigate serverBuilder.setWorkerThreads
        // Per default Builder#setWorkerThreads gets set to ioThreads * 8, but it does not get updated, when setting ioThreads,
        // so we need to set worker threads explicitly
        
        
        int undertow_minimum = 2;//may not be less than 2 because of the inner workings of Undertow
        int ioThreads, workerThreads;
        /*switch (config.serverPerformance()) {
            case HIGHEST:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors() << 1, undertow_minimum);
                workerThreads = ioThreads * 8;
                serverBuilder.setBufferSize(1024 * 16);
                break;
            case HIGH:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors(), undertow_minimum);
                workerThreads = ioThreads * 8;
                break;
            default:
            case MEDIUM:*/
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors() , undertow_minimum);
                workerThreads = ioThreads * 4;
                /*break;
            case LOW:
                ioThreads = undertow_minimum;
                workerThreads = ioThreads;
                break;
            case CUSTOM:
                ioThreads = Math.max(config.ioThreads(), undertow_minimum);
                workerThreads = ioThreads * 8;
                break;
        }*/
        
        serverBuilder.setIoThreads(ioThreads);
        serverBuilder.setWorkerThreads(workerThreads);
        
        //serverBuilder.setSocketOption(Options.BACKLOG, config.backlog());
        serverBuilder.setSocketOption(Options.WORKER_IO_THREADS, ioThreads);
    }

}
