package net.javapla.jawn.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Protocols;

class UndertowHandler implements HttpHandler {

    
    private final net.javapla.jawn.core.server.HttpHandler dispatcher;

    UndertowHandler(final net.javapla.jawn.core.server.HttpHandler dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            HeaderValues upgrade = exchange.getRequestHeaders().get(Headers.UPGRADE);
            if (upgrade != null && upgrade.contains("h2c")) { // HTTP/2 over TCP - https://http2.github.io/http2-spec/#versioning
                // reset protocol
                exchange.setProtocol(Protocols.HTTP_1_1);
            }
            exchange.dispatch(this);
            return;
        }
        
        dispatcher.handle(new UndertowRequest(exchange), new UndertowResponse(exchange));
    }

}
