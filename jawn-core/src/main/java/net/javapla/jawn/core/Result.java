package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

public class Result {

    /**
     * Http status code
     */
    private int statusCode;
    
    /**
     * Object to be handled by a rendering engine
     */
    private Object renderable;
    
    /**
     * Something like: "text/html" or "application/json"
     */
    private String contentType;
    
    /**
     * Something like: "utf-8" => will be appended to the content-type. eg
     * "text/html; charset=utf-8"
     */
    private String charset;
    
    /**
     * A list of content types this result will handle. If you got a general
     * person object you can render it via application/json and application/xml
     * without changing anything inside your controller for instance.
     */
    private final List<String> supportedContentTypes;
    
    private final Map<String, String> headers;
    
    //TODO convert this to be a part of the renderable
    //renderable(Map<>), renderable(String key, Object value), renderable(Entry<String, Object)
    //Keep state of the renderable at all times - never overwrite, just add
    private Map<String, Object> viewObjects;
    
    private String template;
    
    //README perhaps this ought to be a boolean, as it is solely used as a flag whether to use the 
    //defacto layout or not
    private String layout = "index.html";//Configuration.getDefaultLayout();
    
    public Result(int statusCode) {
        supportedContentTypes = new ArrayList<>();
        headers = new HashMap<>();
        viewObjects = new HashMap<>();
        
//        charset = Constants.ENCODING;
        this.statusCode = statusCode;
    }
    
    
    public Result renderable(Object obj) {
        this.renderable = obj;
        return this;
    }
    private static final int cpuCount = Runtime.getRuntime().availableProcessors();

    // TODO: parameterize multipliers
    private final static ExecutorService BoundedFixedThreadPool =
      new ThreadPoolExecutor(
        cpuCount, cpuCount * 2, 100, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(cpuCount * 20), //TODO: do some calculations on the best configuration for this
        new ThreadPoolExecutor.AbortPolicy());
    public Result renderable(Callable<Object> c) {
        try {
            this.renderable = BoundedFixedThreadPool.submit(c);
            return this;
        } catch (RejectedExecutionException e) {
            // if the thread pool is filled
            // throw an error to tell the client that the server is busy
            return ResultBuilder.status(Status.SERVICE_UNAVAILABLE.getStatusCode());
        }
    }
    public Object renderable() {
        if (renderable instanceof Future) {
            try {
                return ((Future<?>) renderable).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return renderable;
    }
    
    
    public Result contentType(String type) {
        this.contentType = type;
        return this;
    }
    public String contentType() {
        return contentType;
    }
    
    public Result status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }
    public int status() {
        return statusCode;
    }
    
    public Result charset(String charset) {
        this.charset = charset;
        return this;
    }
    public String charset() {
        return charset;
    }
    
    
    /**
     * It is up to the caller to handle template suffixes such as .html, .st, or .ftl.html
     * @return
     */
    public String template() {
        return template;
    }
    public Result template(String template) {
        this.template = template;
        return this;
    }
    
    /**
     * It is up to the caller to handle template suffixes such as .html, .st, or ftl.html.
     * @return Layout is allowed to be null, if it is not desired to look for a layout for the template
     */
    public String layout() {
        return layout;
    }
    public Result layout(String layout) {
        this.layout = layout;
        return this;
    }
    
    public Map<String, String> headers() {
        return headers;
    }
    public Result addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }
    
    public Result addViewObject(String name, Object value) {
        viewObjects.put(name, value);
        return this;
    }
    public Result addAllViewObjects(Map<String, Object> values) {
        viewObjects.putAll(values);
        return this;
    }
    public Map<String, Object> getViewObjects() {
        return viewObjects;
    }
    
    
    public Result addSupportedContentType(String contentType) {
        this.supportedContentTypes.add(contentType);
        return this;
    }
    public boolean supportsContentType(String contentType) {
        return supportedContentTypes.contains(contentType);
    }
    public List<String> supportedContentTypes() {
        return supportedContentTypes;
    }
    
    
    @Override
    public String toString() {
        return String.format("%d, %s %s", status(), layout(), renderable().getClass());
    }
    
    
    /**
     * Empty container for rendering purposes.
     * 
     * <p>
     * It causes the {@link ResultRunner} to render no body, just the header. Useful
     * when issuing a redirect and no corresponding content should be shown.
     * 
     * @author MTD
     */
    public static class NoHttpBody {
        // intentionally left empty. Just a marker class.
    }
    
}
