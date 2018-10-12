package net.javapla.jawn.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.api.Router;
import net.javapla.jawn.core.exceptions.BadRequestException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.exceptions.WebException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.util.CollectionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

final class FrameworkEngine {
    
    private static final String FRAMEWORK_SPLASH = "\n" 
            + "     ____.  _____  __      _________   \n"
            + "    |    | /  _  \\/  \\    /  \\      \\  \n"
            + "    |    |/  /_\\  \\   \\/\\/   /   |   \\ \n"
            + "/\\__|    /    |    \\        /    |    \\ \n"
            + "\\________\\____|__  /\\__/\\  /\\____|__  /\n"
            + "  web framework  \\/      \\/         \\/ http://www.javapla.net\n";

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Router router;
    private final ResultRunner runner;

//    private final Lang lang;
    
    @Inject
    public FrameworkEngine(Router router, ResultRunner runner/*Lang lang*/) {
        this.router = router;
        this.runner = runner;
//        this.lang = lang;
    }
    
    public final void onRouteRequest(Context.Internal2 context) {
        String uri = context.request().path();
        if (uri.length() == 0) {
            System.err.println("different servlet implementations, damn");
            uri = "/";//different servlet implementations, damn.
        }
        
        try {

            final Route route = router.retrieveRoute(context.request().method(), uri);
            context.setRouteInformation(route, uri);
            
            // run pre-filters
            Result result = route.getFilterChain().before(context);
            
            // a filter might return a result, in which case do nothing
            if (result == null)
                result = route.executeRouteAndRetrieveResult(context);
            
            // close response streams in the end
            // README: is it possible that it never closes?
            try (ResponseStream rsp = runner.run(context, result)) {
                
                // run post-filters
                route.getFilterChain().after(context);
            }
            
        } catch (RouteException e) {
            // 404
            renderSystemError(context, "/system/404", "index", 404, e);
        } catch (ViewException e) {
            // 501
            renderSystemError(context, "/system/500", "index", 501, e);
        } catch (BadRequestException | MediaTypeException e) {
            // 400
            renderSystemError(context, "/system/400", "index", 400, e);
        } catch (WebException e){
            renderSystemError(context, "/system/"+e.getHttpCode(), "index", e.getHttpCode(), e);
        } catch (Exception e) {
            // 500
            renderSystemError(context, "/system/500", "index", 500, e);
        }
    }
    
    public void onFrameworkStartup() {
        printFrameworkSplash();
    }
    
    public void onFrameworkShutdown() {
        
    }
    
    private void renderSystemError(Context.Internal2 context, String template, String layout, int status, Throwable e) {
        logRequestProperties(context, status, e);
        
        try {
            
            // is probably AJAX, so a simple string is returned
            if (!(context.requestHeader("x-requested-with") == null || context.requestHeader("X-Requested-With") == null)) {

                try {
                    Result response = ResultBuilder.text(getStackTraceString(e), status);
                    runner.run(context, response).close();
                } catch (Exception ex) {
                    logger.error("Failed to send error response to client", ex);
                }
            } else {
                Result response = ResultBuilder
                        .status(status)
                        .addAllViewObjects(getMapWithExceptionDataAndSession(e))
                        .template(template)
                        .layout(layout)
                        .contentType(MediaType.TEXT_HTML);

                runner.run(context, response).close();
            }
        } catch (Throwable t) {

            if (t instanceof IllegalStateException) {
                logger.error("Failed to render a template: '" + template
                        + "' because templates are rendered with Writer, but you probably already used OutputStream");
            }/* else {
                logger.error("java-web-planet internal error: ", t);
            }*/
            try {
                Result renderable = ResultBuilder.notFound()/*.contentType(MediaType.TEXT_HTML).layout(null)
                        .renderable("<html><head><title>Sorry!</title></head><body><div style='background-color:pink;'>internal error</div></body>")*/;
                runner.run(context, renderable).close();
            } catch (Exception ex) {
                logger.error(ex.toString(), ex);
            }
        }
    }
    
    private void logRequestProperties(Context context, int status, Throwable e) {
        String requestProperties = getRequestProperties(context);
        if (status < 500) {
            logger.warn("java-web-planet {} WARNING:\n{}\n{}", status, requestProperties, e.getMessage());
        } else {
            logger.error("java-web-planet {} ERROR:\n{}", status, requestProperties, e);
        }
    }
    
    private String getRequestProperties(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request URL: ").append(context.path()).append("\n");
        sb.append("ContextPath: ").append(context.contextPath()).append("\n");
        sb.append("Query String: ").append(context.queryString()).append("\n");
        sb.append("Method: ").append(context.httpMethod().name()).append("\n");    
        sb.append("IP: ").append(context.remoteIP()).append("\n");
        sb.append("Protocol: ").append(context.protocol()).append("\n");
        return sb.toString();
    }
    
    private Map<String, Object> getMapWithExceptionDataAndSession(Throwable e) {
        return CollectionUtil.map(
                "message", e.getMessage() == null ? e.toString() : e.getMessage(),
                "stack_trace", getStackTraceString(e));
    }
    
    private String getStackTraceString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
    
    private void printFrameworkSplash() {
        logger.info(FRAMEWORK_SPLASH);
    }
}
