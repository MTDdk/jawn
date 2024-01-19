package net.javapla.jawn.server.undertow;

import org.xnio.Options;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.Server;
import net.javapla.jawn.core.util.LoggerManipulation;

public class ServerModule implements Server {
    
    // The server package is a bit noisy
    static {
        LoggerManipulation.quiet("org.jboss.threads");
        LoggerManipulation.quiet("org.xnio.nio");
        LoggerManipulation.quiet("io.undertow");
    }
    
    private Undertow server;
    
    @Override
    public Server start(ServerConfig config, Plugin.Application application) {
        
        HttpHandler handler = new UndertowHandler(application.router(), config);
        
        Undertow.Builder bob = Undertow.builder()
            .setBufferSize(config.bufferSize())
            /** Socket */
            .setSocketOption(Options.BACKLOG, config.backlog())
            /** Server */
            .setServerOption(UndertowOptions.ALWAYS_SET_DATE, config.serverDefaultHeaders())
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
            .setServerOption(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, true)
            .setServerOption(UndertowOptions.DECODE_URL, false)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
            .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 59 * 1000)
            //.setServerOption(UndertowOptions.ENABLE_HTTP2, true)
            /** Workers */
            .setIoThreads(config.ioThreads())
            .setWorkerThreads(config.workerThreads())
            /** Handler */
            .setHandler(handler)
            ;
        
        bob.addHttpListener(config.port(), config.host());
        
        server = bob.build();
        server.start();
        
        //handler.worker = server.getWorker();
        
        return this;
    }
    
    @Override
    public Server stop() {
        if (server != null) {
            try {
                server.stop();
            } finally {
                server = null;
            }
        }
        
        return this;
    }

}
