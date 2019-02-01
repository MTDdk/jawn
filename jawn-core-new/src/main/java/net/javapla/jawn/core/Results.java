package net.javapla.jawn.core;

import java.text.MessageFormat;

public abstract class Results {
    
    public static Result status(final Status status) {
        return new Result().status(status);
    }
    
    public static Result status(final int status) {
        return status(Status.valueOf(status));
    }
    
    public static Result ok() {
        return status(Status.OK);
    }
    
    public static Result noContent() {
        return status(Status.NO_CONTENT);
    }
    
    public static Result redirect(final String location) {
        return redirect(Status.FOUND, location);
    }
    
    public static Result temporaryRedirect(final String location) {
        return redirect(Status.TEMPORARY_REDIRECT, location);
    }
    
    public static Result moved(final String location) {
        return redirect(Status.MOVED_PERMANENTLY, location);
    }
    
    public static Result seeOther(final String location) {
        return redirect(Status.SEE_OTHER, location);
    }
    
    public static Result notFound() {
        return status(Status.NOT_FOUND);
    }
    
    public static Result error() {
        return status(Status.SERVER_ERROR);
    }
    
    
    public static View html() {
        return new View();
    }
    
    public static Result json(final Object entity) {
        return ok().renderable(entity).contentType(MediaType.JSON);
    }
    
    public static Result xml(final Object entity) {
        return ok().renderable(entity).contentType(MediaType.XML);
    }
    
    public static Result text(final Object entity) {
        if (entity instanceof String) {
            // TODO weeee might want to use getBytes(Charset)..
            return ok().renderable(((String) entity).getBytes()).contentType(MediaType.PLAIN);
        }
        return ok().renderable(entity).contentType(MediaType.PLAIN);
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
    public static Result text(String text, Object...objects) {
        return text(MessageFormat.format(text, objects));
    }
    
    public static Result ok(final Object entity) {
        return ok().renderable(entity);
    }

    
    private static Result redirect(final Status status, final String location) {
        return status(status).header("location", location);
    }
}
