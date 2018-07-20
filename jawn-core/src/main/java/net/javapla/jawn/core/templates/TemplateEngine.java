package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

public interface TemplateEngine {
    
    String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "views");//"WEB-INF/views/");
    String LAYOUT_DEFAULT = "index.html";

    /**
     * Render the given object to the given context
     * 
     * @param context
     *      The context to render to
     */
    /*default void invoke(final Context context, final Response response) throws ViewException {
        invoke(context, response, context.finalizeResponse(response, false));
    }*/
    
    
    /**
     * Write the response directly to the stream.
     * Used for retrieving the result without it being the final response to browser
     * 
     * @param context
     * @param response
     * @param stream
     *      The stream to render to
     */
    void invoke(final Context context, final Result response, final ResponseStream stream) throws ViewException;


    /**
     * Get the content type this template engine renders
     * 
     * @return
     *      The content type this template engine renders
     */
    String[] getContentType();
//    public ContentType[] getContentType2();
    
    public static interface TemplateRenderEngine<T> extends TemplateEngine {
        /**
         * Let the template engine handle lookups of templates and in effect also caching hereof.
         * @param templatePath
         * @return
         */
        T readTemplate(String templatePath);
        
        /**
         * For instance returns .st, .ftl.html, or .ftl.json.
         * <p>
         * Or anything else. To display error messages in a nice way...
         * <p>
         * But Gson for instance does not use a template to render stuff. Therefore
         * it will return null
         * 
         * @return
         *      name of suffix or null if engine is not using a template on disk.
         */
        String getSuffixOfTemplatingEngine();
        
        /**
         * When using caches we want to be able to clone the cached value, so
         * nothing gets carried over when re-using the template
         * <p>
         * If the templates are re-usable out of the box, then no need to
         * override this
         * @param 
         *      cloneThis the template to be cloned
         * @return 
         *      the cloned template or simply <code>cloneThis</code> if nothing needs
         *      to be altered
         * 
         */
        default T clone(T cloneThis) { return cloneThis; } 
    }
    
}