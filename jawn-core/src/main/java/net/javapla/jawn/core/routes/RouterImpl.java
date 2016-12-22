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
import net.javapla.jawn.core.exceptions.ControllerException;
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
    
    
    private final List<RouteBuilder> builders;
    private final FiltersHandler filters;
    private final boolean isDev;
    
    private List<Route> routes;
    private RouteTrie deducedRoutes;
    private ActionInvoker invoker;
    
    
    public RouterImpl(FiltersHandler filters, JawnConfigurations properties) {
        builders = new ArrayList<>();
        this.filters = filters;
        
        this.isDev = properties.isDev();
        
        /*if (!isDev) {
            this.cachedRoutes = new HashMap<>();
            for(HttpMethod method : HttpMethod.values()) {
                cachedRoutes.put(method, new RouteTrie());
            }
        } else {
            cachedRoutes = null;
        }*/
//        cachedGetRoutes = new RouteTrie();
    }
    
    public RouteBuilder GET() {
        RouteBuilder bob = RouteBuilder.get();
        builders.add(bob);
        return bob;
    }
    public RouteBuilder POST() {
        RouteBuilder bob = RouteBuilder.post();
        builders.add(bob);
        return bob;
    }
    public RouteBuilder PUT() {
        RouteBuilder bob = RouteBuilder.put();
        builders.add(bob);
        return bob;
    }
    public RouteBuilder DELETE() {
        RouteBuilder bob = RouteBuilder.delete();
        builders.add(bob);
        return bob;
    }
    public RouteBuilder HEAD() {
        RouteBuilder bob = RouteBuilder.head();
        builders.add(bob);
        return bob;
    }

    
    public final Route retrieveRoute(HttpMethod httpMethod, String requestUri/*, ActionInvoker invoker*//*Injector injector*/) throws RouteException {
        // Try with the deduced routes first
        // Only do this if we are not in development
//        if (!isDev) {
            final Route route = deducedRoutes.findExact(requestUri, httpMethod); //exact matches
            if (route != null) return route;
//        }
        
        return calculateRoute(httpMethod, requestUri/*, invoker*//*injector*/);
    }
    
    //TODO we need to have the injector somewhere else.
    //It is not consistent that this particular injector handles implementations from both core and server
    private final Route calculateRoute(HttpMethod httpMethod, String requestUri/*, ActionInvoker invoker*//*Injector injector*/) throws RouteException {
        //if (routes == null) compileRoutes(/*injector*/); // used to throw an illegalstateexception
        
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
    
    public void compileRoutes(ActionInvoker invoker/*Injector injector*/) {
        if (routes != null) return; // used to throw an illegalstateexception
        
        // Build the built in routes
        if (!isDev)
            deducedRoutes = new RoutesDeducer(filters, invoker/*injector*/).deduceRoutesFromControllers().getRoutes();
        else
            deducedRoutes = new RouteTrie();
        this.invoker = invoker;
        
        // Build the user attributed routes
        List<Route> r = new ArrayList<>();
        for (RouteBuilder builder : builders) {
            
            // Routes, not containing wildcards or keywords, should be incorporated into the deduced routes
            Route route = builder.build(filters, invoker/*injector*/);
            if (/*!isDev && */!StringUtil.contains(route.uri,'{')) { //exact match
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
                if (route.func != null) {
                    return route;
                }
                
                // if the route only has an URI defined, then we process the route as if it was an InternalRoute
                if (route.getActionName() == null) {
                    if (route.getController() == null) {
                        Route deferred = deduceRoute(route, httpMethod, requestUri, invoker/*injector*/);
                        if (deferred != null) return deferred;
                        return route;
                    } else {
                        String actionName = deduceActionName(route, requestUri);
                        return RouteBuilder.build(route, actionName);
                    }
                }

                // reload the controller, if we are not in production mode
                if (isDev) {
                    try {
                        // a route might not have the actionName or controller set by the user, so 
                        // we try to infer it from the URI
                        String actionName = deduceActionName(route, requestUri);
                        String controllerName = deduceControllerName(route, requestUri);
                        
                        RouteBuilder bob = loadController(controllerName, httpMethod, route.uri, actionName, false);
                        
                        return bob.build(filters, invoker/*injector*/); // this ought not throw, as we already have done the checks for action presence
                    } catch (CompilationException | ClassLoadException ignore) {
                        // If anything happens during reload, then we simply do not reload
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
    private Route matchStandard(HttpMethod httpMethod, String requestUri, ActionInvoker invoker/*Injector injector*/) throws ClassLoadException {
        // find potential routes
        for (InternalRoute internalRoute : internalRoutes) {
            if (internalRoute.matches(requestUri) ) {
                Route deferred = deduceRoute(internalRoute, httpMethod, requestUri, invoker/*injector*/);
                if (deferred != null) return deferred;
            }
        }
        throw new ClassLoadException("A route for request " + requestUri + " could not be deduced");
    }
    
    private Route deduceRoute(InternalRoute route, HttpMethod httpMethod, String requestUri, ActionInvoker invoker/*Injector injector*/) {
        Map<String, String> params = route.getPathParametersEncoded(requestUri);
        //README it seems wrong that the parameters are calculated at this point and not stored somehow in the resulting Route
        ControllerMeta c = new ControllerMeta(params);
        
        // find a route that actually exists
        try {
            String className = c.getControllerClassName();
            //boolean isDev = injector.getInstance(PropertiesImpl.class).isDev();
            RouteBuilder bob = loadController(className, httpMethod, route.uri, c.getAction(), !isDev);
            
            //TODO cache the routes if !isDev
            return bob.build(filters, invoker/*injector*/); // this might throw if the controller does not have the action
        } catch (ControllerException e) {
            //to() failed - the controller does not contain the corresponding action
        } catch (IllegalStateException e) {
            //build() failed
        } catch(CompilationException | ClassLoadException e) {
            //load class failed
        }
        return null;
    }
    
    private String deduceActionName(Route route, String requestUri) {
        String actionName = route.getActionName();
        if (actionName == null) {
            // try to infer the action
            Map<String, String> params = route.getPathParametersEncoded(requestUri);
            ControllerMeta c = new ControllerMeta(params);
            actionName = c.getAction();
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
                .to(controller, actionName);
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
//            return params.getOrDefault("controller", NewRoute.ROOT_CONTROLLER_NAME);
        }
        public String getAction() {
            String p = params.get("action");
            if (StringUtil.blank(p)) return Constants.DEFAULT_ACTION_NAME;
            return p;
//            return params.getOrDefault("action", NewRoute.DEFAULT_ACTION_NAME);
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
