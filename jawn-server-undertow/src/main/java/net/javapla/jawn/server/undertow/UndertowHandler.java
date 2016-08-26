package net.javapla.jawn.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class UndertowHandler implements HttpHandler {
    
    private final net.javapla.jawn.server.api.HttpHandler handler;

    public UndertowHandler(net.javapla.jawn.server.api.HttpHandler handler) {
        
        this.handler = handler;
        
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        
        handler.handle(new UndertowRequest(exchange), new UndertowResponse(exchange));
    }

}
