package net.javapla.jawn.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public class UndertowHandler implements HttpHandler {
    
    private final net.javapla.jawn.core.server.HttpHandler handler;

    public UndertowHandler(net.javapla.jawn.core.server.HttpHandler handler) {
        
        this.handler = handler;
        
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        //exchange.getResponseHeaders().add(HttpString.tryFromString("Server"), "Undertow");
        
        handler.handle(new UndertowRequest(exchange), new UndertowResponse(exchange));
    }

}
