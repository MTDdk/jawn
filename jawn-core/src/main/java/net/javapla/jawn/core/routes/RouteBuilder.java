package net.javapla.jawn.core.routes;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

public class RouteBuilder {
    
//    public static final String ROOT_CONTROLLER_NAME = "index";//README could be set in filter.init()
//    public static final String DEFAULT_ACTION_NAME = "index";

    private final HttpMethod httpMethod;
    private String uri;
//    private AppController controller; //README see ControllerActionInvoker
    private String actionName; // is not prefixed with the http method
//    private Method actionMethod;
    private Class<? extends Controller> type;
    private Result response;
    
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
        
        this.type = controller.getClass();
        this.actionName = action;
        
        return this;
    }
    
    public void with(Result response) {
        this.response = response;
        this.actionName = Constants.DEFAULT_ACTION_NAME;
    }
    ResponseFunction func;
    public void with(ResponseFunction func) {
        this.func = func;
    }
    
    
    /**
     * Build the route.
     * @return the built route
     */
    public Route build(FiltersHandler filters, ActionInvoker invoker/*Injector injector*/) throws IllegalStateException, ControllerException {
//        if (controller == null) throw new IllegalStateException("Route not with a controller");
        if (uri == null) throw new IllegalStateException("Route is not specified");
        
        String action = constructAction(actionName, httpMethod);
        
        LinkedList<Filter> list = new LinkedList<>();
        if (type != null) {
            // Find all possible filters
            // First the controller specific
            list.addAll(filters.compileFilters(type));
            // Then all specific to the action
            list.addAll(filters.compileFilters(type, action));
        }
        
        Route route = new Route(uri, httpMethod, type, action, actionName, buildFilterChain(list, type, response));
        
        // experimental
        if (func != null) {
            route.setResponseFunction(func);
        } else if (response != null) {
            route.setResponseFunction(context -> response);
        } else if (type != null) {
            // verify that the controller has the corresponding action
            // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
            if ( ! ActionInvoker.isAllowedAction(type, action/*route.getAction()*/))
                throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", type, route.getAction()));

            try {
                route.setActionMethod(route.getController().getMethod(action));
            } catch (NoSuchMethodException | SecurityException ignore) {}

//            ActionInvoker invoker = injector.getInstance(ActionInvoker.class);
            route.setResponseFunction(invoker);
        }
        
//        if (/*actionName != null && */type != null) {
//            // verify that the controller has the corresponding action
//            // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//            if ( ! ActionInvoker.isAllowedAction(type, action/*route.getAction()*/))
//                throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", type, route.getAction()));
//            
//            try {
//                route.setActionMethod(route.getController().getMethod(action));
//            } catch (NoSuchMethodException | SecurityException ignore) {}
//        }
        
        return route;
    }
    
    final static Route build(Route route, String actionName) {
        String action = constructAction(actionName, route.getHttpMethod());
        if ( ! ActionInvoker.isAllowedAction(route.getController(), action/*route.getAction()*/))
            throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", route.getController(), route.getAction()));
        
        Route newRoute = new Route(route.uri, route.getHttpMethod(), route.getController(), action, actionName, route.getFilterChain());
        try {
            newRoute.setActionMethod(route.getController().getMethod(action));
        } catch (NoSuchMethodException | SecurityException ignore) {}
        
        //experimental
        newRoute.setResponseFunction(route.func);
        return newRoute;
    }
    
    private static final FilterChain filterChainEnd = new FilterChainEnd();
    private static final FilterChain buildFilterChain(/*ControllerActionInvoker invoker,*//*FilterChain chainEnd,*//*Injector injector, */List<Filter> filters, Class<? extends Controller> controller/*, boolean isProduction*/, Result response) {
        if (filters.isEmpty()) {
            return /*response != null ? new FilterChainEnd(response) :*/ filterChainEnd;/*chainEnd;/*///new FilterChainEnd(invoker/*new ControllerActionInvoker(controller, isProduction), *//*injector*/);
        } else {
            Filter filter = filters.remove(0);
            return new FilterChainImpl(filter, buildFilterChain(/*invoker,*//*chainEnd,*//*injector*/ filters, controller/*, isProduction*/, response));
        }
    }
    
    /**
     * Creates the action signature for the given action name and http method.<br>
     * E.g.: <p>
     *       actionName = video<br>
     *       method = GET<br>
     *       action = getVideo
     *       <p>
     *       actionName = video-upload<br>
     *       method = POST<br>
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
