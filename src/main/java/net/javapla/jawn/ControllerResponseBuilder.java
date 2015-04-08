package net.javapla.jawn;

import java.text.MessageFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 * 
 * @author MTD
 */
//Results
public class ControllerResponseBuilder {

    private final Context context;
    public ControllerResponseBuilder(Context context) {
        this.context = context;
    }
    
    
//    public static NewControllerResponse status(int code) {
//        return new NewControllerResponse().status(code);
//    }
    public static ControllerResponse ok() {
        return new ControllerResponse(Status.OK.getStatusCode());
    }
    public static ControllerResponse noContent() {
        return new ControllerResponse(Status.NO_CONTENT.getStatusCode()).renderable(new NoHttpBody());
    }
    
    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    public ControllerResponse text(String text) {
        ControllerResponse response = ok();
        context.setControllerResponse(response);
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
    public ControllerResponse text(String text, Object...objects) {
        return text(MessageFormat.format(text, objects));
    }
    
    /**
     * This method will send a JSON response to the client.
     * It will not use any layouts.
     * Use it to build app.services and to support AJAX.
     * 
     * @param obj
     * @return {@link ControllerResponse}, to accept additional information. The response is automatically
     * has its content type set to "application/json"
     */
    public ControllerResponse json(Object obj) {
        ControllerResponse response = ok();
        context.setControllerResponse(response);
        response.contentType(MediaType.APPLICATION_JSON).renderable(obj);
        return response;
    }
    
    /**
     * This method will send a XML response to the client.
     * It will not use any layouts.
     * Use it to build app.services.
     * 
     * @param obj
     * @return {@link ControllerResponse}, to accept additional information. The response is automatically
     * has its content type set to "application/xml"
     */
    public ControllerResponse xml(Object obj) {
        ControllerResponse response = ok();
        context.setControllerResponse(response);
        response.contentType(MediaType.APPLICATION_XML).renderable(obj);
        return response;
    }
    
    public ControllerResponseBuilder status(int statusCode) {
        context.setControllerResponse(new ControllerResponse(statusCode));
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
