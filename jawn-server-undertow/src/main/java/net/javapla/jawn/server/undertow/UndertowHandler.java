package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import io.undertow.io.Receiver;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.ExchangeCompletionListener.NextListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import net.javapla.jawn.core.Body;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.Server.ServerConfig;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;

public class UndertowHandler implements HttpHandler {
    
    private final Router router;
    private final int bufferSize;
    private final long maxRequestSize;
    private final boolean addDefaultHeaders;
    
    private final FormParserFactory parserFactory;

    public UndertowHandler(Router router, ServerConfig config) {
        this.router = router;
        this.bufferSize = config.bufferSize();
        this.maxRequestSize = config.maxRequestSize();
        this.addDefaultHeaders = config.serverDefaultHeaders();
        
        this.parserFactory = FormParserFactory.builder(false)
            .addParser(new MultiPartParserDefinition(TMP_DIR)
                .setDefaultEncoding(StandardCharsets.UTF_8.name()))
            .addParser(new FormEncodedDataDefinition()
                .setDefaultEncoding(StandardCharsets.UTF_8.name()))
            .build();
        
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
                
        UndertowContext context = new UndertowContext(exchange);
        
        HeaderMap headers = exchange.getResponseHeaders();
        headers.put(Headers.CONTENT_TYPE, Context.Response.STANDARD_HEADER_CONTENT_TYPE);
        if (addDefaultHeaders) {
            headers.add(Headers.SERVER, "Jawn/U");
        }
        
        if (!context.method.mightContainBody) {
            
            router.retrieve(context.method.ordinal(), context.path).execute(context);
            
        } else {
            // Might include a body

            long len = exchange.getRequestContentLength();
            if (len > 0) {
                // With the existence of Content-Length, we assume body present
                
                
                // If the request is either "multipart/form-data" or "application/x-www-form-urlencoded"
                // the parserFactory will provide us with a form data parser
                FormDataParser parser = parserFactory.createParser(exchange);
                if (parser != null) {
                    try (parser) {
                        // Eagerly parsing Form data
                        // @see io.undertow.server.handlers.form.EagerFormParsingHandler
                        parser.parse(execute(router, context));
                    } catch (Exception e) {
                        context.resp().respond(Status.BAD_REQUEST);
                        // TODO log the error
                    }
                    
                } else {
                    
                    // Apparently the body was not form data
                    // Read the entire thing, and we will deal with it later
                    
                    Receiver receiver = exchange.getRequestReceiver();
                    
                    if (len > 0 && len <= bufferSize) {
                        receiver.receiveFullBytes(UndertowHandler.receiveFullBytes(context));
                    } else {
                        receiver.receivePartialBytes(new PartialBodyReceiver(context));
                    }
                    
                    router.retrieve(context.method.ordinal(), context.path).execute(context);
                }
                
            } else {
                
                // Apparently no body
                // Just execute route
                router.retrieve(context.method.ordinal(), context.path).execute(context);
                
            }
        }
    }
    
    private static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    private static HttpHandler execute(Router router, UndertowContext context) {
        return exchange -> router.retrieve(context.method.ordinal(), context.path).execute(context);
    }
    
    private static Receiver.FullBytesCallback receiveFullBytes(UndertowContext context) {
        return (HttpServerExchange exchange, byte[] bytes) -> {
            context.body = Body.of(bytes);
        };
    }
    
    private static ExchangeCompletionListener exchangeEvent(final Path file) {
        return (HttpServerExchange exchange, NextListener next) -> {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // ignore
            } finally {
                next.proceed();
            }
        }; 
    }
    
    private final class PartialBodyReceiver implements Receiver.PartialBytesCallback {
        
        private final UndertowContext context;
        
        int accumulated;
        LinkedList<byte[]> chunks = new LinkedList<>();
        Path file;
        FileChannel channel;
        
        PartialBodyReceiver(UndertowContext context) {
            this.context = context;
        }

        @Override
        public void handle(HttpServerExchange exchange, byte[] chunk, boolean last) {
            try {
                if (chunk.length > 0) {
                    accumulated += chunk.length;
                    
                    // Body too big! Explosions!
                    if (accumulated > maxRequestSize) {
                        context.resp().status(Status.REQUEST_ENTITY_TOO_LARGE);
                        // TODO log error
                        
                        closeChannel();
                        return;
                    }

                    // buffer the chunk
                    chunks.add(chunk);
                    
                    // buffer size overflow
                    // the sent body is starting to become bigger than the buffer
                    // save to temp file
                    if (accumulated > bufferSize) {
                        
                        if (file == null) {
                            file = TMP_DIR.resolve("jawn" + System.nanoTime() + "body");
                            channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        }
                        
                        // write the saved chunks of the body to file
                        saveBuffer();
                    }
                    
                    if (last) {
                        // temp file created
                        if (channel != null) {
                            if (accumulated > 0) {
                                // do the final write of the buffer
                                saveBuffer();
                            }
                            
                            exchange.addExchangeCompleteListener(exchangeEvent(file));
                            channel.force(true);
                            closeChannel();
                            
                            context.body = Body.of(file);
                            
                        } else {
                            context.body = Body.of(toByteArray());
                        }
                    }
                }
            } catch (IOException e) {
                context.resp().status(Up.error(e));
                closeChannel();
                exchange.endExchange();
                return;
            }
        }
        
        private void saveBuffer() throws IOException {
            for (byte[] c : chunks) {
                channel.write(ByteBuffer.wrap(c));
            }
            
            // release the memory of the received body so far
            // this will let us buffer the incoming body chunks before writing to temp file again
            chunks.clear();
            accumulated = 0;
        }
        
        private void closeChannel() {
            if (channel != null) {
                try {
                    channel.close();
                } catch(IOException e) {
                    // welp..
                }
                channel = null;
            }
        }
        
        private byte[] toByteArray() {
            byte[] bytes = new byte[accumulated];
            int offset = 0;
            for (byte[] chunk : chunks) {
                System.arraycopy(chunk, 0, bytes, offset, chunk.length);
                offset += chunk.length;
            }
            return bytes;
        }
        
    }
}
