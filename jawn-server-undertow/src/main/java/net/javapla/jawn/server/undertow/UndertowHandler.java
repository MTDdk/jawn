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
        
        // TODO do this in a runnable (test)
        handler.handle(new UndertowRequest(exchange, contextPath), new UndertowResponse(exchange));
        
        //TODO probably extend Undertow's HttpHandler to have an exception callback or something
        /*exchange.dispatch(() -> {
            try {
                handler.handle(new UndertowRequest(exchange, contextPath), new UndertowResponse(exchange));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/
    }
}
