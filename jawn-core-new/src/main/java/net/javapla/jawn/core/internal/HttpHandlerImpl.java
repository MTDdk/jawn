package net.javapla.jawn.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

@Singleton
class HttpHandlerImpl implements HttpHandler {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Injector injector;
    private final Router router;
    
    @Inject
    public HttpHandlerImpl(final Injector injector, final Router router) {
        this.injector = injector;
        this.router = router;
    }

    @Override
    public void handle(final ServerRequest req, final ServerResponse resp) throws Exception {
        String uri = normaliseURI(req.path());
        
        Route route = router.retrieve(req.method(), uri);
        ContextImpl context = new ContextImpl(req, resp, route);
        
        
    }
    
    private static String normaliseURI(final String uri) {
        return uri.length() == 0 ? "/" : uri;
    }

}
