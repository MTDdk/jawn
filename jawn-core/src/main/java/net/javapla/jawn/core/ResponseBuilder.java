package net.javapla.jawn.core;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.javapla.jawn.core.Response.NoHttpBody;
import net.javapla.jawn.core.exceptions.PathNotFoundException;

/**
 * 
 * @author MTD
 */
//Results
public class ResponseBuilder {

    private final ResponseHolder holder;
    public ResponseBuilder(ResponseHolder holder) {
        this.holder = holder;
    }
    
    
    public static Response ok() {
        return new Response(Status.OK.getStatusCode());
    }
    public static Response noContent() {
        return new Response(Status.NO_CONTENT.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Response noBody(int status) {
        return new Response(status).renderable(new NoHttpBody());
    }
    public static Response status(int status) {
        return new Response(status);
    }
    
    public static Response text(String text, int status) {
        return new Response(status).renderable(text).contentType(MediaType.TEXT_PLAIN);
    }
    
    /** 302 (Found) */
    public static Response redirect() {
        return new Response(Status.FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    /** 302 (Found) */
    public static Response redirect(String location) {
        return redirect().addHeader("Location", location);
    }
    
    
    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    public Response text(String text) {
        Response response = ok();
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
    public Response text(String text, Object...objects) {
        return text(MessageFormat.format(text, objects));
    }
    
    /**
     * This method will send a JSON response to the client.
     * It will not use any layouts.
     * Use it to build app.services and to support AJAX.
     * 
     * @param obj
     * @return {@link Response}, to accept additional information. The response is automatically
     * has its content type set to "application/json"
     */
    public Response json(Object obj) {
        Response response = ok();
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
     * @return {@link Response}, to accept additional information. The response is automatically
     * has its content type set to "application/xml"
     */
    public Response xml(Object obj) {
        Response response = ok();
        holder.setControllerResponse(response);
        response.contentType(MediaType.APPLICATION_XML).renderable(obj);
        return response;
    }
    
    /**
     * Convenience method for downloading files. This method will force the browser to find a handler(external program)
     *  for  this file (content type) and will provide a name of file to the browser. This method sets an HTTP header
     * "Content-Disposition" based on a file name.
     *
     * @param file file to download.
     * @return builder instance.
     * @throws PathNotFoundException thrown if file not found.
     */
    public Response sendFile(File file) throws PathNotFoundException {
        try{
            Response r = ResponseBuilder.ok()
                    .addHeader("Content-Disposition", "attachment; filename=" + file.getName())
                    .renderable(new FileInputStream(file))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
            holder.setControllerResponse(r);
            return r;
        }catch(Exception e){
            throw new PathNotFoundException(e);
        }
    }
    
    ResponseBuilder setStatus(int statusCode) {
        holder.setControllerResponse(new Response(statusCode));
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
        private final ResponseBuilder builder;
        StatusWrapper(ResponseBuilder builder) {
            this.builder = builder;
        }
        
        /**
         * 200 OK
         * @return 
         * @return The original builder
         */
        public ResponseBuilder ok() {
//            this.builder.controllerResponse.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode());
//            return builder;
            builder.setStatus(Status.OK.getStatusCode());
            return builder;
        }
        /**
         * 204 No Content
         * @return The original builder
         */
        public ResponseBuilder noContent() {
            builder.setStatus(Status.NO_CONTENT.getStatusCode());
            return builder;
        }
        /**
         * 400 Bad Request
         * @return The original builder
         */
        public ResponseBuilder badRequest() {
            builder.setStatus(Status.BAD_REQUEST.getStatusCode());
            return builder;
        }
        /**
         * 404 Not Found
         * @return The original builder
         */
        public ResponseBuilder notFound() {
            builder.setStatus(Status.NOT_FOUND.getStatusCode());
            return builder;
        }
        /**
         * 500 Internal Server Error
         * @return The original builder
         */
        public ResponseBuilder internalServerError() {
            builder.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return builder;
        }
    }
}
