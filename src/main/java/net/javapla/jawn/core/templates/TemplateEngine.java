package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.ControllerResponse;
import net.javapla.jawn.core.ResponseStream;
import net.javapla.jawn.core.exceptions.ViewException;

public interface TemplateEngine {
    
    static final String TEMPLATE_DEFAULT = "index.html";
    static final String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "WEB-INF/views/");

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
    public void invoke(Context context, ControllerResponse response, ResponseStream stream) throws ViewException;

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