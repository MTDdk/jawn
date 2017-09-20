package net.javapla.jawn.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class UndertowHandler implements HttpHandler {
    
    private final net.javapla.jawn.core.server.HttpHandler handler;
    private final String contextPath;

    public UndertowHandler(net.javapla.jawn.core.server.HttpHandler handler, String contextPath) {
        this.handler = handler;
        this.contextPath = contextPath;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        //exchange.getResponseHeaders().add(HttpString.tryFromString("Server"), "Undertow");
        
        handler.handle(new UndertowRequest(exchange, contextPath), new UndertowResponse(exchange));
    }
}
