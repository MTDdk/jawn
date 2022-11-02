package net.javapla.jawn.core.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Renderer;
import net.javapla.jawn.core.Status;

public class ResponseRenderer implements Renderer {
    
    private Map<MediaType, Renderer> renderers = new HashMap<>();
    
    public ResponseRenderer add(MediaType type, Renderer renderer) {
        
        if (renderer instanceof Renderer.TemplateRenderer) {
            
        } else {
            renderers.put(type, renderer);
        }
        
        return this;
    }

    @Override
    public byte[] render(Context ctx, Object value) throws Exception {
        
        if (value instanceof Status) {
            ctx.resp().respond((Status) value);
            return null;
        }
        
        if (value instanceof InputStream) {
            ctx.resp().respond((InputStream) value);
            return null;
        }
        
        // String, CharSequence, Number
        ctx.resp().respond(TO_STRING.render(ctx, value));
        return null;//TO_STRING.render(ctx, value);
        
        
        // Java 16
        /*if (value instanceof Status s) {
            ctx.resp().respond(s);
            return null;
        }*/
        
        // Java 17
        /*return switch (value) {
            case CharSequence s -> Renderer.TO_STRING.render(ctx, value);
            default -> Renderer.TO_STRING.render(ctx, value);
        }*/
    }
    
    Renderer TO_STRING = (ctx, value) -> {
        return value.toString().getBytes(ctx.resp().charset());
    };

}
