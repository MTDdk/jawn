package net.javapla.jawn.core;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.javapla.jawn.core.Result.NoHttpBody;

public class Results {
    
    public static final Result status(int status) {
        return new Result(status);
    }
    public static Result ok() {
        return status(Status.OK.getStatusCode());
    }
    public static Result noContent() {
        return status(Status.NO_CONTENT.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Result noBody(int status) {
        return status(status).renderable(new NoHttpBody());
    }
    public static Result notFound() {
        return status(Status.NOT_FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Result unauthorized() {
        return status(Status.UNAUTHORIZED.getStatusCode()).renderable(new NoHttpBody());
    }
    public static Result internalServerError() {
        return status(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    /** 302 (Found) */
    static Result redirect() {
        return status(Status.FOUND.getStatusCode()).renderable(new NoHttpBody());
    }
    /** 302 (Found) */
    public static Result redirect(String location) {
        return redirect().addHeader("Location", location);
    }
    
    public static Result html() {
        return ok().contentType(MediaType.TEXT_HTML);
    }
    public static Result text() {
        return ok().contentType(MediaType.TEXT_PLAIN);
    }
    public static Result text(String t) {
        return ok().contentType(MediaType.TEXT_PLAIN).renderable(t);
    }
    public static Result text(byte[] t) {
        return ok().contentType(MediaType.TEXT_PLAIN).renderable(t);
    }
    public static Result xml() {
        return ok().contentType(MediaType.APPLICATION_XML);
    }
    public static Result json() {
        return ok().contentType(MediaType.APPLICATION_JSON);
    }
    public static Result json(Object o) {
        return ok().contentType(MediaType.APPLICATION_JSON).renderable(o);
    }
    
    public static final Result gzip() {
        return null; //TODO
    }
    
    //TODO asynchronous responses
}
