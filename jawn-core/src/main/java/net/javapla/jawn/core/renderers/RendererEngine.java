package net.javapla.jawn.core.renderers;

import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;

public interface RendererEngine {
    
    /**
     * Write the response directly to the stream.
     * Used for retrieving the result without it being the final response to browser
     * 
     * @param context
     * @param response
     */
    byte[] invoke(final Context context, final Object renderable) throws Exception;
    
    
    /**
     * Get the content type this template engine renders
     * 
     * @return
     *      The content type this template engine renders
     */
    default MediaType[] getContentType() {
        return new MediaType[] { MediaType.TEXT };
    }
    
    
    /**
     * Simple toString renderer
     */
    RendererEngine TO_STRING = (ctx, obj) -> {
        if (ctx.req().accept(ctx.resp().contentType())) {
            return obj.toString().getBytes(StandardCharsets.UTF_8);
        }
        throw Up.BadMediaType.here(ctx.req().header("Accept").value());
    };
}