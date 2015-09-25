package net.javapla.jawn.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.exceptions.BadRequestException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.i18n.Lang;
import net.javapla.jawn.core.i18n.LanguagesNotSetException;
import net.javapla.jawn.core.i18n.NotSupportedLanguageException;
import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

//NinjaDefault
public class FrameworkEngine {
    
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
    private final Lang lang;
    private final Injector injector;
    
    @Inject
    public FrameworkEngine(Router router, ResponseRunner runner, Lang lang, Injector injector) {
        this.router = router;
        this.runner = runner;
        this.lang = lang;
        this.injector = injector;
    }
    
    //onRouteRequest
    public void runRequest(Context.Internal context) {
        String path = context.path();
        
        String format = null;
        String uri;
        // look for any format in the request
        if (path.contains(".")) {
            uri = path.substring(0, path.lastIndexOf('.'));
            format = path.substring(path.lastIndexOf('.') + 1);
        } else {
            uri = path;
        }
        
        //README maybe first do this language extraction IFF custom route not found
        String language;
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
        }
            
        if (StringUtil.blank(uri)) {
            uri = "/";//different servlet implementations, damn.
        }
        
        try {

//            long time = System.nanoTime();
            /*68318*/
//            Route route = router.getRoute(context.getHttpMethod(), uri, injector);
            /*16062*/
            Route route = router.retrieveRoute(context.getHttpMethod(), uri, injector);
//            System.out.println("Timing :: " + (System.nanoTime() - time));
            context.setRouteInformation(route, format, language, uri);
            
            if (route != null) {
                // run pre-filters
                Response response = route.getFilterChain().before(context);
                
                runner.run(context, response);
                
                // run post-filters
                route.getFilterChain().after(context);
            } else {
                
                // This scenario ought not happen as the Router#getRoute() would have thrown an exception
                // if no route is found
                logger.warn("No matching route for servlet path: " + context.path() + ", passing down to container.");
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
                    runner.run(context, response);
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

                runner.run(context, response);
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
                runner.run(context, renderable);
            } catch (Exception ex) {
                logger.error(ex.toString(), ex);
            }
        }
    }
    
    private void logRequestProperties(Context context, int status, Throwable e) {
        String requestProperties = getRequestProperties(context);
        if (status == 404) {
            logger.warn("java-web-planet 404 WARNING: {} \n{}", e.getMessage(), requestProperties);
        } else {
            logger.error("java-web-planet ERROR: \n" + requestProperties, e);
        }
    }
    
    private String getRequestProperties(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request URL: ").append(context.requestUrl()).append("\n");
        sb.append("ContextPath: ").append(context.contextPath()).append("\n");
        sb.append("Query String: ").append(context.queryString()).append("\n");
        sb.append("URI Full Path: ").append(context.requestUri()).append("\n");
        sb.append("URI Path: ").append(context.path()).append("\n");
        sb.append("Method: ").append(context.method()).append("\n");
        return sb.toString();
    }
    
    private Map<String, Object> getMapWithExceptionDataAndSession(Throwable e) {
        return CollectionUtil.map("message", e.getMessage() == null ? e.toString() : e.getMessage(),
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
