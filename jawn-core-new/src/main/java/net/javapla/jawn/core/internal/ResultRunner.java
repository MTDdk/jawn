package net.javapla.jawn.core.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

@Singleton
final class ResultRunner {
    
    private final RendererEngineOrchestrator engines;
    
    @Inject
    ResultRunner(final RendererEngineOrchestrator engines) {
        this.engines = engines;
    }
    
    
    void execute(final Result result, final ContextImpl context) {
        
        context.readyResponse(result);
    
        if (HttpMethod.HEAD == context.req().httpMethod()) {
            context.end();
            return;
        }
        
        result.renderable().ifPresent(renderable -> {
            final MediaType type = result.contentType().orElseThrow(() -> new Err(Status.NOT_ACCEPTABLE, "Could not find any suitable way to serve you your content"));
            
            engines.getRendererEngineForContentType(type, engine -> invoke(engine, context, renderable));
        });
        
        context.end();
    }
    
    private void invoke(final RendererEngine engine, final Context context, final Object renderable) {
        try {
            engine.invoke(context, renderable);
        } catch (Exception e) {
            throw new Err.RenderableError(e);
        }
    }
}
