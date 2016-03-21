package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

//Result
public class Response {

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
    
    public Response(int statusCode) {
        supportedContentTypes = new ArrayList<>();
        headers = new HashMap<>();
        viewObjects = new HashMap<>();
        
//        charset = Constants.ENCODING;
        this.statusCode = statusCode;
    }
    
    
    public Response renderable(Object obj) {
        this.renderable = obj;
        return this;
    }
    /*private static final int cpuCount = Runtime.getRuntime().availableProcessors();

    // todo: parameterize multipliers
    private final static ExecutorService EXECUTOR =
      new ThreadPoolExecutor(
        cpuCount, cpuCount * 2, 200, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(cpuCount * 100),
        new ThreadPoolExecutor.CallerRunsPolicy());
    public Response renderable(Callable<Object> c) {
        this.renderable = EXECUTOR.submit(c);
        return this;
    }*/
    public Object renderable() {
        if (renderable instanceof Future)
            try {
                return ((Future<?>) renderable).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        return renderable;
    }
    
    
    public Response contentType(String type) {
        this.contentType = type;
        return this;
    }
    public String contentType() {
        return contentType;
    }
    
    public Response status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }
    public int status() {
        return statusCode;
    }
    
    public Response charset(String charset) {
        this.charset = charset;
        return this;
    }
    public String charset() {
        return charset;
    }
    
    
    public Response template(String template) {
        this.template = template;
        if (template.endsWith(".st"))
            this.template = template.substring(0, template.length()-3);//FIXME this ought to be up to the templateengine to remove
        return this;
    }
    public String template() {
        return template;
    }
    
    public Response layout(String layout) {
        this.layout = layout;
        if (layout != null) {
            if (layout.endsWith(".st"))
                this.layout = layout.substring(0, layout.length()-3);
            if (!this.layout.endsWith(".html"))
                this.layout += ".html";
        }
        return this;
    }
    public String layout() {
        return layout;
    }
    
    public Map<String, String> headers() {
        return headers;
    }
    public Response addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }
    
    public Response addViewObject(String name, Object value) {
        viewObjects.put(name, value);
        return this;
    }
    public Response addAllViewObjects(Map<String, Object> values) {
        viewObjects.putAll(values);
        return this;
    }
    public Map<String, Object> getViewObjects() {
        return viewObjects;
    }
    
    
    public Response addSupportedContentType(String contentType) {
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
     * It causes the {@link ResponseRunner} to render no body, just the header. Useful
     * when issuing a redirect and no corresponding content should be shown.
     * 
     * @author MTD
     */
    public static class NoHttpBody {
        // intentionally left empty. Just a marker class.
    }
    
}
