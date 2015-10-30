package net.javapla.jawn.core;

import java.text.MessageFormat;
import java.util.List;

import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.api.FilterChainEnd;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ControllerActionInvoker;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

import com.google.inject.Injector;

public class RouteBuilder {
    
//    public static final String ROOT_CONTROLLER_NAME = "index";//README could be set in filter.init()
//    public static final String DEFAULT_ACTION_NAME = "index";

    private final HttpMethod httpMethod;
    private String uri;
//    private AppController controller; //README see ControllerActionInvoker
    private String actionName; // is not prefixed with the http method
//    private Method actionMethod;
    private Class<? extends Controller> type;
    
    private RouteBuilder(HttpMethod m) {
        httpMethod = m;
//        actionName = DEFAULT_ACTION_NAME;
    }
    
    public static RouteBuilder get() {
        return new RouteBuilder(HttpMethod.GET);
    }
    
    public static RouteBuilder post() {
        return new RouteBuilder(HttpMethod.POST);
    }
    
    public static RouteBuilder put() {
        return new RouteBuilder(HttpMethod.PUT);
    }
    
    public static RouteBuilder delete() {
        return new RouteBuilder(HttpMethod.DELETE);
    }
    
    public static RouteBuilder head() {
        return new RouteBuilder(HttpMethod.HEAD);
    }
    
    public static RouteBuilder method(HttpMethod method) {
        return new RouteBuilder(method);
    }
    
    
    /**
     * Used for custom routes
     * <p>Can be a regular expression
     * <p>If <b>uri</b> contains reserved keywords like "action", "controller" or "package", then this will be attempted to be deduced
     * and used <b>IFF</b> it has not been explicitly stated already
     * @param uri what was specified in the  RouteConfig class
     */
    public RouteBuilder route(String uri) {
        this.uri = uri;
        return this;
    }
    
    public RouteBuilder to(Class<? extends Controller> type) {
        this.type = type;
        //this.actionName = DEFAULT_ACTION_NAME;
        return this;
    }
    
    public RouteBuilder to(Class<? extends Controller> type, String action) /*throws ControllerException*/{
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
        this.type = type;
        this.actionName = action;
        
        return this;
    }
    
    public RouteBuilder to(Controller controller, String action) /*throws ControllerException*/ {
//        // verify that the controller has the corresponding action
//        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
//        this.controller = controller;
        this.type = controller.getClass();
        this.actionName = action;
        
        return this;
    }
    
   /* public RouteBuilder to(Class<? extends Controller> type, Method action) {
        this.type = type;
        this.actionMethod = action;
        
        return this;
    }*/
    
    
    /**
     * Build the route.
     * @return the built route
     */
    public Route build(FiltersHandler filters, Injector injector) throws IllegalStateException, ControllerException {
//        if (controller == null) throw new IllegalStateException("Route not with a controller");
        if (uri == null) throw new IllegalStateException("Route is not specified");
        
        String action = constructAction(actionName, httpMethod);
        
        // Find all possible filters
        // First the controller specific
        List<Filter> list = filters.compileFilters(type);
        // Then all specific to the action
        list.addAll(filters.compileFilters(type, action));
        
        
        FilterChainEnd chainEnd = injector.getInstance(FilterChainEnd.class);
        Route route = new Route(uri, httpMethod, type, action, actionName, buildFilterChain(chainEnd,/*injector,*/ list, type));
        
        if (/*actionName != null && */type != null)
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
        if ( ! ControllerActionInvoker.isAllowedAction(type, action/*route.getAction()*/))
            throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", type, route.getAction()));
        
        try {
            route.setActionMethod(route.getController().getMethod(action));
        } catch (NoSuchMethodException | SecurityException ignore) {}
        
        return route;
    }
    
    final static Route build(Route route, String actionName) {
        String action = constructAction(actionName, route.getHttpMethod());
        if ( ! ControllerActionInvoker.isAllowedAction(route.getController(), action/*route.getAction()*/))
            throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", route.getController(), route.getAction()));
        
        Route newRoute = new Route(route.uri, route.getHttpMethod(), route.getController(), action, actionName, route.getFilterChain());
        try {
            newRoute.setActionMethod(route.getController().getMethod(action));
        } catch (NoSuchMethodException | SecurityException ignore) {}
        return newRoute;
    }
    
    private FilterChain buildFilterChain(FilterChain chainEnd,/*Injector injector, */List<Filter> filters, Class<? extends Controller> controller/*, boolean isProduction*/) {
        if (filters.isEmpty()) {
            return chainEnd;//new FilterChainEnd(/*new ControllerActionInvoker(controller, isProduction), *//*injector*/);
        } else {
            Filter filter = filters.remove(0);
            return new FilterChainImpl(filter, buildFilterChain(chainEnd/*injector*/, filters, controller/*, isProduction*/));
        }
    }
    
    /**
     * Creates the action signature for the given action name and http method.
     * E.g.: actionName = video
     *       method = GET
     *       action = getVideo
     *       
     *       actionName = video-upload
     *       method = POST
     *       action = postVideoUpload
     *       
     * <p>
     * Special case for actionName = index, as this should be the same for the action itself.
     * @param actionName
     * @param method
     * @return
     */
    private static final String constructAction(String actionName, HttpMethod method) {
        if (StringUtil.blank(actionName)) return Constants.DEFAULT_ACTION_NAME;
        if (Constants.DEFAULT_ACTION_NAME.equals(actionName) && method == HttpMethod.GET)
            return actionName;
        return method.name().toLowerCase() + StringUtil.camelize(actionName.replace('-', '_'), true);
    }
    
}
