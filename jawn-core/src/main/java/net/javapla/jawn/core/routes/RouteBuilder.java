package net.javapla.jawn.core.routes;

import java.text.MessageFormat;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.routes.Route.ResponseFunction;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

public class RouteBuilder {
    
    private final HttpMethod httpMethod;
    private String uri;
    private String actionName; // is not prefixed with the http method
    private Class<? extends Controller> controller;
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
        this.uri = (uri.charAt(0) != '/') ? "/" + uri : uri;
        return this;
    }
    
    public RouteBuilder to(Class<? extends Controller> type) {
        this.controller = type;
        //this.actionName = DEFAULT_ACTION_NAME;
        return this;
    }
    
    public RouteBuilder to(Class<? extends Controller> controller, String action) /*throws ControllerException*/{
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
        this.controller = controller;
        this.actionName = action;
        
        return this;
    }
    
    public RouteBuilder to(Controller controller, String action) /*throws ControllerException*/ {
//        // verify that the controller has the corresponding action
//        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
        this.controller = controller.getClass();
        this.actionName = action;
        
        return this;
    }
    
    public void with(Result response) {
        this.response = response;
        this.actionName = Constants.DEFAULT_ACTION_NAME;
    }
    ResponseFunction func;
    public RouteBuilder with(ResponseFunction func) {
        this.func = func;
        return this;
    }
    
    
    /**
     * Build the route.
     * @return the built route
     */
    public Route build(FiltersHandler filters, ActionInvoker invoker) throws IllegalStateException, ControllerException {
        if (uri == null) throw new IllegalStateException("Route is not specified");
        
        String action = constructAction(actionName, httpMethod);
        
        // Find all possible filters
        LinkedList<Filter> list = new LinkedList<>();
        if (controller != null) {
            // First the controller specific
            list.addAll(filters.compileFilters(controller));
            // Then all specific to the action
            list.addAll(filters.compileFilters(controller, action));
        } else {
            list.addAll(filters.compileGlobalFilters());
        }
        
        Route route = new Route(uri, httpMethod, controller, action, actionName, buildFilterChain(list, controller, response));
        
        // experimental
        if (func != null) {
            route.setResponseFunction(func, true);
        } else if (response != null) {
            route.setResponseFunction(context -> response, true);
        } else if (controller != null) {
            // verify that the controller has the corresponding action
            // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
            if ( ! ActionInvoker.isAllowedAction(controller, action/*route.getAction()*/))
                throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", controller, route.getAction()));

            try {
                route.setActionMethod(route.getController().getMethod(action));
                route.setResponseFunction(invoker::executeAction);
                //route.setResponseFunction(invoker, false);
            } catch (NoSuchMethodException | SecurityException ignore) {}
        }
        
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
    private static final FilterChain buildFilterChain(LinkedList<Filter> filters, Class<? extends Controller> controller, Result response) {
        if (filters.isEmpty()) {
            return filterChainEnd;
        } else {
            Filter filter = filters.remove(0);
            return new FilterChainImpl(filter, buildFilterChain(filters, controller, response));
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
     * @param routeBuilder 
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
    
    /**
     * Indicates the end of a filter chain.
     * This should always be the last filter to be called.
     * 
     * @author MTD
     */
    static class FilterChainEnd implements FilterChain {
        private final Logger log = LoggerFactory.getLogger(FilterChainEnd.class);

        
        @Override
        public Result before(Context context) {
            // When returning null, the ControllerActionInvoker is called instead
            return null;
        }

        @Override
        public void after(Context context) {
            //if (context instanceof Context.Internal2) ((Context.Internal2) context).response().end();
        }
        
        @Override
        public void onException(Exception e) {
            log.error("Filter chain broke", e);
        }
    }
    
    static class FilterChainImpl implements FilterChain {

        private final Filter filter;
        private final FilterChain next;
        
        public FilterChainImpl(Filter filter, FilterChain next) {
            this.filter = filter;
            this.next = next;
        }

        @Override
        public Result before(Context context) {
            return filter.before(next, context);
        }
        
        @Override
        public void after(Context context) {
            filter.after(next, context);
        }
        
        @Override
        public void onException(Exception e) {
            filter.onException(next, e);
        }
    }
    
}
