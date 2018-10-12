package net.javapla.jawn.core.routes;

import java.util.function.Consumer;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.Response;


/**
 * Holding path parameters as well - including user defined
 * 
 * This class cannot hold state within the context of a request, as this class is
 * just a simple representation of a possible route in the system.
 * 
 * @author MTD
 * @implSpec
 *      This class is immutable and thread-safe
 */
public class Route extends InternalRoute {
    private final HttpMethod httpMethod;
    private Class<? extends Controller> controller;
    private final String action;
    private final String actionName;// action without httpMethod
    
    private final FilterChain filterChain;
    
    
    public Route(String uri, HttpMethod method, Class<? extends Controller> controller, String action, String actionName,  FilterChain chain) {
        super(uri);
        this.httpMethod = method;
        this.controller = controller;
        this.action = action;//constructAction(action, method);
        this.actionName = actionName;//StringUtil.blank(action) ? DEFAULT_ACTION_NAME : action;
        
        this.filterChain = chain;
    }
    
    public Route(Route route) {
        this(route.uri, route.httpMethod, route.controller, route.action, route.actionName, route.filterChain);
    }
    
    // Indicates that this Route does not have any route parameters that needs to be deduced in runtime.
    // E.g. /somecontroller/action is fully qualified
    //      /somecontroller/{action} is not
    public boolean isUrlFullyQualified() {
        return this.parameters.isEmpty() /*|| 
            this.parameters.stream().noneMatch(param -> param.equals(ACTION) || param.equals(CONTROLLER) || param.equals(ID) || param.equals(PACKAGE))*/;
    }
    
    public boolean hasActionSet() {
        return action != null && actionName != null;
    }
    
    /*private Method actionMethod;
    public void setActionMethod(Method method) {
        this.actionMethod = method;
    }
    public Method getActionMethod() {
        return actionMethod;
    }*/
    private Consumer<Controller> controllerAction;
    public void setControllerAction(Consumer<Controller> controllerAction) {
        this.controllerAction = controllerAction;
    }
    public Consumer<Controller> getControllerAction() {
        return controllerAction;
    }
    
    public String getUri() {
        return uri;
    }
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
    public Class<? extends Controller> getController() {
        return controller;
    }
    void replaceController(Class<? extends Controller> controller) {
        this.controller = controller;
    }
    
    /**
     * @return The action on the controller including the HTTP method. E.g. getMovie, postMovie
     */
    public String getAction() {
        return action;//constructAction(action, httpMethod);
    }
    /**
     * @return The name of the route action without any HTTP method prepended
     */
    public String getActionName() {
        return actionName;
    }
    
    public FilterChain getFilterChain() {
        return filterChain;
    }
    
    ResponseFunction func;
    boolean directlyExecutable;
    public void setResponseFunction(ResponseFunction func, boolean directlyExecutable) {
        this.func = func;
        this.directlyExecutable = directlyExecutable;
    }
    public Result executeRouteAndRetrieveResult(Context context) {
        return func.handle(context);
    }
    public boolean isDirectlyExecutable() {
        return directlyExecutable;
    }
    
    /**
     * Matches /index to /index or /me/1 to /person/{id}
     *
     * @return True if the actual route matches a raw rout. False if not.
     */
    public boolean matches(HttpMethod httpMethod, String requestUri) {
        if (this.httpMethod == httpMethod) {
            return matches(requestUri);
        }
        return false;
    }
    
    interface TwoArgAction {
        Object action(Request req, Response resp) throws Throwable;
    }
    
    interface OneArgAction {
        Object action(Request req) throws Throwable;
    }
    
    interface ZeroArgAction {
        Object action() throws Throwable;
    }
    
    @FunctionalInterface
    public interface ResponseFunction {
        Result handle(Context context);
    }
    
    @FunctionalInterface
    public interface ZeroArgResponseFunction {
        Result handle();
    }
    
    @FunctionalInterface
    public interface VoidResponseFunction {
        void handle(Context context);
    }
    
}
