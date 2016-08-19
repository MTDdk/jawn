package net.javapla.jawn.core;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.javapla.jawn.core.Response.NoHttpBody;

public class Responses {
    
    public static final Response status(int status) {
        return new Response(status);
    }
    public static Response ok() {
        return status(Status.OK.getStatusCode());
    }
    public static Response noContent() {
        return status(Status.NO_CONTENT.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Response noBody(int status) {
        return status(status).renderable(new NoHttpBody());
    }
    public static Response notFound() {
        return status(Status.NOT_FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Response unauthorized() {
        return status(Status.UNAUTHORIZED.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Response internalServerError() {
        return status(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    /** 302 (Found) */
    static Response redirect() {
        return status(Status.FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    /** 302 (Found) */
    public static Response redirect(String location) {
        return redirect().addHeader("Location", location);
    }
    
    public static Response html() {
        return ok().contentType(MediaType.TEXT_HTML);
    }
    public static Response text() {
        return ok().contentType(MediaType.TEXT_PLAIN);
    }
    public static Response text(String t) {
        return ok().contentType(MediaType.TEXT_PLAIN).renderable(t);
    }
    public static Response text(byte[] t) {
        return ok().contentType(MediaType.TEXT_PLAIN).renderable(t);
    }
    public static Response xml() {
        return ok().contentType(MediaType.APPLICATION_XML);
    }
    public static Response json() {
        return ok().contentType(MediaType.APPLICATION_JSON);
    }
    public static Response json(Object o) {
        return ok().contentType(MediaType.APPLICATION_JSON).renderable(o);
    }
    
    public static final Response gzip() {
        return null; //TODO
    }
    
    //TODO asynchronous responses
}
