package net.javapla.jawn.core.internal;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.After;
import net.javapla.jawn.core.Route.Before;
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
    private final DeploymentInfo deploymentInfo;
    private final Injector injector;

    
    @Inject
    HttpHandlerImpl(final Charset charset, final Router router, final ResultRunner runner, final DeploymentInfo deploymentInfo, final Injector injector) {
        System.out.println("  00  " + HttpHandlerImpl.class.getName());
        this.charset = charset;
        this.router = router;
        this.runner = runner;
        this.deploymentInfo = deploymentInfo;
        this.injector = injector;
    }

    @Override
    public void handle(final ServerRequest req, final ServerResponse resp) throws Exception {
        //String uri = normaliseURI(context.req().path()); README is this even necessary?
        resp.header("Server", "jawn");
        
//        System.out.println("HttpHandlerImpl#handle " + injector.get().hashCode());
        final ContextImpl context = new ContextImpl(req, resp, charset, deploymentInfo, injector);
        
        // ServerRequest.path() holds the actual path received on the server,
        // so from this point onward the Context.req().path() is preferred, as this
        // takes contextPath into account
        
        try {
            final Route route = router.retrieve(req.method(), context.req().path()/*uri*/);
            context.route(route);
            
            Result result = null;
            int i = 0;
            
            //TODO after having moved the handling of befores and afters in here again
            // some design choices needs to be reconsidered. 
            
            
            // Before filters
            Before[] befores = route.before();
            if (befores != null) {
                do {
                    result = befores[i].before(context, () -> null);
                } while (result == null && ++i < befores.length);
            }
            
            // Execute
            if (result == null) {
                result = route.handle(context);
                if (result == null) throw new Up.BadResult("The execution of the route itself rendered no result");
                
                // Execute handler
                runner.execute(result, context);
            }
            
            // After filters
            After[] afters = route.after();
            if (afters != null) {
                Result r = result; // <-- TODO like this
                for (i = 0; i < afters.length; i++) {
                    r = afters[i].after(context, r);
                }
                
                // TODO and this
                if (r == null) throw new Up.BadResult("A ("+ Route.After.class.getSimpleName() +") filter rendered a 'null' result");
                /*if (r != null) */result = r;
            }
            
            
        
        } catch (Up.RouteMissing e) {
            // 404
            if (e.path.equals("/favicon.ico")) {
                runner.execute(Results.status(Status.NOT_FOUND), context);
            } else {
                renderSystemError(context, /*"/system/404", "index",*/ 404, e);
            }
        } catch (Up.ViewError e) {
            // 501
            renderSystemError(context, /*"/system/500", "index",*/ 501, e);
        } catch (Up.BadResult | Up.BadMediaType e) {
            // 400
            renderSystemError(context, /*"/system/400", "index",*/ 400, e);
        } catch (Up e) {
            // catch-all for known exceptions
            renderSystemError(context, /*"/system/"+e.getHttpCode(), "index",*/ e.statusCode(), e);
        } catch (Exception e) {
            // catch-all for UN-known exceptions
            // 500
            renderSystemError(context, /*"/system/500", "index",*/ 500, e);
        }
        
    }
    
    /*private static String normaliseURI(final String uri) {
        return uri.length() == 0 ? "/" : uri;
    }*/
    
    void renderSystemError(final ContextImpl context, final int status, Throwable e) {
        runner.execute(Results.status(Status.valueOf(status)), context);
        
        if (status == 404) {
            logger.info("404 [{}]", context.req().path());
        } else {
            logger.error("{} [{}] ", status, context.req().path(), e);
        }
    }
}
