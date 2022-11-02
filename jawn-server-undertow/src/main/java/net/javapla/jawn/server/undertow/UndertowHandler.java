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
import net.javapla.jawn.core.HttpMethod;
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
        UndertowContext context = new UndertowContext(exchange);
        
        System.out.println(context.req().path());
        
        if (context.req().httpMethod() == HttpMethod.GET) {
            router.retrieve(context.req().httpMethod(), context.req().path()).execute(context);
        } else {
            // Might be a HTTP body
            
            FormDataParser parser = parserFactory.createParser(exchange);
            if (parser != null) {
                try {
                    parser.parse(execute(router, context));
                } catch (Exception e) {
                    context.resp().respond(Status.BAD_REQUEST);
                    // TODO log the error
                }
            } else {
                // TODO do some raw body parsing
            }
        }
    }
    
    private static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    private static HttpHandler execute(Router router, UndertowContext ctx) {
        return exchange -> router.retrieve(ctx.req().httpMethod(), ctx.req().path());
    }
}
