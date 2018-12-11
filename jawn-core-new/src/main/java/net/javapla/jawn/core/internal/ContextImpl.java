package net.javapla.jawn.core.internal;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.server.Request;
import net.javapla.jawn.core.server.Response;

class ContextImpl implements Context {
    
    private final Request req;
    private final Response resp;
    
    public ContextImpl(final Request req, final Response resp) {
        this.req = req;
        this.resp = resp;
    }
    
    

    @Override
    public HttpMethod httpMethod() {
        return req.method();
    }

    void readyResponse() {
        
    }
}
