package net.javapla.jawn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.exceptions.CompilationException;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.RouteException;
import net.javapla.jawn.util.StringUtil;

import com.google.inject.Inject;

public class NewRouter {
    
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
    
    
    private final List<NewRouteBuilder> builders;
    
    private List<NewRoute> routes;
    
    @Inject
    public NewRouter() {
        builders = new ArrayList<>();
    }
    
    public NewRouteBuilder GET() {
        NewRouteBuilder bob = NewRouteBuilder.get();
        builders.add(bob);
        return bob;
    }
    public NewRouteBuilder POST() {
        NewRouteBuilder bob = NewRouteBuilder.post();
        builders.add(bob);
        return bob;
    }
    public NewRouteBuilder PUT() {
        NewRouteBuilder bob = NewRouteBuilder.put();
        builders.add(bob);
        return bob;
    }
    public NewRouteBuilder DELETE() {
        NewRouteBuilder bob = NewRouteBuilder.delete();
        builders.add(bob);
        return bob;
    }
    public NewRouteBuilder HEAD() {
        NewRouteBuilder bob = NewRouteBuilder.head();
        builders.add(bob);
        return bob;
    }
    
    
    public NewRoute getRoute(HttpMethod httpMethod, String requestUri) {
        if (routes == null) throw new IllegalStateException("Routes have not been compiled");//README could be compiling instead
        
        // go through custom user routes
        for (NewRoute route : routes) {
            if (route.matches(httpMethod, requestUri)) {
                return route;
            }
        }
        
        // nothing is found - try to deduce a route from controllers
        try {
            NewRoute route = matchStandard(httpMethod, requestUri);
            return route;
        } catch (ClassLoadException e) {
            // a route could not be deduced
        }
        
        throw new RouteException("Failed to map resource to URI: " + requestUri);
    }
    
    public void compileRoutes() {
        if (routes != null) throw new IllegalStateException("Routes already compiled");//README could just return without throw
        List<NewRoute> r = new ArrayList<>();
        for (NewRouteBuilder builder : builders) {
            r.add(builder.build());
        }
        routes = r;//README probably some immutable
    }
    
    public List<NewRoute> getRoutes() {
        if (routes == null) throw new IllegalStateException("Routes have not been compiled");//README could be compiling instead
        return routes;
    }
    
    
    private NewRoute matchStandard(HttpMethod httpMethod, String requestUri) throws ClassLoadException {
        NewRouteBuilder bob = NewRouteBuilder.method(httpMethod);
        
        // find potential routes
        for (InternalRoute internalRoute : internalRoutes) {
            if (internalRoute.matches(requestUri) ) {
                Map<String, String> params = internalRoute.getPathParametersEncoded(requestUri);
                //README it seems wrong that the parameters are calculated at this point and not stored somehow in the resulting Route
                Controller c = new Controller(params);
                
                // find a route that actually exists
                try {
                    String className = c.getControllerClassName();
                    AppController controller = DynamicClassFactory.createInstance(className, AppController.class, false);

                    bob.route(internalRoute.uri);
                    bob.to(controller, c.getAction());
                    return bob.build(); // this might throw if the controller does not have the action
//                    break; // let's just try this one
                } catch (ControllerException e) {
                    //to() failed - the controller does not contain the corresponding action
                } catch (IllegalStateException e) {
                    //build() failed
                } catch(CompilationException | ClassLoadException e) {
                    //load class failed
                }
            }
        }
        throw new ClassLoadException("A route for request " + requestUri + " could not be deduced");
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
            if (StringUtil.blank(p)) return NewRoute.ROOT_CONTROLLER_NAME;
            return p;
//            return params.getOrDefault("controller", NewRoute.ROOT_CONTROLLER_NAME);
        }
        public String getAction() {
            String p = params.get("action");
            if (StringUtil.blank(p)) return NewRoute.DEFAULT_ACTION_NAME;
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
