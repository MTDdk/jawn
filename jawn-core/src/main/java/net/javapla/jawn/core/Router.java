package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.exceptions.CompilationException;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.spi.Routes;
import net.javapla.jawn.core.util.StringUtil;

import com.google.inject.Injector;

public class Router implements Routes {
    
    private static final List<InternalRoute> internalRoutes;
    static {
        internalRoutes = 
            Collections.unmodifiableList(
                Arrays.asList(
                      new InternalRoute("/{controller}/{action}/{id}")
                    , new InternalRoute("/{controller}/{action}")
                    , new InternalRoute("/{controller}")
                    , new InternalRoute("/{package: .*?}/{controller}/{action}/{id}")
                    , new InternalRoute("/{package: .*?}/{controller}/{action}")
                    , new InternalRoute("/{package: .*?}/{controller}")
                )
            );
    }
    
    
    private final List<RouteBuilder> builders;
    private final Filters filters;
    
    private List<Route> routes;
    
    public Router(Filters filters) {
        builders = new ArrayList<>();
        this.filters = filters;
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
    
    
    //TODO we need to have the injector somewhere else.
    //It is not consistent that this particular injector handles implementations from both core and server
    public Route getRoute(HttpMethod httpMethod, String requestUri, Injector injector) throws RouteException {
        if (routes == null) compileRoutes(injector); // used to throw an illegalstateexception
        
        // go through custom user routes
        for (Route route : routes) {
            if (route.matches(httpMethod, requestUri)) {
                
                // if the route only has an URI defined, then we process the route as if it was an InternalRoute
                if (route.getActionName() == null && route.getController() == null) {
                    Route deferred = deduceRoute(route, httpMethod, requestUri, injector);
                    if (deferred != null) return deferred;
                    return route;
                }

                // reload the controller, if we are not in production mode
                boolean isProd = injector.getInstance(PropertiesImpl.class).isProd();
                if (!isProd) {
                    try {
                        // a route might not have the actionName or controller set by the user, so 
                        // we try to infer it from the URI
                        String actionName = deduceAction(route, requestUri);
                        String controllerName = deduceControllerName(route, requestUri);
                        
                        RouteBuilder bob = loadController(controllerName, httpMethod, route.uri, actionName, false);
                        
                        return bob.build(filters, injector); // this ought not throw, as we already have done the checks for action presence
                    } catch (CompilationException | ClassLoadException ignore) {
                        // If anything happens during reload, then we simply do not reload
                    }
                }
                
                return route;
            }
        }
        
        // nothing is found - try to deduce a route from controllers
        try {
            Route route = matchStandard(httpMethod, requestUri, injector);
            return route;
        } catch (ClassLoadException e) {
            // a route could not be deduced
        }
        
        throw new RouteException("Failed to map resource to URI: " + requestUri);
    }
    
    public void compileRoutes(Injector injector) {
        if (routes != null) return; // used to throw an illegalstateexception
        
        List<Route> r = new ArrayList<>();
        for (RouteBuilder builder : builders) {
            r.add(builder.build(filters, injector));
        }
        routes = r;//README probably some immutable
    }
    
    public List<Route> getRoutes() {
        if (routes == null) throw new IllegalStateException("Routes have not been compiled");
        return routes;
    }
    
    
    private Route matchStandard(HttpMethod httpMethod, String requestUri, Injector injector) throws ClassLoadException {
        
        // find potential routes
        for (InternalRoute internalRoute : internalRoutes) {
            if (internalRoute.matches(requestUri) ) {
                Route deferred = deduceRoute(internalRoute, httpMethod, requestUri, injector);
                if (deferred != null) return deferred;
            }
        }
        throw new ClassLoadException("A route for request " + requestUri + " could not be deduced");
    }
    
    private Route deduceRoute(InternalRoute route, HttpMethod httpMethod, String requestUri, Injector injector) {
        Map<String, String> params = route.getPathParametersEncoded(requestUri);
        //README it seems wrong that the parameters are calculated at this point and not stored somehow in the resulting Route
        Controller c = new Controller(params);
        
        // find a route that actually exists
        try {
            String className = c.getControllerClassName();
            boolean isProd = injector.getInstance(PropertiesImpl.class).isProd();
            RouteBuilder bob = loadController(className, httpMethod, route.uri, c.getAction(), isProd);
            
            //TODO cache the routes if isProd
            return bob.build(filters, injector); // this might throw if the controller does not have the action
        } catch (ControllerException e) {
            //to() failed - the controller does not contain the corresponding action
        } catch (IllegalStateException e) {
            //build() failed
        } catch(CompilationException | ClassLoadException e) {
            //load class failed
        }
        return null;
    }
    
    private String deduceAction(Route route, String requestUri) {
        String actionName = route.getActionName();
        if (actionName == null) {
            // try to infer the action
            Map<String, String> params = route.getPathParametersEncoded(requestUri);
            Controller c = new Controller(params);
            actionName = c.getAction();
        }
        return actionName;
    }
    
    private String deduceControllerName(Route route, String requestUri) {
        String controllerName;
        if (route.getController() == null) {
            // try to infer the controller
            Map<String, String> params = route.getPathParametersEncoded(requestUri);
            Controller c = new Controller(params);
            controllerName = c.getControllerClassName();
        } else  {
            controllerName = route.getController().getName();
        }
        return controllerName;
    }
    
    private RouteBuilder loadController(String controllerName, HttpMethod httpMethod, String uri, String actionName, boolean useCache) throws CompilationException, ClassLoadException {
        Class<? extends ApplicationController> controller = 
                DynamicClassFactory.getCompiledClass(controllerName, ApplicationController.class, useCache);
        RouteBuilder bob = RouteBuilder.method(httpMethod);
        bob.route(uri);
        bob.to(controller, actionName);
        return bob;
    }

    private class Controller {
        public final Map<String,String> params;
        
        public Controller(Map<String,String> params) {
            this.params = params;
        }
        
        public String getPackage() {
            String p = params.get("package");
            if (!StringUtil.blank(p)) return p.replace('/', '.');
            return null;
        }
        public String getController() {
            String p = params.get("controller");
            if (StringUtil.blank(p)) return RouteBuilder.ROOT_CONTROLLER_NAME;
            return p;
//            return params.getOrDefault("controller", NewRoute.ROOT_CONTROLLER_NAME);
        }
        public String getAction() {
            String p = params.get("action");
            if (StringUtil.blank(p)) return RouteBuilder.DEFAULT_ACTION_NAME;
            return p;
//            return params.getOrDefault("action", NewRoute.DEFAULT_ACTION_NAME);
        }
        
        public String getControllerClassName() {
            String controllerName = getController();
            String packagePrefix = getPackage();
            
            String name = controllerName.replace('-', '_');
            String temp = "app.controllers"; //README might be a part of some configuration
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
