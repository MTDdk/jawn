package net.javapla.jawn.core;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.javapla.jawn.core.Result.NoHttpBody;
import net.javapla.jawn.core.exceptions.PathNotFoundException;

/**
 * 
 * @author MTD
 */
//Results
public final class ResultBuilder {

    private final ResultHolder holder;
    public ResultBuilder(ResultHolder holder) {
        this.holder = holder;
    }
    
    
    public static Result ok() {
        return new Result(Status.OK.getStatusCode());//.renderable(new NoHttpBody());
    }
    public static Result noContent() {
        return new Result(Status.NO_CONTENT.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Result noBody(int status) {
        return new Result(status).renderable(new NoHttpBody());
    }
    public static Result status(int status) {
        return new Result(status);
    }
    public static Result notFound() {
        return new Result(Status.NOT_FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    
    public static Result text(String text, int status) {
        return new Result(status).renderable(text).contentType(MediaType.TEXT_PLAIN);
    }
    
    /** 302 (Found) */
    static Result redirect() {
        return new Result(Status.FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    /** 302 (Found) */
    public static Result redirect(String location) {
        return redirect().addHeader("Location", location);
    }
    
    
    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    public Result text(String text) {
        Result response = ok();
        holder.setControllerResult(response);
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
    public Result text(String text, Object...objects) {
        return text(MessageFormat.format(text, objects));
    }
    
    public Result text(byte[] text) {
        Result response = ok();
        holder.setControllerResult(response);
        response.contentType(MediaType.TEXT_PLAIN).renderable(text);
        return response;
    }
    public Result text(char[] text) {
        Result response = ok();
        holder.setControllerResult(response);
        response.contentType(MediaType.TEXT_PLAIN).renderable(text);
        return response;
    }
    
    /**
     * This method will send a JSON response to the client.
     * It will not use any layouts.
     * Use it to build app.services and to support AJAX.
     * 
     * @param obj
     * @return {@link Result}, to accept additional information. The response is automatically
     * has its content type set to "application/json"
     */
    public final Result json(Object obj) {
        final Result response = ok().contentType(MediaType.APPLICATION_JSON).renderable(obj);
        holder.setControllerResult(response);
        return response;
    }
    public final Result json(Callable<Object> c) {
        final Result response = ok().contentType(MediaType.APPLICATION_JSON).renderable(c);
        holder.setControllerResult(response);
        return response;
    }
    public final Result json(byte[] arr) {
        final Result response = ok().contentType(MediaType.APPLICATION_JSON).renderable(arr);
        holder.setControllerResult(response);
        return response;
    }
    
    /**
     * This method will send a XML response to the client.
     * It will not use any layouts.
     * Use it to build app.services.
     * 
     * @param obj
     * @return {@link Result}, to accept additional information. The response is automatically
     * has its content type set to "application/xml"
     */
    public Result xml(Object obj) {
        Result response = ok();
        holder.setControllerResult(response);
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
    public Result sendFile(File file) throws PathNotFoundException {
        try{
            Result r = ResultBuilder.ok()
                    .addHeader("Content-Disposition", "attachment; filename=" + file.getName())
                    .renderable(new FileInputStream(file))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
            holder.setControllerResult(r);
            return r;
        }catch(Exception e){
            throw new PathNotFoundException(e);
        }
    }
    
    ResultBuilder setStatus(int statusCode) {
        holder.setControllerResult(new Result(statusCode));
        return this;
    }
    ResultBuilder setNoContent() {
        holder.setControllerResult(noContent());
        return this;
    }
    ResultBuilder setNotFound() {
        holder.setControllerResult(notFound());
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
    public static class StatusWrapper {
        private final ResultBuilder builder;
        StatusWrapper(ResultBuilder builder) {
            this.builder = builder;
        }
        
        /**
         * 200 OK
         * @return 
         * @return The original builder
         */
        public ResultBuilder ok() {
//            this.builder.controllerResponse.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode());
//            return builder;
            builder.setStatus(Status.OK.getStatusCode()).setNoContent();
            return builder;
        }
        /**
         * 204 No Content
         * @return The original builder
         */
        public ResultBuilder noContent() {
            builder.setNoContent();
            return builder;
        }
        /**
         * 400 Bad Request
         * @return The original builder
         */
        public ResultBuilder badRequest() {
            builder.setStatus(Status.BAD_REQUEST.getStatusCode());
            return builder;
        }
        /**
         * 404 Not Found
         * @return The original builder
         */
        public ResultBuilder notFound() {
            builder.setNotFound();
            return builder;
        }
        /**
         * 401 Unauthorized
         * @return The original builder
         */
        public ResultBuilder unauthorized() {
            builder.setStatus(Status.UNAUTHORIZED.getStatusCode());
            return builder;
        }
        /**
         * 500 Internal Server Error
         * @return The original builder
         */
        public ResultBuilder internalServerError() {
            builder.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return builder;
        }
        
        public ResultBuilder code(int statuscode) {
            builder.setStatus(statuscode);
            return builder;
        }
    }
}
