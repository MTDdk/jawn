package net.javapla.jawn.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Result {
    
    /**
     * Http status code
     */
    protected Status status;
    
    /**
     * Something like: "text/html" or "application/json"
     */
    protected MediaType contentType = MediaType.PLAIN;

    /**
     * Object to be handled by a rendering engine
     */
    protected Object renderable;
    
    /**
     * Something like: "utf-8" => will be appended to the content-type. eg
     * "text/html; charset=utf-8"
     */
    protected String charset = "UTF-8";
    
    /**
     * A list of content types this result will handle. If you got a general
     * person object you can render it via application/json and application/xml
     * without changing anything inside your controller for instance.
     */
    //protected final List<String> supportedContentTypes = new ArrayList<>();
    
    // TODO README: using a hashmap of course prevents us from having multiple values for a single header
    // this could be implemented with a map<string, object> instead, where object CAN be an iterable,
    // and as such set a list of values in the response
    protected final Map<String, String> headers = new HashMap<>();
    
    public Optional<Status> status() {
        return Optional.ofNullable(status);
    }
    
    public Result status(final int status) {
        return status(Status.valueOf(status));
    }
    
    public Result status(final Status status) {
        this.status = status;
        return this;
    }
    
    public Optional<MediaType> contentType() {
        return Optional.ofNullable(contentType);
    }
    
    public Result contentType(final MediaType type) {
        this.contentType = type;
        return this;
    }
    
    public Result contentType(final String type) {
        return contentType(MediaType.valueOf(type));
    }

    public Optional<Object> renderable() {
        return Optional.ofNullable(renderable);
    }
    
    public Result renderable(final Object content) {
        this.renderable = content;
        return this;
    }
    
    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }
    
    public Result header(final String name, final String value) {
        headers.put(name, value);
        return this;
    }
    
}
