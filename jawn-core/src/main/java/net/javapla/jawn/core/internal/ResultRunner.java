package net.javapla.jawn.core.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

@Singleton
final class ResultRunner {
    
    private final RendererEngineOrchestrator engines;
    
    @Inject
    ResultRunner(final RendererEngineOrchestrator engines) {
        this.engines = engines;
    }
    
    
    void execute(final Result result, final ContextImpl context) throws Up.BadMediaType, Up.ViewError {
        //if (context.resp().committed()) return;
        
        //context.readyResponse(result);
        readyResponse(result, context);
    
        if (HttpMethod.HEAD == context.req().httpMethod()) {
            //context.end();
            return;
        }
        
        result.renderable().ifPresent(renderable -> {
            engines.getRendererEngineForContentType(result.contentType(), engine -> invoke(engine, context, renderable));
        });
        
        //context.end(); // TODO could this be placed in context.done() ?
    }
    
    private void readyResponse(final Result result, final ContextImpl context) {
        if (!context.resp().committed()) {
            context.resp().contentType(result.contentType());
            
            context.resp().status(result.status().value());
            
            result.charset()
                .ifPresent(context.resp()::charset);
            
            result.headers().
                ifPresent(map -> map.forEach(context.resp()::header));
            
            context.writeCookies();
        }
    }
    
    private void invoke(final RendererEngine engine, final Context context, final Object renderable) {
        try {
            engine.invoke(context, renderable);
        } catch (Up e) {
            // nothing - just rethrow
            // this is to not wrap the exception in Up.RenderableError as below 
            throw e;
        } catch (Exception e) {
            throw new Up.RenderableError(e);
        }
    }
}
