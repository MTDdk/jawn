package net.javapla.jawn.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import net.javapla.jawn.core.Route.PostResponse;

public abstract class AbstractContext implements Context {

    // session
    // flash
    // attributes
    
    
    private HashMap<String, Object> attributes;
    
    private void instantiateAttributes() {
        if (attributes == null) attributes = new HashMap<>(5);
    }
    
    private Object attributeOrNull(final String name) {
        if (attributes == null || attributes.isEmpty()) return null;
        return attributes.get(name);
    }
    
    @Override
    public void attribute(final String name, final Object value) {
        instantiateAttributes();
        attributes.put(name, value);
    }
    
    @Override
    public Optional<Object> attribute(final String name) {
        return Optional.ofNullable(attributeOrNull(name));
    }
    
    @Override
    public void removeAttribute(final String name) {
        if (attributes != null) attributes.remove(name);
    }
    
    
    /* Session  */
    
    
    
    
    
    protected abstract class AbstractResponse implements Context.Response {
        
        @Override
        public void postResponse(PostResponse task) {
            if (onComplete == null) onComplete = new LinkedList<>();
            onComplete.add(task);
        }
    }
    
    private LinkedList<Route.PostResponse> onComplete;
    protected void onComplete() {
        if (onComplete != null)
            onComplete.forEach(action -> action.onComplete(this, null));
    }
}
