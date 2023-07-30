package net.javapla.jawn.core;

/**
 * Rendering of responses
 *
 */
public interface Renderer {
    
//    Renderer TO_STRING = (ctx, value) -> {
//        if (ctx.req().accepts(MediaType.PLAIN/*ctx.resp().contentType()*/)) {
//            return value.toString().getBytes(ctx.resp().charset());
//        }
//        throw Up.NotAcceptable(ctx.req().header("Accept").value());
//    };
    

    /**
     * 
     * @param ctx
     * @param value
     * @return <code>null</code> if the response has been handled by the renderer
     * @throws Exception
     */
    byte[] render(Context ctx, Object value) throws Exception;

}
