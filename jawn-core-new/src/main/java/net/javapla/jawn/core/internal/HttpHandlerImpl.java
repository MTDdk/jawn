package net.javapla.jawn.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.Request;
import net.javapla.jawn.core.server.Response;

@Singleton
class HttpHandlerImpl implements HttpHandler {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Injector injector;
    
    @Inject
    public HttpHandlerImpl(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) throws Exception {
        
    }

}
