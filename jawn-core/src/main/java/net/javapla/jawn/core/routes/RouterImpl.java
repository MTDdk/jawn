package net.javapla.jawn.core.routes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.api.Router;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.exceptions.CompilationException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.reflection.DynamicClassFactory;
import net.javapla.jawn.core.reflection.RoutesDeducer;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.PropertiesConstants;
import net.javapla.jawn.core.util.StringUtil;

public class RouterImpl implements Router {
    
    private static final List<InternalRoute> internalRoutes;
    static {
        internalRoutes = 
            Collections.unmodifiableList(
                Arrays.asList(
                      new InternalRoute("/{controller}/{action}/{id}")
                    , new InternalRoute("/{controller}/{action}")
                    //, new InternalRoute("/{controller}/{id}")
                    , new InternalRoute("/{controller}")
                    , new InternalRoute("/{package: .*?}/{controller}/{action}")
                    //, new InternalRoute("/{package: .*?}/{controller}/{id}")
                    , new InternalRoute("/{package: .*?}/{controller}")
                    , new InternalRoute("/{package: .*?}/{controller}/{action}/{id}")
                )
            );
    }
    
    
    private final ArrayList<RouteBuilder> builders;
    private final FiltersHandler filters;
    private final JawnConfigurations properties;
    
    private List<Route> routes;
    private RouteTrie deducedRoutes;
    private ActionInvoker invoker;
    private boolean isDev;
    
    
    public RouterImpl(List<RouteBuilder> builders, FiltersHandler filters, JawnConfigurations properties) {
        this.builders = new ArrayList<>(builders);
        this.filters = filters;
        this.properties = properties;
    }
    
    @Override
    public final Route retrieveRoute(HttpMethod httpMethod, String requestUri) throws RouteException {
        // Try with the deduced routes first
        // Only do this if we are not in development
        //if (!isDev) {
            final Route route = deducedRoutes.findExact(requestUri, httpMethod); //exact matches
            if (route != null) { 
                if (isDev && !route.isDirectlyExecutable()) {
                    reloadController(route, false);
                }
                return route;
            }
        //}
        
        Route r = calculateRoute(httpMethod, requestUri);
        if (!isDev)
            deducedRoutes.insert(requestUri, r);
        return r;
    }
    
    //TODO we need to have the injector somewhere else.
    //It is not consistent that this particular injector handles implementations from both core and server
    private final Route calculateRoute(HttpMethod httpMethod, String requestUri/*, ActionInvoker invoker*//*,Injector injector*/) throws RouteException {
        if (routes == null) throw new IllegalStateException("Routes have not been compiled. Call #compileRoutes() first");
        
        final Route route = matchCustom(httpMethod, requestUri);
        if (route != null) return route;
        
        // nothing is found - try to deduce a route from controllers
        //if (isDev) {
            try {
                return matchStandard(httpMethod, requestUri, invoker/*injector*/);
            } catch (ClassLoadException e) {
                // a route could not be deduced
            }
        //}
        
        throw new RouteException("Failed to map resource to URI: " + requestUri);
    }
    
    public void compileRoutes(ActionInvoker invoker) {
        if (routes != null) return; // previously threw an illegalstateexception
        isDev = properties.isDev();
        
        // Build the built in routes
        if (!isDev)
            deducedRoutes = new RoutesDeducer(filters, invoker).deduceRoutesFromControllers(PropertiesConstants.CONTROLLER_PACKAGE).getRoutes();
        else
            deducedRoutes = new RouteTrie();
        this.invoker = invoker;
        
        // Build the user attributed routes
        ArrayList<Route> r = new ArrayList<>();
        for (RouteBuilder builder : builders) {
            
            // Routes not containing wildcards or keywords should be incorporated into the deduced routes
            Route route = builder.build(filters, invoker);
            if (/*!isDev && */route.isUrlFullyQualified()) {
                deducedRoutes.insert(route.uri, route);
                System.out.println("deduced : " + route);
            } else {
                r.add(route);
            }
        }
        routes = Collections.unmodifiableList(r);
    }
    
    public List<Route> getRoutes() {
        if (routes == null) throw new IllegalStateException("Routes have not been compiled");
        return routes;
    }
    
    private final Route matchCustom(final HttpMethod httpMethod, final String requestUri) {
        // go through custom user routes
        // TODO surely we want to store the routes based on HttpMethod, so we do not need to go through ALL of them
        for (Route route : routes) {
            if (route.matches(httpMethod, requestUri)) {
                
                // TODO experimental
                if (route.isDirectlyExecutable()) {
                    return route;
                }
                
                // TODO something is inherently wrong here.. All of this ought to be the responsibility of ActionInvoker or some middleman.
                // It definitely should not be here, but a part of the execution of the route instead, and set during RouteBuilder#build
                
                // reload the controller, if we are not in production mode
                if (isDev) {
                    try {
                        if (route.hasActionSet()) {
                            return reloadController(route, false);
                        }
                        
                        // a route might not have the actionName or controller set by the user, so 
                        // we try to infer it from the URI
                        String actionName = deduceActionName(route, requestUri);
                        String controllerName = deduceControllerName(route, requestUri);
                        
                        RouteBuilder bob = loadController(controllerName, httpMethod, route.uri, actionName, false);
                        
                        return bob.build(filters, invoker); // this ought not throw, as we already have done the checks for action presence
                    } catch (CompilationException | ClassLoadException ignore) {
                        // If anything happens during reload, then we simply do not reload
                    }
                }
                
                // if the route only has an URI defined, then we process the route as if it was an InternalRoute
                if (route.getActionName() == null || !route.isUrlFullyQualified()) {
                    if (route.getController() == null) {
                        Route deferred = deduceRoute(route, httpMethod, requestUri, invoker);
                        if (deferred != null) return deferred;
                        return route;
                    } else {
                        if (route.isUrlFullyQualified())
                            return route;
                        
                        String actionName = deduceActionName(route, requestUri);
                        return RouteBuilder.build(route, actionName);
                    }
                }
                
                return route;
            }
        }
        
        return null;
    }
    
