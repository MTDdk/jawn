package net.javapla.jawn.core.routes;

import java.lang.reflect.Method;

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
    private final String actionName;//without httpMethod
    
    private final FilterChain filterChain;
    
    
    public Route(String uri, HttpMethod method, Class<? extends Controller> controller, String action, String actionName,  FilterChain chain) {
        super(uri);
        this.httpMethod = method;
        this.controller = controller;
        this.action = action;//constructAction(action, method);
        this.actionName = actionName;//StringUtil.blank(action) ? DEFAULT_ACTION_NAME : action;
        
        this.filterChain = chain;
    }
    
    private Method actionMethod;
    public void setActionMethod(Method method) {
        this.actionMethod = method;
    }
    public Method getActionMethod() {
        return actionMethod;
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
    public void setResponseFunction(ResponseFunction func) {
        this.func = func;
    }
    public Result executeRouteAndRetrieveResponse(Context context) {
        return func.handle(context);
    }
    
    /**
     * Matches /index to /index or /me/1 to /person/{id}
     *
     * @return True if the actual route matches a raw rout. False if not.
     */
    public boolean matches(HttpMethod httpMethod, String requestUri) {
        if (this.httpMethod.equals(httpMethod)) {
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
    
}
