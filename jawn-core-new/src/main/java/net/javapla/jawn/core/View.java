package net.javapla.jawn.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class View extends Result {

    
    //TODO convert this to be a part of the renderable
    //renderable(Map<>), renderable(String key, Object value), renderable(Entry<String, Object)
    //Keep state of the renderable at all times - never overwrite, just add
    protected final HashMap<String, Object> viewModel = new HashMap<>();
    
    /*private String template = "index";
    
    //README perhaps this ought to be a boolean, as it is solely used as a flag whether to use the 
    //defacto layout or not
    private String layout = "index.html";//Configuration.getDefaultLayout();*/
    
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
    
    @Override
    public Result renderable(Object content) {
        throw new UnsupportedOperationException("Not allowed in views, use one of the put methods.");
    }
}
