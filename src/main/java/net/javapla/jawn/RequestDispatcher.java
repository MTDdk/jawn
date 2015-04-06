/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import net.javapla.jawn.application.IRouteConfig;
import net.javapla.jawn.exceptions.ActionNotFoundException;
import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.exceptions.CompilationException;
import net.javapla.jawn.exceptions.ConfigurationException;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.InitException;
import net.javapla.jawn.exceptions.RouteException;
import net.javapla.jawn.exceptions.ViewException;
import net.javapla.jawn.exceptions.ViewMissingException;
import net.javapla.jawn.exceptions.WebException;
import net.javapla.jawn.i18n.Lang;
import net.javapla.jawn.trash.RenderTemplateResponse;
import net.javapla.jawn.util.CollectionUtil;
import net.javapla.jawn.util.Constants;
import net.javapla.jawn.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author Igor Polevoy
 * @author ALVN
 */
public class RequestDispatcher implements Filter {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private ServletContext servletContext;
    private Set<String> exclusions = new TreeSet<String>();
    private ControllerRegistry controllerRegistry;
    private AppContext appContext;
    private Bootstrap appBootstrap;
//    private String root_controller;
    
    private NewRouter router2;

    private Injector injector;

    private PropertiesImpl properties;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        properties = new PropertiesImpl(ModeHelper.determineModeFromSystem());
        
        this.servletContext = filterConfig.getServletContext();
        controllerRegistry = new ControllerRegistry(servletContext);
        appContext = new AppContext(servletContext);
        
        // adding user modules to controllerRegistry
        initApp(appContext, controllerRegistry, properties);
        
        
//        Lang lang = new Lang(appContext.getSupportedLanguages());
        
        
        //-----
        Bootstrapper bootstrapper = new Bootstrapper(properties, controllerRegistry.getModules());
        bootstrapper.boot();
        injector = bootstrapper.getInjector();
        controllerRegistry.setInjector(injector);
//        runner = new ControllerRunner(injector);
        router2 = createRouter(); // created at startup, as we have no need for reloading custom routes.
        //--------
        
        
//        Configuration.getTemplateManager().setServletContext(servletContext);
//        Context.setControllerRegistry(registry);//bootstrap below requires it //README this might not be the case anymore
        servletContext.setAttribute("appContext", appContext);
        
        findExclusionPaths();
        
