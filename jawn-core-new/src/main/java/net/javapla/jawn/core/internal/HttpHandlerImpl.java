package net.javapla.jawn.core.internal;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.Request;
import net.javapla.jawn.core.server.Response;

@Singleton
class HttpHandlerImpl implements HttpHandler {
    
    private Injector injector;
    
    @Inject
    public HttpHandlerImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        
    }

}
