package net.javapla.jawn.core;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public class Up extends RuntimeException {
    
    public static class BadMediaType extends Up {
        public BadMediaType(final String message) {
            super(Status.BAD_REQUEST, message);
        }
    }

    public static class Missing extends Up {
        public Missing(final String message) {
            super(Status.BAD_REQUEST, message);
        }
    }
    
    public static class ParsableError extends Up {
        public ParsableError(final Throwable err) {
            super(Status.UNPROCESSABLE_ENTITY, err);
        }
        public ParsableError(final String message) {
            super(Status.UNSUPPORTED_MEDIA_TYPE, message);
        }
    }
    
    public static class RenderableError extends Up {
        public RenderableError(final Throwable err) {
            super(Status.SERVER_ERROR, err);
        }
    }
    
    public static class ViewError extends Up {
        public ViewError(final Throwable err) {
            super(Status.SERVER_ERROR, err);
        }
        
        public ViewError(final String message) {
            super(Status.SERVER_ERROR, message);
        } 
    }
    
    public static class BadResult extends Up {
        public BadResult() {
            super(Status.SERVER_ERROR);
        }
        
        public BadResult(String msg) {
            super(Status.SERVER_ERROR, msg);
        }
    }
    
    public static class Compilation extends Up {
        public Compilation(final Throwable err) {
            super(Status.SERVER_ERROR, err);
        }
    }
    
    public static class UnloadableClass extends Up {
        public UnloadableClass(final Throwable err) {
            super(Status.SERVER_ERROR, err);
        }
        
        public UnloadableClass(final String msg, final Throwable err) {
            super(Status.SERVER_ERROR, msg);
        }
    }
    
    public static class IO extends Up {
        public IO(final Throwable err) {
            super(Status.SERVER_ERROR, err);
        }
        
        public IO(final String msg) {
            super(Status.SERVER_ERROR, msg);
        }
    }
    
    public static class RouteMissing extends Up {
        public final String path;
        public RouteMissing(final String path, final String msg) {
            super(Status.NOT_FOUND, msg);
            this.path = path;
        }
    }
    
    public static class RouteFoundWithDifferentMethod extends Up {
        public RouteFoundWithDifferentMethod(HttpMethod lookingFor) {
            super(Status.METHOD_NOT_ALLOWED, "Was looking for ["+lookingFor+"]");
        }
    }
    
    private final int statusCode;
    
    public Up(final Status status, final String message, final Throwable cause) {
        super(message(status, message), cause);
        this.statusCode = status.value();
    }
    
    public Up(final int status, final String message, final Throwable cause) {
        super(message("", status, message), cause);
        this.statusCode = status;
    }
    
    public Up(final Status status, final String message) {
        super(message(status, message));
        this.statusCode = status.value();
    }
    
    public Up(final int status, final String message) {
        this(Status.valueOf(status), message);
    }
    
    public Up(final Status status, final Throwable cause) {
        super(message(status, null), cause);
        this.statusCode = status.value();
    }
    
    public Up(final int status, final Throwable cause) {
        this(Status.valueOf(status), cause);
    }
    
    public Up(final Status status) {
        super(message(status, null));
        this.statusCode = status.value();
    }

    public Up(final int status) {
        this(Status.valueOf(status));
    }
    
    public int statusCode() {
        return statusCode;
    }
    
    /**
     * Build an error message using the HTTP status.
     *
     * @param status The HTTP Status.
     * @param tail A message to append.
     * @return An error message.
     */
    private static String message(final Status status, @Nullable final String tail) {
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
    private static String message(final String reason, final int status, @Nullable  final String tail) {
        return reason + "(" + status + ")" + (tail == null ? "" : ": " + tail);
    }
}
