package net.javapla.jawn.core;

import java.text.MessageFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 * 
 * @author MTD
 */
//Results
public class ControllerResponseBuilder {

    private final ControllerResponseHolder holder;
    public ControllerResponseBuilder(ControllerResponseHolder holder) {
        this.holder = holder;
    }
    
    
    public static ControllerResponse ok() {
        return new ControllerResponse(Status.OK.getStatusCode());
    }
    public static ControllerResponse noContent() {
        return new ControllerResponse(Status.NO_CONTENT.getStatusCode()).renderable(new NoHttpBody());
    }
    public static ControllerResponse noBody(int status) {
        return new ControllerResponse(status).renderable(new NoHttpBody());
    }
    /**
     * 302 (Found)
     */
    public static ControllerResponse redirect() {
        return new ControllerResponse(Status.FOUND.getStatusCode()).renderable(new NoHttpBody());
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
        holder.setControllerResponse(response);
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
        holder.setControllerResponse(response);
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
        holder.setControllerResponse(response);
        response.contentType(MediaType.APPLICATION_XML).renderable(obj);
        return response;
    }
    
    ControllerResponseBuilder status(int statusCode) {
        holder.setControllerResponse(new ControllerResponse(statusCode));
        return this;
    }
    
    /**
     * Conveniently wraps status codes into simple method calls 
     * @return wrapper methods
     */
    public StatusWrapper status() {
        return new StatusWrapper(this);
    }
    
    /**
     * Conveniently wraps status codes into simple method calls
     * 
     * @author MTD
     */
    public class StatusWrapper {
        private final ControllerResponseBuilder builder;
        StatusWrapper(ControllerResponseBuilder builder) {
            this.builder = builder;
        }
        
        /**
         * 200 OK
         * @return 
         * @return The original builder
         */
        public ControllerResponseBuilder ok() {
//            this.builder.controllerResponse.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode());
//            return builder;
            builder.status(javax.ws.rs.core.Response.Status.OK.getStatusCode());
            return builder;
        }
        /**
         * 204 No Content
         * @return The original builder
         */
        public ControllerResponseBuilder noContent() {
            builder.status(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode());
            return builder;
        }
        /**
         * 400 Bad Request
         * @return The original builder
         */
        public ControllerResponseBuilder badRequest() {
            builder.status(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode());
            return builder;
        }
        /**
         * 404 Not Found
         * @return The original builder
         */
        public ControllerResponseBuilder notFound() {
            builder.status(javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode());
            return builder;
        }
        /**
         * 500 Internal Server Error
         * @return The original builder
         */
        public ControllerResponseBuilder internalServerError() {
            builder.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return builder;
        }
    }
}
