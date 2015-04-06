package net.javapla.jawn.templates;

import net.javapla.jawn.Context;
import net.javapla.jawn.NewControllerResponse;
import net.javapla.jawn.ResponseStream;

public interface TemplateEngine {

    /**
     * Render the given object to the given context
     * 
     * @param context
     *            The context to render to
     * @param result
     *            The result to render
     */
    public void invoke(Context context, NewControllerResponse response/*, Result result*/);
    
    
    /**
     * Write the response directly to the stream.
     * Used for retrieving the result without it being the final response to browser
     * 
     * @param context
     * @param response
     * @param stream
     *            The stream to render to
     */
    public void invoke(Context context, NewControllerResponse response, ResponseStream stream);

    /**
     * For instance returns ".ftl.html" Or .ftl.json.
     * <p>
     * Or anything else. To display error messages in a nice way...
     * <p>
     * But Gson for instance does not use a template to render stuff. Therefore
     * it will return null
     * 
     * @return name of suffix or null if engine is not using a template on disk.
     */
    public String getSuffixOfTemplatingEngine();

    /**
     * Get the content type this template engine renders
     * 
     * @return The content type this template engine renders
     */
    public String getContentType();

}