        // either the encoding was set by the user, or we default
        String enc = appContext.getAsString(AppContext.ENCODING);
        if (enc == null) {
            enc = Constants.ENCODING;
            appContext.set(AppContext.ENCODING, enc);
        }
        logger.debug("Setting encoding: " + enc);
        
//        root_controller = filterConfig.getInitParameter("root_controller");
        logger.info("ActiveWeb: starting the app in environment: " + properties.getMode());//Configuration.getEnv());
    }

    protected void initApp(AppContext context, ControllerRegistry registry, PropertiesImpl properties) {
        initAppConfig(properties.get(Constants.Params.bootstrap.name()) /*Configuration.getBootstrapClassName()*/, context, registry, properties, true);
        //these are optional config classes:
        initAppConfig(properties.get(Constants.Params.controllerConfig.name())/*Configuration.getControllerConfigClassName()*/, context, registry, properties, false);//AppControllerConfig
        initAppConfig(properties.get(Constants.Params.dbconfig.name())/*Configuration.getDbConfigClassName()*/, context, registry, properties, false);
    }
    
    private void findExclusionPaths() {
        // Let other handlers deal with folders that do not reside in the WEB-INF or META-INF
        Set<String> resourcePaths = servletContext.getResourcePaths("/");
        resourcePaths.removeIf( path -> path.contains("-INF") || path.contains("-inf"));
        for (String path : resourcePaths) {
            if (path.charAt(0) != '/')
                path = '/' + path;
            if (path.charAt(path.length()-1) == '/')
                path = path.substring(0, path.length()-1);// remove the last slash
            exclusions.add(path);
        }
        
        /*String exclusionsParam = filterConfig.getInitParameter("exclusions");
        logger.error("exclusions {}", exclusionsParam);
        if (exclusionsParam != null) {
            List<String> list = Arrays.asList(exclusionsParam.split(","));
            for (String exclusion : list) {
                String trimmed = exclusion.trim();
                if (trimmed.charAt(0) != '/')
                    trimmed = '/' + trimmed;
                exclusions.add(trimmed);
            }
        }*/
    }

    //this exists for testing only
    /*private AbstractRouteConfig routeConfigTest;
    private boolean testMode = false;
    protected void setRouteConfig(AbstractRouteConfig routeConfig) {
        this.routeConfigTest = routeConfig;
        testMode = true;
    }*/

    //README is it necessary to dynamically load the Router? 
    /*private Router getRouter(AppContext context, ControllerRegistry controllerRegistry){
        Router router = new Router(root_controller, controllerRegistry, injector.getInstance(Lang.class)context.getSupportedLanguages()Configuration.supportedLanguages()filterConfig.getInitParameter("default_language"));
        
        String routeConfigClassName = Configuration.getRouteConfigClassName();
        try {
            AbstractRouteConfig routeConfigLocal;
            if(testMode){
                routeConfigLocal = routeConfigTest;
            }else{
                routeConfigLocal = DynamicClassFactory.createInstance(routeConfigClassName, AbstractRouteConfig.class); // handles caching of classes
            }
            routeConfigLocal.clear();
            routeConfigLocal.init(context);
            router.setRoutes(routeConfigLocal.getRoutes());
            router.setIgnoreSpecs(routeConfigLocal.getIgnoreSpecs());

            logger.debug("Loaded routes from: " + routeConfigClassName);

        } catch (IllegalArgumentException e) {
            throw e;
        }catch(ConfigurationException e){
            throw  e;
        } catch (Exception e) {
            logger.debug("Did not find custom routes. Going with built in defaults: " + getCauseMessage(e));
        }
        return router;
    }*/
    
    //TODO the router most definitely should be created at startup, as we do not care for looking up custom routes per request
    private NewRouter createRouter() {
        NewRouter router = injector.getInstance(NewRouter.class);
        
        String routeConfigClassName = "app.config.NewRouteConfig";
        // try to read custom routes provided by user
        try {
            IRouteConfig localRouteConfig = DynamicClassFactory.createInstance(routeConfigClassName, IRouteConfig.class, false);
            localRouteConfig.init(router);
            logger.debug("Loaded routes from: " + routeConfigClassName);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch(ConfigurationException e){
            throw  e;
        } catch (Exception e) {
            logger.debug("Did not find custom routes. Going with built in defaults: " + getCauseMessage(e));
        }
        
        router.compileRoutes();
        
        return router;
    }

    //TODO: refactor to some util class. This is stolen...ehrr... borrowed from Apache ExceptionUtils
    static String getCauseMessage(Throwable throwable) {
        List<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && list.contains(throwable) == false) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list.get(0).getMessage();
    }

    private void initAppConfig(String configClassName, AppContext context, ControllerRegistry registry, PropertiesImpl properties, boolean fail){
        AppConfig appConfig;
        try {
            appConfig = DynamicClassFactory.createInstance(configClassName, AppConfig.class, false);
            if(appConfig instanceof  Bootstrap){
                appBootstrap = (Bootstrap) appConfig;
            }
//            appConfig.init(context);
            appConfig.init(properties);
            appConfig.completeInit(registry);
        }
        catch (Throwable e) {
            if(fail){
                throw new InitException("Failed to create and init a new instance of class: " + configClassName, e);
            }else{
                logger.debug("Failed to create and init a new instance of class: " + configClassName
                        + ", proceeding without it. " + e);
            }
        }
    }


