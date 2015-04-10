package net.javapla.jawn.templates;

import net.javapla.jawn.Context;
import net.javapla.jawn.ControllerResponse;
import net.javapla.jawn.ResponseStream;
import net.javapla.jawn.exceptions.ViewException;

public interface TemplateEngine {

    /**
     * Render the given object to the given context
     * 
     * @param context
     *            The context to render to
     * @param result
     *            The result to render
     */
    public void invoke(Context context, ControllerResponse response/*, Result result*/) throws ViewException;
    
    
    /**
     * Write the response directly to the stream.
     * Used for retrieving the result without it being the final response to browser
     * 
     * @param context
     * @param response
     * @param stream
     *            The stream to render to
     */
    public void invoke(Context context, ControllerResponse response, ResponseStream stream);

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
    public String[] getContentType();

}