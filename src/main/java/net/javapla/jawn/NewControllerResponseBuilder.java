package net.javapla.jawn;

import java.text.MessageFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 * 
 * @author MTD
 */
//Results
public class NewControllerResponseBuilder {

    private final Context context;
    public NewControllerResponseBuilder(Context context) {
        this.context = context;
    }
    
    
//    public static NewControllerResponse status(int code) {
//        return new NewControllerResponse().status(code);
//    }
    public static NewControllerResponse ok() {
        return new NewControllerResponse(Status.OK.getStatusCode());
    }
    public static NewControllerResponse noContent() {
        return new NewControllerResponse(Status.NO_CONTENT.getStatusCode());
    }
    
    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    public NewControllerResponse text(String text) {
        NewControllerResponse response = ok();
        context.setNewControllerResponse(response);
        response.contentType(MediaType.TEXT_PLAIN).renderable(text);
        return response;
    }
    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     * 
     * @param text A string containing &quot;{index}&quot;, like so: &quot;Message: {0}, error: {1}&quot;
     * @param objects A varargs of objects to be put into the <code>text</code>
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     * @see MessageFormat#format
     */
    public NewControllerResponse text(String text, Object...objects) {
        return text(MessageFormat.format(text, objects));
    }
    
    /**
     * This method will send a JSON response to the client.
     * It will not use any layouts.
     * Use it to build app.services and to support AJAX.
     * 
     * @param obj
     * @return {@link NewControllerResponse}, to accept additional information. The response is automatically
     * has its content type set to "application/json"
     */
    public NewControllerResponse json(Object obj) {
        NewControllerResponse response = ok();
        context.setNewControllerResponse(response);
        response.contentType(MediaType.APPLICATION_JSON).renderable(obj);
        return response;
    }
    
    /**
     * This method will send a XML response to the client.
     * It will not use any layouts.
     * Use it to build app.services.
     * 
     * @param obj
     * @return {@link NewControllerResponse}, to accept additional information. The response is automatically
     * has its content type set to "application/xml"
     */
    public NewControllerResponse xml(Object obj) {
        NewControllerResponse response = ok();
        context.setNewControllerResponse(response);
        response.contentType(MediaType.APPLICATION_XML).renderable(obj);
        return response;
    }
    
    public NewControllerResponseBuilder status(int statusCode) {
        context.setNewControllerResponse(new NewControllerResponse(statusCode));
        return this;
    }
    
    /**
     * Conveniently wraps status codes into simple method calls 
     * @return wrapper methods
     */
    public StatusWrapper status() {
        return new StatusWrapper(this);
    }
}
