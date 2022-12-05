package net.javapla.jawn.core;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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
    
    
    // ** Route **
    //Route route;
    /*void route(Route route) {
        this.route = route;
    }
    Route route() {
        return route;
    }*/
    public Router.RoutePath routePath;
    
    
    
    // ** Framework specific shortcuts
    public abstract String requestHeader(String name);
    MediaType accept(List<MediaType> responseTypes) {
        if (responseTypes.isEmpty()) return null;
        
        String accept = requestHeader(ACCEPT);
        if (accept == null) {
            // We got no header, so just use the first of the possible response types
            return responseTypes.get(0);
        }
        
        
        // Example
        // Multiple types, weighted with the quality value syntax:
        // Accept: text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
        
        // TODO take weights into account
        // For now, we assume "Accept" is a prioritised list of MIME types
        
        List<MediaType> acceptable = MediaType.parse(accept);
        
        // Find appropriate type
        for (MediaType accepting : acceptable) {
            for (MediaType producing : responseTypes) {
                if (accepting.matches(producing)) return producing;
            }
        }
        
        return responseTypes.get(0);
    }
    
    
    
    
    protected abstract class AbstractRequest implements Context.Request {
        
        @SuppressWarnings("unchecked")
        @Override
        public <T> T parse(Type type) {
            try {
                return (T) routePath.route.parsers.get(contentType()).parse(AbstractContext.this, type);
            } catch (Exception e) {
                throw Up.ParseError("", e);
            }
        }
        
        @Override
        public Value pathParam(String name) {
            if (routePath.pathParameters == null) return Value.empty();
            return Value.of(routePath.pathParameters.get(name));
        }
    }
    
    protected abstract class AbstractResponse implements Context.Response {
        protected MediaType responseType = MediaType.PLAIN;
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
