package net.javapla.jawn.core.routes;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.routes.Route.ResponseFunction;
import net.javapla.jawn.core.routes.Route.VoidResponseFunction;
import net.javapla.jawn.core.routes.Route.ZeroArgResponseFunction;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

public class RouteBuilder {
    
    private final HttpMethod httpMethod;
    private String uri;
    private String actionName; // is not prefixed with the http method
    private String action; // the controller action prefixed with http method
    private Class<? extends Controller> controller;
//    private Result response;
    
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
    
    public static RouteBuilder options() {
        return new RouteBuilder(HttpMethod.OPTIONS);
    }
    
    public static RouteBuilder method(HttpMethod method) {
        return new RouteBuilder(method);
    }
    
    public static RouteBuilder route(Route route) {
        RouteBuilder builder = new RouteBuilder(route.getHttpMethod()).route(route.getUri());
        builder.action = route.getAction();
        builder.actionName = route.getActionName();
        
        return builder;
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
        //setAction(Constants.DEFAULT_ACTION_NAME);
        return this;
    }
    
    public RouteBuilder to(Class<? extends Controller> controller, String action) throws RouteException {
        this.controller = controller;
        setAction(action);
        
        // verify that the controller has the corresponding action
        if ( ! ActionInvoker.isAllowedAction(controller, action))
            throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", controller, action));
        
        return this;
    }
    
    public RouteBuilder to(Controller controller, String action) throws RouteException {
        this.controller = controller.getClass();
        setAction(action);
        
        // verify that the controller has the corresponding action
        if ( ! ActionInvoker.isAllowedAction(controller.getClass(), action))
            throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", controller, action));
        
        
        return this;
    }
    
    private RouteBuilder setAction(String action) {
        this.action = action;
        this.actionName = constructActionName(action, httpMethod);
        return this;
    }
    
    ResponseFunction func;
    public RouteBuilder with(Result response) {
        this.func = context -> response;
        return this;
    }
    public RouteBuilder with(ResponseFunction func) {
        this.func = func;
        return this;
    }
    public RouteBuilder with(ZeroArgResponseFunction func) {
        this.func = (context) -> func.handle();
        return this;
    }
    public RouteBuilder with(VoidResponseFunction func) {
        this.func = (context) -> {func.handle(context); return null; };
        return this;
    }
    Consumer<Controller> ext;
    @SuppressWarnings("unchecked")
    public <C extends Controller> RouteBuilder to(Class<C> controller, Consumer<C> ext) {
        this.ext = (Consumer<Controller>) ext;
        this.controller = controller;
        setAction(Constants.DEFAULT_ACTION_NAME);
        return this;
    }
    

    /**
     * Build the route.
     * @return the built route
     */
    public Route build(FiltersHandler filters, ActionInvoker invoker) throws IllegalStateException {
        if (uri == null) throw new IllegalStateException("Route is not specified");
        
        // Find all possible filters
        LinkedList<Filter> list = new LinkedList<>();
        if (controller != null) {
            // First the controller specific
            list.addAll(filters.compileFilters(controller));
            // Then all specific to the action
            //list.addAll(filters.compileFilters(controller, action));
        } else {
            list.addAll(filters.compileGlobalFilters());
        }
        
        Route route = new Route(uri, httpMethod, controller, action, actionName, buildFilterChain(list));
        
        
        // experimental
        if (ext != null) {
            route.setControllerAction(ext);
            route.setResponseFunction(invoker::testingExecute, true);
        } else if (func != null) {
            route.setResponseFunction(func, true);
        } else if (controller != null) {
            try {
                //route.setActionMethod(controller.getMethod(action));
                route.setResponseFunction(invoker::executeAction, false);
            } catch (/*NoSuchMethodException | */SecurityException ignore) {}
        }
        
        return route;
    }
    
    final static Route build(Route route, String actionName) {
        String action = constructAction(actionName, route.getHttpMethod());
        if ( ! ActionInvoker.isAllowedAction(route.getController(), action))
            throw new RouteException(MessageFormat.format("{0} does not contain a method called {1}", route.getController(), action));
        
        Route newRoute = new Route(route.uri, route.getHttpMethod(), route.getController(), action, actionName, route.getFilterChain());
        try {
            //newRoute.setActionMethod(route.getController().getMethod(action));
            //experimental
            newRoute.setResponseFunction(route.func, false);
        } catch (/*NoSuchMethodException | */SecurityException ignore) {}
        
        return newRoute;
    }
    
    /*final static Route build(Route route, Class<? extends Controller> type) {
        Route newRoute = new Route(route);
        try {
            newRoute.
            newRoute.setActionMethod(route.getController().getMethod(action));
            //experimental
            newRoute.setResponseFunction(route.func, false);
        } catch (NoSuchMethodException | SecurityException ignore) {}
    }*/
    
    private static final FilterChain filterChainEnd = new FilterChainEnd();
    private static final FilterChain buildFilterChain(LinkedList<Filter> filters) {
        if (filters.isEmpty()) {
            return filterChainEnd;
        } else {
            Filter filter = filters.remove(0);
            return new FilterChainImpl(filter, buildFilterChain(filters));
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
    static final String constructAction(String actionName, HttpMethod method) {
        if (StringUtil.blank(actionName)) return Constants.DEFAULT_ACTION_NAME;
        if (Constants.DEFAULT_ACTION_NAME.equals(actionName) && method == HttpMethod.GET)
            return actionName;
        return method.name().toLowerCase() + StringUtil.camelize(actionName.replace('-', '_'), true);
    }
    
    /**
     * Strips the {@link HttpMethod} part of the action
     * 
     * @param action
     * @param method
     * @return
     */
    private static final String constructActionName(String action, HttpMethod method) {
        if (StringUtil.blank(action)) return Constants.DEFAULT_ACTION_NAME;
        if (Constants.DEFAULT_ACTION_NAME.equals(action) && method == HttpMethod.GET) return action;
        
        if (!action.startsWith(method.name().toLowerCase()))
            throw new RouteException(MessageFormat.format("action [{0}] does not start with correct HttpMethod [{1}]", action, method));
        
        return StringUtil.underscore(action.substring(method.name().length()));
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
        
        FilterChainImpl(Filter filter, FilterChain next) {
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
