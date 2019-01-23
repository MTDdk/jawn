package net.javapla.jawn.core.internal;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

@Singleton
final class HttpHandlerImpl implements HttpHandler {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Charset charset;
    private final Router router;
    private final ResultRunner runner;
    
    @Inject
    HttpHandlerImpl(final Charset charset, final Router router, final ResultRunner runner) {
        this.charset = charset;
        this.router = router;
        this.runner = runner;
    }

    @Override
    public void handle(final ServerRequest req, final ServerResponse resp) throws Exception {
        String uri = normaliseURI(req.path());
        resp.header("Server", "jawn");
        
        
        final ContextImpl context = new ContextImpl(req, resp, charset);
        try {
            Route route = router.retrieve(req.method(), uri);
            context.route(route);
            
            // Execute handler
            runner.execute(/*result*/route.handle(context), context);
        
        } catch (Up.RouteMissing e) {
            // 404
            if (e.path.equals("/favicon.ico")) {
                runner.execute(Results.status(Status.NOT_FOUND), context);
            } else {
                renderSystemError(context, /*"/system/404", "index",*/ 404, e);
            }
        } /*catch (ViewException e) {
            // 501
            renderSystemError(context, "/system/500", "index", 501, e);
        }*/ catch (Up.BadResult | Up.BadMediaType e) {
            // 400
            renderSystemError(context, /*"/system/400", "index",*/ 400, e);
        } /*catch (WebException e){
            renderSystemError(context, "/system/"+e.getHttpCode(), "index", e.getHttpCode(), e);
        }*/ catch (Exception e) {
            // 500
            renderSystemError(context, /*"/system/500", "index",*/ 500, e);
        }
        
    }
    
    private static String normaliseURI(final String uri) {
        return uri.length() == 0 ? "/" : uri;
    }
    
    void renderSystemError(final ContextImpl context, final int status, Throwable e) {
        runner.execute(Results.status(Status.valueOf(status)), context);
        
        if (status == 404) {
            logger.info("404 [{}]", context.req().path());
        } else {
            logger.error("{} [{}] ", status, context.req().path(), e);
        }
    }
}
