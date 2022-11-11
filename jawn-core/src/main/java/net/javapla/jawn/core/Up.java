package net.javapla.jawn.core;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

// Exception
@SuppressWarnings("serial")
public abstract class Up extends RuntimeException {
    
    private final Status status;
    
    public Up(Status status, Throwable cause) {
        super(message(status, null), cause);
        this.status = status;
    }
    public Up(Status status, String msg) {
        super(message(status, msg));
        this.status = status;
    }
    public Up(Status status, String msg, Throwable cause) {
        super(message(status, msg), cause);
        this.status = status;
    }
    
    public Status status() {
        return status;
    }
    
    /*public static Up because(Throwable cause) {
        return new Up();
    }*/

    public static Up.IO IO(Throwable cause) {
        return new Up.IO(cause);
    }
    public static Up.RegistryException RegistryException(String msg) {
        return new Up.RegistryException(msg);
    }
    /*public static Up.RouteMissing RouteMissing(String path) {
        return new Up.RouteMissing(path);
    }*/
    public static Up ParseError(String msg, Throwable cause) {
        return new Up.ParseError(msg, cause);
    }
    public static Up ParseError(String msg) {
        return new Up.ParseError(msg);
    }
    public static Up.BadMediaType BadMediaType(String msg) {
        return new Up.BadMediaType(msg);
    }
    public static Up.UnsupportedMediaType UnsupportedMediaType(String msg) {
        return new Up.UnsupportedMediaType(msg);
    }
    public static Up.NotAcceptable NotAcceptable(String contentType) {
        return new Up.NotAcceptable(contentType);
    }
    /*public static Up.RouteFoundWithDifferentMethod RouteFoundWithDifferentMethod(String method) {
        return new Up.RouteFoundWithDifferentMethod(method);
    }*/
    public static Up.RouteAlreadyExists RouteAlreadyExists(String route) {
        return new Up.RouteAlreadyExists(route);
    }
    
    /**
     * Build an error message using the HTTP status.
     *
     * @param status The HTTP Status.
     * @param tail A message to append.
     * @return An error message.
     */
    private static String message(final Status status, final String tail) {
        return message(status.reason(), status.value(), tail);
    }

    /**
     * Build an error message using the HTTP status.
     *
     * @param reason Reason.
     * @param status The Status.
     * @param tail A message to append.
     * @return An error message.
     */
    private static String message(final String reason, final int status, final String tail) {
        return reason + "(" + status + ")" + (tail == null ? "" : ": " + tail);
    }
    
    public static boolean isFatal(Throwable x) {
        return x instanceof InterruptedException
            || x instanceof LinkageError
            || x instanceof ThreadDeath
            || x instanceof VirtualMachineError;
    }
    
    
    public static Status error(Throwable e) {
        if (e instanceof Up) return ((Up) e).status();
        if (e instanceof IllegalArgumentException || e instanceof NoSuchElementException) return Status.BAD_REQUEST;
        if (e instanceof FileNotFoundException) return Status.NOT_FOUND;
        return Status.SERVER_ERROR;
    }
    
    
    public static class IO extends Up {
        public IO(Throwable cause) {
            super(Status.SERVER_ERROR, cause);
        }
    }
    
    public static class RegistryException extends Up {
        public RegistryException(String msg) {
            super(Status.SERVER_ERROR, msg);
        }
        
        public RegistryException(String msg, Throwable cause) {
            super(Status.SERVER_ERROR, msg, cause);
        }
    }
    
    /*public static class RouteMissing extends Up {
        public RouteMissing( String msg) {
            super(Status.NOT_FOUND, msg);
        }
    }*/
    
    public static class ParseError extends Up {
        public ParseError(String msg, Throwable cause) {
            super(Status.UNPROCESSABLE_ENTITY, msg, cause);
        }
        public ParseError(String msg) {
            super(Status.UNPROCESSABLE_ENTITY, msg);
        }
    }
    
    public static class BadMediaType extends Up {
        public BadMediaType(String msg) {
            super(Status.BAD_REQUEST, msg);
        }
    }
    
    public static class UnsupportedMediaType extends Up {
        public UnsupportedMediaType(String message) {
            super(Status.UNSUPPORTED_MEDIA_TYPE, message);
        }
    }
    
    public static class NotAcceptable extends Up {
        public NotAcceptable(String contentType) {
            super(Status.NOT_ACCEPTABLE, contentType);
        }
    }
    
    /*public static class RouteFoundWithDifferentMethod extends Up {
        public RouteFoundWithDifferentMethod(String method) {
            super(Status.METHOD_NOT_ALLOWED, "Was looking for [" + method + "]");
        }
    }*/
    
    public static class RouteAlreadyExists extends Up {
        public RouteAlreadyExists(String route) {
            super(Status.ALREADY_REPORTED, "Found " + route);
        }
    }
}