//    protected ControllerRegistry getControllerRegistry() {
//        return (ControllerRegistry) /*filterConfig.get*/servletContext.getAttribute("controllerRegistry");
//    }
    

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        try {
            doFilter((HttpServletRequest) req, (HttpServletResponse) resp, chain);
        } catch (ClassCastException e) {
            // ought not be possible
            throw new ServletException("non-HTTP request or response", e);
        }
    }
    
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)  throws ServletException, IOException {
        try {

            String path = /*request.getRequestURI();*/request.getServletPath();
            
            // redirect if the path ends with a slash - URLS cannot end with a slash - they should not do such a thing - why would they?
            if (redirectUrlEndingWithSlash(path, response)) return;

            //MTD: filtering resources
            if (filteringResources(request, response, chain, path)) return;
            
            String format = null;
            String uri;
            // look for any format in the request
            if(path.contains(".")){
                uri = path.substring(0, path.lastIndexOf('.'));
                format = path.substring(path.lastIndexOf('.') + 1);
            }else{
                uri = path;
            }
            
            
            //TODO first do this language extraction IF custom route not found
            //MTD: we first look for a language prefix and strip the URI if it is found
            String language = findLanguagePrefix(uri, injector.getInstance(Lang.class));
            if (!StringUtil.blank(language)) {
                uri = uri.substring(language.length() +1 );
                //if (uri.isEmpty()) uri = "/";
            } else {
                language = injector.getInstance(Lang.class).getDefaultLanguage();//defaultLanguage;
            }

            if (StringUtil.blank(uri)) {
                uri = "/";//different servlet implementations, damn.
            }
            
            
            //TODO create Context and send it along the calling chain (remember: there are some 'new Context()'s around) 
//            NewContext c = new NewContext();
//            c.init(servletContext, request, response, appContext);
//            Context.setTLs(request, response, /*servletContext,*/ /*getControllerRegistry(), *//*appContext,*/ new RequestContext(), format);
            
//            RequestContext requestContext = new RequestContext();
            
//            NewRouter router2 = createRouter();
            NewRoute route2 = router2.getRoute(HttpMethod.getMethod(request), uri);
            logger.debug("---- route2 = {}", route2);
            if (route2 != null) {
                logger.debug("--------- {}.{}", route2.getController().getClass(), route2.getAction());
            }
            
//            Router router = getRouter(appContext, controllerRegistry);
//            Route route = router.recognize(uri, HttpMethod.getMethod(request), requestContext);
//            route.setFormat(format);

            if (route2 != null) {
                //TODO consider if it should be possible to ignore routes
//                if(route.ignores(path)) {
//                    chain.doFilter(request, response); // let someone else handle this
//                    logger.debug("URI ignored: " + path);
//                    return;
//                }

                if (properties.getBoolean(Constants.LOG_REQUESTS)) {
                    logger.info("================ New request: " + new Date() + " ================");
                }
                
                Context context = new Context(servletContext, request, response, injector.getInstance(PropertiesImpl.class));
                context.init(route2, format, language, uri);
//                new ControllerRunner(injector.getInstance(PropertiesImpl.class), request, response, controllerRegistry, context).run(route2, true);
         //-------------------------------
//                NewControllerResponse resp2 = new NewControllerResponse();
//                resp2.contentType(MediaType.TEXT_HTML);
//                resp2.status(200);
//                resp2.renderable("henning");
//                context.setNewControllerResponse(resp2);
                toBePutIntoClass(context, route2);
                ControllerResponseRunner runner = injector.getInstance(ControllerResponseRunner.class);
                runner.run(context, context.getNewControllerResponse());
            } else {
                //TODO: theoretically this will never happen, because if the route was not excluded, the router.recognize() would throw some kind
                // of exception, leading to the a system error page.
                logger.warn("No matching route for servlet path: " + request.getServletPath() + ", passing down to container.");
                chain.doFilter(request, response);//let it fall through
            }
        } catch (CompilationException e) {
            renderSystemError(request, response, e);
//        } catch (ClassLoadException e) {
//            renderSystemError(request, response, "/system/404", useDefaultLayoutForErrors() ? getDefaultLayout():null, 404, e);
//        }catch (ActionNotFoundException e) {
//            renderSystemError(request, response, "/system/404", useDefaultLayoutForErrors() ? getDefaultLayout():null, 404, e);
        }catch(/*ClassLoadException |*/ ActionNotFoundException |ViewMissingException | RouteException e){
            renderSystemError(request, response, "/system/404", properties.get(Constants.Params.defaultLayout.name()), 404, e);
//        }catch(RouteException e){
//            renderSystemError(request, response, "/system/404", useDefaultLayoutForErrors() ? getDefaultLayout():null, 404, e);
        }catch(ViewException e){
            renderSystemError(request, response, "/system/error", properties.get(Constants.Params.defaultLayout.name()), 500, e);
        }catch (Throwable e) {
            renderSystemError(request, response, e);
        }finally {
//           Context.clear();
//            List<String> connectionsRemaining = DB.getCurrrentConnectionNames();
//            if(connectionsRemaining.size() != 0){
//                logger.warn("CONNECTION LEAK DETECTED ... and AVERTED!!! You left connections opened:"
//                        + connectionsRemaining + ". ActiveWeb is closing all active connections for you...");
//                DB.closeAllConnections();
//            }
        }
    }
    
    
    private void injectControllerWithContext(HttpSupport controller, Context context) {
        controller.init(context, injector);
    }
    private void toBePutIntoClass(Context context, NewRoute route2) {
     // if we want to reload the controller, this is a good time to do it
        if (! properties.isProd()) {
            context.getRoute().reloadController();
        }
        //
        injectControllerWithContext(route2.getController(), context);
        
        //Inject dependencies
        if (injector != null) {
            injector.injectMembers(route2.getController());
        }
        
        //Execute action
        try{
            
          //find the method name and run it
          String methodName = route2.getAction().toLowerCase();
          for (Method method : route2.getController().getClass().getMethods()) {
              if (methodName.equals( method.getName().toLowerCase() )) {
                  method.invoke(route2.getController());
                  return;
              }
          }
          throw new ControllerException(String.format("Action name (%s) not found in controller (%s)", route2.getAction(), route2.getController().getClass().getSimpleName()));
          
//          Method m = controller.getClass().getMethod(actionName);
//          m.invoke(controller);
      }catch(InvocationTargetException e){
          if(e.getCause() != null && e.getCause() instanceof  WebException){
              throw (WebException)e.getCause();                
          }else if(e.getCause() != null && e.getCause() instanceof RuntimeException){
              throw (RuntimeException)e.getCause();
          }else if(e.getCause() != null){
              throw new ControllerException(e.getCause());
          }
      }catch(WebException e){
          throw e;
      }catch(Exception e){
          throw new ControllerException(e);
      }
    }
    
    private boolean redirectUrlEndingWithSlash(String path, HttpServletResponse response) throws IOException {
        if (path.length() > 1 && path.endsWith("/")) { 
            response.sendRedirect(path.substring(0, path.length()-1));
            return true;
        }
        return false;
    }

    private Map<String, Object> getMapWithExceptionDataAndSession(HttpServletRequest request, Throwable e) {
        return CollectionUtil.map("message", e.getMessage() == null ? e.toString() : e.getMessage(),
                   "stack_trace", getStackTraceString(e),
                   "session", SessionHelper.getSessionAttributes(request));
    }


    /**
     * A problem arises when the server tries to serve a resource, which does not start with an excluded path,
     * and the server just sees it as a route to handle itself, when it actually ought to send it up the chain
     * 
     * @author MTD
     * @return true, if a filtering has happened and nothing else should be done by this current dispatcher
     */
    private boolean filteringResources(HttpServletRequest req, HttpServletResponse resp, FilterChain chain, String path) throws ServletException, IOException {
        String translated = translateResource(path);
        if (translated != null) {
//            logger.debug("URI excluded: {}", path);
            
            HttpServletRequest r = req;
            if (path.length() != translated.length()) {//!path.startsWith(translated)) {
//                int indexOfExcluded = path.indexOf(translated);
//                String t = path.substring(indexOfExcluded);
                r = createServletRequest(req, translated);
//                logger.debug(".. and translated -> {}", translated);
            }
            
            
            // setting default headers for static files
            resp.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=604800"); // one week
            File file = new File(servletContext.getRealPath(translated));
            if (file.canRead())
                resp.setHeader(HttpHeaders.ETAG, String.valueOf(file.lastModified()));
            
            chain.doFilter(r, resp);
            return true;
        }
        return false;
    }
    
    /**
     * Creates a new HttpServletRequest object.
     * Useful, as we cannot modify an existing ServletRequest.
     * Used when resources needs to have the {controller} stripped from the servletPath.
     * 
     * @author MTD
     */
    private HttpServletRequest createServletRequest(HttpServletRequest req, String translatedPath) {
        return new HttpServletRequestWrapper(req){
            @Override
            public String getServletPath() {
                return translatedPath;
            }
        };
    }

    /**
     * Instead of just verifying if a path contains an exclusion, the method returns the exclusion.
     * This takes care of some resources being prepended with the language prefix.
     * Like:
     *      http://localhost/da/images/something.jpg
     * should be the same as
     *      http://localhost/en/images/something.jpg,
     *      http://localhost/images/something.jpg
     * @author MTD
     */
    private String translateResource(String servletPath) {
        // TODO look at the necessity of this line. Its purpose is to serve single files not in a folder of the root webapp - like favicon.ico
        if (exclusions.contains(servletPath))
            return servletPath;
        
        int start = 0, end = servletPath.indexOf('/',1);
        String segment;
        while (end > -1) { // fail fast
            segment = servletPath.substring(start, end);
            for (String exclusion : exclusions) {
                if (segment.equals( exclusion )) {
                    return servletPath.substring(start);
                }
            }
            start = end;
            end = servletPath.indexOf('/',start+1);
        }
        
        /*String[] segments = servletPath.split("/");
        for (String segment : segments) {
            if (segment.equals(exclusion))
                return exclusion;//exclusion.charAt(0) == '/' ? exclusion : '/'+exclusion; <-- this is ensured in the init()
        }*/
        return null;
    }
    
    
    private void renderSystemError(HttpServletRequest request, HttpServletResponse response, Throwable e) {
        renderSystemError(request, response, "/system/error", null, 500, e);
    }


    private String getStackTraceString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private void renderSystemError(HttpServletRequest request, HttpServletResponse response, String template, String layout, int status, Throwable e) {
        try{
            String requestProperties = getRequestProperties(request);
            if(status == 404){
                logger.warn("ActiveWeb 404 WARNING: \n" + requestProperties, e);
            }else{
                logger.error("ActiveWeb ERROR: \n" + requestProperties, e);
            }

            if (request.getHeader("x-requested-with") != null
                    || request.getHeader("X-Requested-With") != null) {

                try {
                    response.setStatus(status);
                    response.getWriter().write(getStackTraceString(e));
                } catch (Exception ex) {
                    logger.error("Failed to send error response to client", ex);
                }
            } else {
                Context c = new Context(servletContext, request, response, injector.getInstance(PropertiesImpl.class));
                ControllerResponseRunner runner = injector.getInstance(ControllerResponseRunner.class);
                runner.run(c, c.getNewControllerResponse().layout(layout).contentType("text/html").status(status));
                
//                RenderTemplateResponse resp = new RenderTemplateResponse(, getMapWithExceptionDataAndSession(request, e), template);
//                resp.setLayout(layout);
//                resp.setContentType("text/html");
//                resp.setStatus(status);
//                resp.setTemplateManager(Configuration.getTemplateManager());
////                ParamCopy.copyInto(resp.values(), request, null);
//                resp.process();
            }
        }catch(Throwable t){

            if(t instanceof IllegalStateException){
                logger.error("Failed to render a template: '" + template + "' because templates are rendered with Writer, but you probably already used OutputStream");
            }else{
                logger.error("ActiveWeb internal error: ", t);
            }
            try{
                //MTD: changed from getOutputStream(), as this results in IllegalStateException, due to getWriter() has been called prior to this statement 
                response.getWriter().print("<html><head><title>Sorry!</title></head><body><div style='background-color:pink;'>internal error</div></body>");
            }catch(Exception ex){
                logger.error(ex.toString(), ex);
            }
        }
    }


    private String getRequestProperties(HttpServletRequest request){
        StringBuilder sb = new StringBuilder();
        if (request == null) return "";
        sb.append("Request URL: ").append(request.getRequestURL()).append("\n");
        sb.append("ContextPath: ").append(request.getContextPath()).append("\n");
        sb.append("Query String: ").append(request.getQueryString()).append("\n");
        sb.append("URI Full Path: ").append(request.getRequestURI()).append("\n");
        sb.append("URI Path: ").append(request.getServletPath()).append("\n");
        sb.append("Method: ").append(request.getMethod()).append("\n");
        return sb.toString();
    }
    
    public void destroy() {
        appBootstrap.destroy(appContext);
    }
    
    /**
     * Finds the language segment of the URI if the language property is set.
     * Just extracts the first segment.
     * @param uri The full URI
     * @return the extracted language segment. null, if none is found
     * @author MTD
     */
    //TODO perhaps refactor into some LangHelper
    protected String findLanguagePrefix(String uri, Lang language) {
        if ( ! language.areLanguagesSet()) return null;
        String lang = uri.startsWith("/") ? uri.substring(1) : uri;
        lang = lang.split("/")[0];
        
        if(language.isLanguageSupported(lang)) return lang;
        
        //TODO we probably want to throw some exceptions
        return null;
    }
}
