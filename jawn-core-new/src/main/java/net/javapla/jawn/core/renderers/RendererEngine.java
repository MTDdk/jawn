package net.javapla.jawn.core.renderers;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;

public interface RendererEngine {
    
    /**
     * Write the response directly to the stream.
     * Used for retrieving the result without it being the final response to browser
     * 
     * @param context
     * @param response
     */
    void invoke(final Context context, final Object renderable) throws Exception;


    /**
     * Get the content type this template engine renders
     * 
     * @return
     *      The content type this template engine renders
     */
    MediaType[] getContentType();
    
}