package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

public interface TemplateEngine {
    
    static final String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "views");//"WEB-INF/views/");
    static final String LAYOUT_DEFAULT = "index.html";

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
    public void invoke(final Context context, final Response response, final ResponseStream stream) throws ViewException;

    /**
     * For instance returns ".ftl.html" Or .ftl.json.
     * <p>
     * Or anything else. To display error messages in a nice way...
     * <p>
     * But Gson for instance does not use a template to render stuff. Therefore
     * it will return null
     * 
     * @return
     *      name of suffix or null if engine is not using a template on disk.
     */
    public String getSuffixOfTemplatingEngine();

    /**
     * Get the content type this template engine renders
     * 
     * @return
     *      The content type this template engine renders
     */
    public String[] getContentType();

}