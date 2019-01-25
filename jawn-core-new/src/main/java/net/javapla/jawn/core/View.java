package net.javapla.jawn.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class View extends Result {

    
    //TODO convert this to be a part of the renderable
    //renderable(Map<>), renderable(String key, Object value), renderable(Entry<String, Object)
    //Keep state of the renderable at all times - never overwrite, just add
    protected final HashMap<String, Object> viewModel = new HashMap<>();
    
    private String template = "index";
    
    //README perhaps this ought to be a boolean, as it is solely used as a flag whether to use the 
    //defacto layout or not
    private String layout = null;//"index.html";//Configuration.getDefaultLayout();
    
    private String templatePath = "";
    
    protected View() {
        contentType(MediaType.HTML);
        super.renderable(this);
    }
    
    public View put(final String name, final Object value) {
        viewModel.put(name, value);
        return this;
    }
    
    public View put(final Map<String, Object> values) {
        viewModel.putAll(values);
        return this;
    }
    
    public Map<String, Object> model() {
        return Collections.unmodifiableMap(viewModel);
    }
    
    /**
     * It is up to the caller to handle template suffixes such as .html, .st, or .ftl.html
     * @return
     */
    public String template() {
        return template;
    }
    
    public View template(String template) {
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
    
    public View layout(String layout) {
        this.layout = layout;
        return this;
    }
    
    public String templatePath() {
        return templatePath;
    }
    
    public View templatePath(String path) {
        this.templatePath = path;
        return this;
    }
    
    @Override
    public Result renderable(Object content) {
        throw new UnsupportedOperationException("Not allowed in views, use one of the put methods.");
    }
}
