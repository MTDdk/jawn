package net.javapla.jawn.core.internal.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Protocols;
import net.javapla.jawn.core.Config;

public class UndertowHandler implements HttpHandler {

    
    private final net.javapla.jawn.core.server.HttpHandler dispatcher;
    private final Config config;

    UndertowHandler(final net.javapla.jawn.core.server.HttpHandler dispatcher, final Config config) {
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            HeaderValues upgrade = exchange.getRequestHeaders().get(Headers.UPGRADE);
            if (upgrade != null && upgrade.contains("h2c")) {
                // reset protocol
                exchange.setProtocol(Protocols.HTTP_1_1);
            }
            exchange.dispatch(this);
            return;
        }
        
        dispatcher.handle(new UndertowRequest(exchange, config), new UndertowResponse(exchange));
    }

}
