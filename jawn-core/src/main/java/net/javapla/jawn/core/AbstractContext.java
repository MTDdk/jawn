package net.javapla.jawn.core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

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
    public Context attribute(final String name, final Object value) {
        instantiateAttributes();
        attributes.put(name, value);
        return this;
    }
    
    @Override
    public Optional<Object> attribute(final String name) {
        return Optional.ofNullable(attributeOrNull(name));
    }
    
    @Override
    public Context removeAttribute(final String name) {
        if (attributes != null) attributes.remove(name);
        return this;
    }
    
    
    /* Session  */
    
    
    
    
    
    /*protected abstract class AbstractRequest implements Context.Request {
        
        @Override
        public Body body() {
            return null;
        }
    }*/
    
    protected abstract class AbstractResponse implements Context.Response {
        protected MediaType responseType = MediaType.TEXT;
        protected MediaType defaultResponseType = null;
        protected Charset cs = StandardCharsets.UTF_8;
        
        
        @Override
        public Response rendererContentType(MediaType type) {
            defaultResponseType = type;
            return this;
        }
        
        
        /*@Override
        public void postResponse(PostResponse task) {
            if (onComplete == null) onComplete = new LinkedList<>();
            onComplete.add(task);
        }*/
    }
    
    /*private LinkedList<Route.PostResponse> onComplete;
    protected void onComplete() {
        if (onComplete != null)
            onComplete.forEach(action -> action.onComplete(this, null));
    }*/
    
}
