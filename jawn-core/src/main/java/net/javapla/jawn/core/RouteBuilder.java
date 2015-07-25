package net.javapla.jawn.core;

import java.text.MessageFormat;
import java.util.List;

import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ControllerActionInvoker;
import net.javapla.jawn.core.spi.Filter;
import net.javapla.jawn.core.spi.FilterChain;
import net.javapla.jawn.core.spi.FilterChainEnd;
import net.javapla.jawn.core.util.StringUtil;

import com.google.inject.Injector;

public class RouteBuilder {
    
    public static final String ROOT_CONTROLLER_NAME = "index";//README could be set in filter.init()
    public static final String DEFAULT_ACTION_NAME = "index";

    private final HttpMethod httpMethod;
    private String uri;
//    private AppController controller; //README see ControllerActionInvoker
    private String actionName; // is not prefixed with the http method
    private Class<? extends ApplicationController> type;
    
    private RouteBuilder(HttpMethod m) {
        httpMethod = m;
//        actionName = DEFAULT_ACTION_NAME;
    }
    
    static RouteBuilder get() {
        return new RouteBuilder(HttpMethod.GET);
    }
    
    static RouteBuilder post() {
        return new RouteBuilder(HttpMethod.POST);
    }
    
    static RouteBuilder put() {
        return new RouteBuilder(HttpMethod.PUT);
    }
    
    static RouteBuilder delete() {
        return new RouteBuilder(HttpMethod.DELETE);
    }
    
    static RouteBuilder head() {
        return new RouteBuilder(HttpMethod.HEAD);
    }
    
    static RouteBuilder method(HttpMethod method) {
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
    
    public RouteBuilder to(Class<? extends ApplicationController> type) {
        this.type = type;
        this.actionName = DEFAULT_ACTION_NAME;
//        try {
//            this.controller = type.newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        return this;
    }
    
    public RouteBuilder to(Class<? extends ApplicationController> type, String action) /*throws ControllerException*/{
//        try {
//            this.controller = type.newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
        this.type = type;
        this.actionName = action;
        
        return this;
    }
    
    public RouteBuilder to(ApplicationController controller, String action) /*throws ControllerException*/ {
//        // verify that the controller has the corresponding action
//        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
//        this.controller = controller;
        this.type = controller.getClass();
        this.actionName = action;
        
        return this;
    }
    
    
    /**
     * Build the route
     * @return
     */
    Route build(FiltersHandler filters, Injector injector) throws IllegalStateException, ControllerException {
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
        if ( ! ControllerActionInvoker.isAllowedAction(type, action/*route.getAction()*/))//controller.isAllowedAction(route.getAction()) )
            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, route.getAction()));
        
        return route;
    }
    
    private FilterChain buildFilterChain(FilterChain chainEnd,/*Injector injector, */List<Filter> filters, Class<? extends ApplicationController> controller/*, boolean isProduction*/) {
        if (filters.isEmpty()) {
            return chainEnd;//new FilterChainEnd(/*new ControllerActionInvoker(controller, isProduction), *//*injector*/);
        } else {
            Filter filter = filters.remove(0);
            return new FilterChainImpl(filter, buildFilterChain(chainEnd/*injector*/, filters, controller/*, isProduction*/));
        }
    }
    
    private String constructAction(String actionName, HttpMethod method) {
        if (StringUtil.blank(actionName)) return DEFAULT_ACTION_NAME;
        if (DEFAULT_ACTION_NAME.equals(actionName) && method == HttpMethod.GET)
            return actionName;
        return method.name().toLowerCase() + StringUtil.camelize(actionName.replace('-', '_'), true);
    }
    
}