    /**
     * Only to be used in DEV mode
     * @param httpMethod
     * @param requestUri
     * @param injector
     * @return
     * @throws ClassLoadException
     */
    private Route matchStandard(HttpMethod httpMethod, String requestUri, ActionInvoker invoker) throws ClassLoadException {
        // find potential routes
        for (InternalRoute internalRoute : internalRoutes) {
            if (internalRoute.matches(requestUri) ) {
                Route deferred = deduceRoute(internalRoute, httpMethod, requestUri, invoker);
                if (deferred != null) return deferred;
            }
        }
        throw new ClassLoadException("A route for request " + requestUri + " could not be deduced");
    }
    
    private Route deduceRoute(InternalRoute route, HttpMethod httpMethod, String requestUri, ActionInvoker invoker) {
        Map<String, String> params = route.getPathParametersEncoded(requestUri);
        //README it seems wrong that the parameters are calculated at this point and not stored somehow in the resulting Route
        ControllerMeta c = new ControllerMeta(params);
        
        // find a route that actually exists
        try {
            String className = c.getControllerClassName();
            RouteBuilder bob = loadController(className, httpMethod, route.uri, c.getActionName(), !isDev);
            
            //TODO cache the routes if !isDev
            return bob.build(filters, invoker); // this might throw if the controller does not have the action
        } catch (IllegalStateException e) {
            //build() failed
        } catch(CompilationException | ClassLoadException e) {
            //load class failed
        }
        return null;
    }
    
    private String deduceActionName(Route route, String requestUri) {
        String actionName = route.getActionName();
        if (actionName == null && !route.isUrlFullyQualified()) {
            // try to infer the action
            Map<String, String> params = route.getPathParametersEncoded(requestUri);
            ControllerMeta c = new ControllerMeta(params);
            actionName = c.getActionName();
        }
        return actionName;
    }
    
    //TODO do not deduce action and controllername seperately (even though it is only during DEV)
    private String deduceControllerName(Route route, String requestUri) {
        String controllerName;
        if (route.getController() == null) {
            // try to infer the controller
            Map<String, String> params = route.getPathParametersEncoded(requestUri);
            ControllerMeta c = new ControllerMeta(params);
            controllerName = c.getControllerClassName();
        } else  {
            controllerName = route.getController().getName();
        }
        return controllerName;
    }
    
    private RouteBuilder loadController(
            String controllerName, 
            HttpMethod httpMethod, 
            String uri, 
            String actionName, 
            boolean useCache) throws CompilationException, ClassLoadException {
        
        Class<? extends Controller> controller = DynamicClassFactory.getCompiledClass(controllerName, Controller.class, useCache);
        
        return RouteBuilder
                .method(httpMethod)
                .route(uri)
                .to(controller, RouteBuilder.constructAction(actionName, httpMethod));
    }
    
    private Route reloadController(Route route, boolean useCache) {
        try {
            route.replaceController(DynamicClassFactory.getCompiledClass(route.getController().getName(), Controller.class, useCache));
        } catch (CompilationException | ClassLoadException e) {
            throw new RouteException("Failed to reload Controller " + route.getController());
        }
        return route;
    }
    
    private final class ControllerMeta {
        public final Map<String,String> params;
        
        public ControllerMeta(Map<String,String> params) {
            this.params = params;
        }
        
        public String getPackage() {
            String p = params.get("package");
            if (!StringUtil.blank(p)) return p.replace('/', '.');
            return null;
        }
        public String getController() {
            String p = params.get("controller");
            if (StringUtil.blank(p)) return Constants.ROOT_CONTROLLER_NAME;
            return p;
        }
        public String getActionName() {
            String p = params.get("action");
            if (StringUtil.blank(p)) return Constants.DEFAULT_ACTION_NAME;
            return p;
        }
        
        public String getControllerClassName() {
            String controllerName = getController();
            String packagePrefix = getPackage();
            
            String name = controllerName.replace('-', '_');
            String temp = PropertiesConstants.CONTROLLER_PACKAGE;
            if (packagePrefix != null) {
                temp += "." + packagePrefix;
            }
            return temp + "." + StringUtil.camelize(name) + "Controller";
        }
        
        @Override
        public String toString() {
            return params.toString();
        }
    }
}
