package net.javapla.jawn.server.undertow;

import java.nio.charset.StandardCharsets;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Protocols;
import net.javapla.jawn.core.Config;

class UndertowHandler implements HttpHandler {

    private final Config config;
    private final net.javapla.jawn.core.server.HttpHandler dispatcher;
    
    private final FormParserFactory parserFactory;

    UndertowHandler(final Config config, final net.javapla.jawn.core.server.HttpHandler dispatcher) {
        this.config = config;
        this.dispatcher = dispatcher;
        
        /** Eager body parsing: */
        parserFactory = FormParserFactory.builder(false)
            .addParser(new MultiPartParserDefinition(UndertowRequest.TMP_DIR)
                .setDefaultEncoding(StandardCharsets.UTF_8.name()))
            .addParser(new FormEncodedDataDefinition()
                .setDefaultEncoding(StandardCharsets.UTF_8.name()))
            .build();
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
        
        FormDataParser parser = parserFactory.createParser(exchange);
        if (parser != null) {
            parser.parse(this::handle);
            //parser.close(); // TODO when to close this?
        } else {
//            Receiver receiver = exchange.getRequestReceiver();
//            receiver.receiveFullBytes((ex, bytes) -> );
//            
            handle(exchange);
        }
    }

    private final void handle(final HttpServerExchange exchange) throws Exception {
        dispatcher.handle(new UndertowRequest(config, exchange), new UndertowResponse(exchange));
    }
}
