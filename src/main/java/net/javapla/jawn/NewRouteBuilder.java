package net.javapla.jawn;

import java.text.MessageFormat;

import net.javapla.jawn.exceptions.ControllerException;

public class NewRouteBuilder {

    private final HttpMethod httpMethod;// = HttpMethod.GET; //defaults
    private String uri;
    private AppController controller;
    private String action;
    private Class<? extends AppController> type;
    
    private NewRouteBuilder(HttpMethod m) {
        httpMethod = m;
    }
    
    static NewRouteBuilder get() {
        return new NewRouteBuilder(HttpMethod.GET);
    }
    
    static NewRouteBuilder post() {
        return new NewRouteBuilder(HttpMethod.POST);
    }
    
    static NewRouteBuilder put() {
        return new NewRouteBuilder(HttpMethod.PUT);
    }
    
    static NewRouteBuilder delete() {
        return new NewRouteBuilder(HttpMethod.DELETE);
    }
    
    static NewRouteBuilder head() {
        return new NewRouteBuilder(HttpMethod.HEAD);
    }
    
    static NewRouteBuilder method(HttpMethod method) {
        return new NewRouteBuilder(method);
    }
    
    
    /**
     * Used for custom routes
     * @param uri what was specified in the  RouteConfig class
     */
    public NewRouteBuilder route(String uri) {
        this.uri = uri;
        return this;
    }
    
    public <T extends AppController> NewRouteBuilder to(Class<T> type) {
        this.type = type;
        try {
            this.controller = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }
    
    public <T extends AppController> NewRouteBuilder to(Class<T> type, String action) /*throws ControllerException*/{
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
    
    public NewRouteBuilder to(AppController controller, String action) /*throws ControllerException*/ {
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
    public NewRoute build() throws IllegalStateException, ControllerException {
        if (controller == null) throw new IllegalStateException("Route not with a controller");
        if (uri == null) throw new IllegalStateException("Route is not specified");
        
        NewRoute route = new NewRoute(uri, httpMethod, controller, action);
        
        // verify that the controller has the corresponding action
        // this could be done at action() and to(), but we cannot be sure of the httpMethod at those points
        if ( ! controller.isAllowedAction(route.getAction()) )
            throw new ControllerException(MessageFormat.format("{0} does not contain a method called {1}", type, route.getAction()));
        
        return route;
    }
    
}
