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

public final class FrameworkEngine {
    
    private static final String FRAMEWORK_SPLASH = "\n" 
            + "     ____.  _____  __      _________   \n"
            + "    |    | /  _  \\/  \\    /  \\      \\  \n"
            + "    |    |/  /_\\  \\   \\/\\/   /   |   \\ \n"
            + "/\\__|    /    |    \\        /    |    \\ \n"
            + "\\________\\____|__  /\\__/\\  /\\____|__  /\n"
            + "  web framework  \\/      \\/         \\/ http://www.javapla.net\n";

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Router router;
    private final ResponseRunner runner;
//    private final ActionInvoker invoker;
//    private final Injector injector;

//    private final Lang lang;
    
    @Inject
    public FrameworkEngine(Router router, ResponseRunner runner/*, ActionInvoker invoker*//*, Injector injector *//*Lang lang*/) {
        this.router = router;
        this.runner = runner;
//        this.invoker = invoker;
//        this.injector = injector;
//        this.lang = lang;
    }
    
    public final void onRouteRequest(Context.Internal2 context) {
        final String path = context.request().path();
        String uri = path;
        
        if (uri.length() == 0) {
            uri = "/";//different servlet implementations, damn.
        }
        
        try {

            final Route route = router.retrieveRoute(context.request().method(), uri/*, invoker*//*injector*/);
            context.setRouteInformation(route, null/*format*/, null/*language*/, uri);
            
            //if (route != null) {
                // run pre-filters
                Response response = route.getFilterChain().before(context);
                
                // a filter might return a response, in which case do nothing
                if (response == null)
                    //response = invoker.executeAction(context);
                    response = route.executeRouteAndRetrieveResponse(context);
                
                ResponseStream rsp = runner.run(context, response);
            
                // run post-filters
                route.getFilterChain().after(context);
                
                // close response streams in the end
                rsp.close();
                
            /*} else {
                
                // This scenario ought not happen as the Router#getRoute() would have thrown an exception
                // if no route is found
                logger.warn("No matching route for servlet path: " + context.path() + ", passing down to container.");
            }*/
            
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
    
    //onRouteRequest
    public final void runRequest(Context.Internal context) {
        final String path = context.path();
        
        //String format = null;
        String uri;
        // look for any format in the request
        /*if (path.contains(".")) {
            uri = path.substring(0, path.lastIndexOf('.'));
            format = path.substring(path.lastIndexOf('.') + 1);
        } else*/ {
            uri = path;
        }
        
        //README maybe first do this language extraction IFF custom route not found
        /*String language = null;
        try {
            // if languages are set we try to deduce them
            language = lang.deduceLanguageFromUri(uri);
            if (!StringUtil.blank(language))
                uri = uri.substring(language.length() +1 ); // strip the language prefix from the URI
            else
                language = lang.getDefaultLanguage();
        } catch (LanguagesNotSetException e) {
            language = null;
        } catch (NotSupportedLanguageException e) {
            // use the default language
            language = lang.getDefaultLanguage();
            // We cannot be sure that the URI actually contains
            // a language parameter, so we let the router handle this
        }*/
            
        if (uri.length() == 0) {
            uri = "/";//different servlet implementations, damn.
        }
        
        try {

            /*16062*/
            /*8799*/
            //long time = System.nanoTime();
            final Route route = router.retrieveRoute(context.httpMethod(), uri/*, invoker*//*injector*/);
            //System.out.println("Timing :: " + (System.nanoTime() - time));
            context.setRouteInformation(route, null/*format*/, null/*language*/, uri);
            
            //if (route != null) {
                // run pre-filters
                Response response = route.getFilterChain().before(context);
                
                //might already have been handled by the controller or filters
                if (response == null)
                    //response = invoker.executeAction(context);
                    response = route.executeRouteAndRetrieveResponse(context);
                runner.run(context, response);
                
                // run post-filters
                route.getFilterChain().after(context);
            /*} else {
                
                // This scenario ought not happen as the Router#getRoute() would have thrown an exception
                // if no route is found
                logger.warn("No matching route for servlet path: " + context.path() + ", passing down to container.");
            }*/
            
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
    
    private void renderSystemError(Context context, String template, String layout, int status, Throwable e) {
        logRequestProperties(context, status, e);
        
        try {
            
            // is probably AJAX, so a simple string is returned
            if (!(context.requestHeader("x-requested-with") == null || context.requestHeader("X-Requested-With") == null)) {

                try {
                    Response response = ResponseBuilder.text(getStackTraceString(e), status);
                    runner.run(context, response).close();
                } catch (Exception ex) {
                    logger.error("Failed to send error response to client", ex);
                }
            } else {
                Response response = ResponseBuilder
                        .status(status)
                        .addAllViewObjects(getMapWithExceptionDataAndSession(e))
                        .template(template)
                        .layout(layout)
                        .contentType(MediaType.TEXT_HTML);

                runner.run(context, response).close();
                // ParamCopy.copyInto(resp.values(), request, null);
            }
        } catch (Throwable t) {

            if (t instanceof IllegalStateException) {
                logger.error("Failed to render a template: '" + template
                        + "' because templates are rendered with Writer, but you probably already used OutputStream");
            } else {
                logger.error("java-web-planet internal error: ", t);
            }
            try {
                Response renderable = ResponseBuilder.ok().contentType(MediaType.TEXT_HTML)
                        .renderable("<html><head><title>Sorry!</title></head><body><div style='background-color:pink;'>internal error</div></body>");
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
        sb.append("Request URL: ").append(context.requestUrl()).append("\n");
        sb.append("ContextPath: ").append(context.contextPath()).append("\n");
        sb.append("Query String: ").append(context.queryString()).append("\n");
        sb.append("URI Full Path: ").append(context.requestUri()).append("\n");
        sb.append("URI Path: ").append(context.path()).append("\n");
        sb.append("Method: ").append(context.httpMethod().name()).append("\n");    
        sb.append("IP: ").append(context.remoteAddress()).append("\n");
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
