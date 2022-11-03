package net.javapla.jawn.server.undertow;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.Status;

public class UndertowHandler implements HttpHandler {
    
    private final Router router;
    
    private final FormParserFactory parserFactory;



    public UndertowHandler(Router router) {
        this.router = router;
        
        parserFactory = FormParserFactory.builder(false)
            .addParser(new MultiPartParserDefinition(TMP_DIR)
                .setDefaultEncoding(StandardCharsets.UTF_8.name()))
            .addParser(new FormEncodedDataDefinition()
                .setDefaultEncoding(StandardCharsets.UTF_8.name()))
            .build();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(() -> {
                
                UndertowContext context = new UndertowContext(exchange);
                
                if (!context.req().httpMethod().mightContainBody) {
                    
                    router.retrieveAndExecute(context);
                    
                } else {
                    // Might include a HTTP Form body
        
                    long len = exchange.getRequestContentLength();
                    if (len > 0) {
                        // With the existence of Content-Length, we assume body present
                        
                            
                        /**
                         * Eagerly parsing Form data
                         * @see io.undertow.server.handlers.form.EagerFormParsingHandler
                         */
                        FormDataParser parser = parserFactory.createParser(exchange);
                        if (parser != null) {
                            try (parser) {
                                parser.parse(execute(router, context));
                            } catch (Exception e) {
                                context.resp().respond(Status.BAD_REQUEST);
                                // TODO log the error
                            }
                        } else {
                            // TODO do some raw body parsing
                            
                            /*Receiver receiver = exchange.getRequestReceiver();
                            if (len)*/
                            router.retrieveAndExecute(context);
                        }
                        
                    } else {
                        
                        // Apparently no body
                        // Just execute route
                        router.retrieveAndExecute(context);
                        
                    }
                }
            });
        }
    }
    
    private static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    private static HttpHandler execute(Router router, UndertowContext ctx) {
        return exchange -> router.retrieveAndExecute(ctx);
    }
}
