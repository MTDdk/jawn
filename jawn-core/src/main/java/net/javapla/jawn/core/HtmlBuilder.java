package net.javapla.jawn.core;

import java.util.Map;

import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.util.CollectionUtil;

public class HtmlBuilder {

    
    /**
     * Assigns value that will be passed into view.
     *
     * @param name name of object to be passed to view
     * @param value object to be passed to view
     */
    protected HtmlBuilder view(String name, Object value) {
//        response.addViewObject(name, value);
        
        return this;
    }


    /**
     * Convenience method, calls {@link #view(String, Object)} internally.
     * The keys in the map are converted to String values.
     *
     * @param values map with values to pass to view.
     */
    protected HtmlBuilder view(Map<String, Object> values){
        for(String key:values.keySet() ){
            view(key, values.get(key));
        }
        
        return this;
    }
    
    @SuppressWarnings("unchecked")
    protected HtmlBuilder view(Object value) {
        if (value instanceof Map) {
            view((Map<String, Object>)value);
        } else if (value instanceof String) {
            view(value, value);
        } else if (value instanceof Object[]) {
            throw new ControllerException("Name of the value could not be guessed");
        } else {
            view(value.getClass().getSimpleName(), value);
        }
        
        return this;
    }
    
    /**
     * Convenience method, calls {@link #view(Map)} internally.
     *
     * @param values An even list of key/value pairs
     */
    protected HtmlBuilder view(Object ... values) {
        view(CollectionUtil.map(values));
        
        return this;
    }

    /**
     * Convenience method, takes in a map of values to flash.
     *
     * @see #flash(String, Object)
     *
     * @param values values to flash.
     */
    protected HtmlBuilder flash(Map<String, Object> values){
        for(Object key:values.keySet() ){
            flash(key.toString(), values.get(key));
        }
        
        return this;
    }

    /**
     * Convenience method, takes in a vararg of values to flash.
     * Number of values must be even.
     *
     * @see #flash(String, Object)
     * @param values values to flash.
     */
    protected HtmlBuilder flash(Object ... values){
        flash(CollectionUtil.map(values));
        
        return this;
    }

    /**
     * Sets a flash name for a flash with  a body.
     * Here is a how to use a tag with a body:
     *
     * <i>flash</i> keyword
     *
     * <pre>
     * $if(flash.&lt;name&gt;)$
     *   &lt;div class=&quot;warning&quot;&gt;$flash.&lt;name&gt;$&lt;/div&gt;
     * $endif$
     * </pre>
     *
     * If body refers to variables (as in this example), then such variables need to be passed in to the template as usual using
     * the {@link #view(String, Object)} method.
     *
     * @param name name of a flash
     */
    protected HtmlBuilder flash(String name){
        flash(name, name);
        
        return this;
    }
    
    /**
     * Sends value to flash. Flash survives one more request.  Using flash is typical
     * for POST->REDIRECT->GET pattern,
     *
     * @param name name of value to flash
     * @param value value to live for one more request in current session.
     */
    protected HtmlBuilder flash(String name, Object value) {
//        context.setFlash(name, value);
        
        return this;
    }
}
