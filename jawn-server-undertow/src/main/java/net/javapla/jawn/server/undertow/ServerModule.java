package net.javapla.jawn.server.undertow;

import org.xnio.Options;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import net.javapla.jawn.core.Module;
import net.javapla.jawn.core.Server;

public class ServerModule implements Server {
    
    private Undertow server;
    
    @Override
    public Server start(ServerConfig config, Module.Application application) {
        
        HttpHandler handler = new UndertowHandler(application.router());
        
        Undertow.Builder bob = Undertow
            .builder()
            /** Socket */
            .setSocketOption(Options.BACKLOG, config.backlog())
            /** Server */
            .setServerOption(UndertowOptions.ALWAYS_SET_DATE, config.serverDefaultHeaders())
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
            .setServerOption(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, true)
            .setServerOption(UndertowOptions.DECODE_URL, false)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
            /** Workers */
            .setIoThreads(config.ioThreads())
            /** Handler */
            .setHandler(handler)
            ;
        
        bob.addHttpListener(config.port(), config.host());
        
        
        server = bob.build();
        server.start();
        
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
