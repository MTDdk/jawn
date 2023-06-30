package net.javapla.jawn.core.internal;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.TypeLiteral;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.internal.reflection.RouteClassAnalyser;

abstract class Pipeline {
    
    private Pipeline() {}
    
    
    
    static Route compile(RouteClassAnalyser analyser, ParserRenderEngine engine, Route.Builder bob) {
         
        // Add parsers to later be available in Context.
        // Unfortunately we have to let them be chained 
        // through Route.Builder -> Route -> AbstractContext
        bob.parsers(engine);
        
        // if the route has multiple possible response types, 
        // we want to look at the request's ACCEPT-header to pick one of them for us.
        if (bob.produces().size() > 1) {
            bob.before(Route.RESPONSE_CONTENT_TYPE.apply(bob.produces()));
        } else {
            // If only a single option is available then always set response type accordingly.
            bob.before(ctx -> ctx.resp().contentType(bob.fallbackResponseType()));
        }
        
        // TODO insert Parsers somewhere, so they are reachable from handlers
        // which probably means they have to be reachable from Context
        
        //bob.renderer(engine.render(bob.fallbackResponseType()));

        
        // pipeline
        pipeline(bob, analyser, engine);
        
        
        if (bob.originalHandler instanceof WebSocket.WebSocketHandler) {
            // some things are just for WS
        } else {
            // When everything else is set, look for request header "Content-Type"
            // and make sure it is what the route expects
            if (bob.consumes() != MediaType.WILDCARD) {
                bob.before(Route.CONTENT_TYPE_SUPPORTED);
                // TODO needs to be tested
            }
        }
            
        return bob.build();
    }
    
    private static void pipeline(Route.Builder bob, RouteClassAnalyser analyser, ParserRenderEngine engine) {
        Type returnType = bob.returnType();
        if (returnType == null) {
            returnType = analyser.returnType(bob.originalHandler);
        }
        
        Class<?> raw = TypeLiteral.rawType(returnType);
        
        bob.execution(execution(raw, bob, engine));
    }
    
    private static Route.Execution execution(Class<?> raw, final Route.Builder bob, final ParserRenderEngine engine) {
        
        /* Bytes */
        if (byte[].class == raw) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    Object result = handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond((byte[])result);
                    }
                } catch (Exception e) {
                    ctx.error(e);
                }
            };
        }
        if (ByteBuffer.class.isAssignableFrom(raw)) {
            final Route.Handler handler = bob.handler();
            return ctx -> {
                try {
                    Object result = handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond((ByteBuffer)result);
                    }
                } catch (Exception e) {
                    ctx.error(e);
                }
            };
        }
        
        /* void */
        if (void.class == raw) {
            final Route.Handler handler = bob.handler();
            
            final Status status = bob.method == HttpMethod.DELETE ? Status.NO_CONTENT : Status.OK;
            
            return ctx -> {
                try {
                    handler.handle(ctx);
                    if (!ctx.resp().isResponseStarted()) {
                        ctx.resp().respond(status);
                    }
                } catch (Exception e) {
                    ctx.error(e);
                }
            };
            
        }
        
        return defaultExecution(bob, engine);
    }
    
    private static Route.Execution defaultExecution(Route.Builder bob, final ParserRenderEngine engine) {
        final Route.Handler handler = bob.handler();
        //final Renderer renderer = bob.renderer();
        
        return ctx -> {
            
            try {
                Object result = handler.handle(ctx);
                
                if (!ctx.resp().isResponseStarted()) {
                    if (result instanceof Context) {
                        ctx.resp().respond(Status.OK);
                    } else {
                        byte[] rendered = engine.render(ctx.resp().contentType()).render(ctx, result);
                        if (rendered != null) {
                            System.out.println("Response has not been handled");
                            ctx.resp().respond(Status.NO_CONTENT);
                        }
                    }
                }
            } catch (Exception e ) {
                ctx.error(e);
            }
        };
    }

}
