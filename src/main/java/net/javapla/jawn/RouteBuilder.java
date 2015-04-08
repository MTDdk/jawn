package net.javapla.jawn;

import java.text.MessageFormat;

import net.javapla.jawn.exceptions.ControllerException;

public class RouteBuilder {

    private final HttpMethod httpMethod;// = HttpMethod.GET; //defaults
    private String uri;
    private AppController controller;
    private String action;
    private Class<? extends AppController> type;
    
    private RouteBuilder(HttpMethod m) {
        httpMethod = m;
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
     * @param uri what was specified in the  RouteConfig class
     */
    public RouteBuilder route(String uri) {
        this.uri = uri;
        return this;
    }
    
    public <T extends AppController> RouteBuilder to(Class<T> type) {
        this.type = type;
        try {
            this.controller = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }
    
    public <T extends AppController> RouteBuilder to(Class<T> type, String action) /*throws ControllerException*/{
        try {
            this.controller = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
        this.type = type;
        this.action = action;
        
        return this;
    }
    
    public RouteBuilder to(AppController controller, String action) /*throws ControllerException*/ {
//        // verify that the controller has the corresponding action
//        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
//        if ( ! controller.isAllowedAction(NewRoute.constructAction(action, httpMethod)) )
//            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, NewRoute.constructAction(action, httpMethod)));
        
        this.controller = controller;
        this.type = controller.getClass();
        this.action = action;
        
        return this;
    }
    
    /**
     * Name of action to which a route is mapped.
     *
     * @param action name of action.
     * @return instance of {@link RouteBuilder}.
     */
    
    //TODO consider to implement the action as a part of the to() - by doing this it should be possible to do a 
    /*public NewRouteBuilder action(String action) {
        if (controller != null) {
            
        }
        this.action = action;
        return this;
    }*/
    
    /**
     * Build the route
     * @return
     */
    public Route build() throws IllegalStateException, ControllerException {
        if (controller == null) throw new IllegalStateException("Route not with a controller");
        if (uri == null) throw new IllegalStateException("Route is not specified");
        
        Route route = new Route(uri, httpMethod, controller, action);
        
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
        if ( ! controller.isAllowedAction(route.getAction()) )
            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, route.getAction()));
        
        return route;
    }
    
}
