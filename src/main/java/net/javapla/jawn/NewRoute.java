package net.javapla.jawn;

import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.util.StringUtil;

/**
 * Holding path parameters as well - including user defined
 * 
 * This class cannot hold state within the context of a request, as this class is
 * just a simple representation of a possible route in the system.
 *  
 * @author MTD
 */
public class NewRoute extends InternalRoute {
    public static final String ROOT_CONTROLLER_NAME = "index";//README could be set in filter.init()
    public static final String DEFAULT_ACTION_NAME = "index";
    
    
    private final HttpMethod httpMethod;
    private AppController controller;
    private final String action;
    private final String actionName;//without httpMethod
    
    
    public NewRoute(String uri, HttpMethod method, AppController controller, String action) {
        super(uri);
        this.httpMethod = method;
        this.controller = controller;
        this.action = constructAction(action, method);
        this.actionName = StringUtil.blank(action) ? DEFAULT_ACTION_NAME : action;
    }
    
    public String getUri() {
        return uri;
    }
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
    public AppController getController() {
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
    
    
    /**
     * Matches /index to /index or /me/1 to /person/{id}
     *
     * @return True if the actual route matches a raw rout. False if not.
     */
    public boolean matches(HttpMethod httpMethod, String requestUri) {
        if (this.httpMethod.equals(httpMethod)) {
            boolean matches = matches(requestUri);
            return matches;
        }
        return false;
    }

    //README if the Route always would contain the constructed action, this method could be moved into the RouteBuilder along with the static DEFAULT_NAME
    private String constructAction(String action, HttpMethod method) {
        if (StringUtil.blank(action)) return DEFAULT_ACTION_NAME;
        if (DEFAULT_ACTION_NAME.equals(action))
            return action;
        return method.name().toLowerCase() + StringUtil.camelize(action.replace('-', '_'), true);
    }
    
    void reloadController() {
        try {
            //reload
            controller = loadController(controller.getClass().getName());
        } catch (ClassLoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
//    /**
//     * This method does not do any decoding / encoding.
//     *
//     * If you want to decode you have to do it yourself.
//     *
//     * Most likely with:
//     * http://docs.oracle.com/javase/6/docs/api/java/net/URI.html
//     *
//     * @param uri The whole encoded uri.
//     * @return A map with all parameters of that uri. Encoded in => encoded out.
//     */
//    public Map<String, String> getPathParametersEncoded(String uri) {
//        return getPathParametersEncoded(uri, regex.matcher(uri), this.parameters);
//    }
    
}
