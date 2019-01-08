package net.javapla.jawn.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.exceptions.ActionNotFoundException;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.ParsableException;
import net.javapla.jawn.core.exceptions.PathNotFoundException;
import net.javapla.jawn.core.exceptions.WebException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.FormItem;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.http.Session;
import net.javapla.jawn.core.images.ImageHandlerBuilder;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.routes.RouterHelper;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.core.templates.TemplateEngineOrchestrator;
import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.ConvertUtil;
import net.javapla.jawn.core.util.HttpHeaderUtil;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.ResponseStreamWriter;
import net.javapla.jawn.core.util.StringUtil;

/**
 * Subclass this class to create application controllers. A controller is a main component of a web
 * application. Its main purpose in life is to process web requests. 
 *
 * @author MTD
 */
public abstract class Controller implements ResultHolder {
    
 // Standard behaviour is to look for HTML template
    protected Result result = ResultBuilder.ok().contentType(MediaType.TEXT_HTML);
    
    @Override
    public void setControllerResult(Result r) {
        result = r;
    }
    public Result getControllerResult() {
        if (layout() != null)
            result.layout(layout());
        return result;
    }
    
    protected String layout() {
        return null;
    }
    
    private Context context;
    private Injector injector;
    
    public void init(Context context, Injector injector) {
        this.context = context;
        this.injector = injector;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    protected Logger log() {
        return logger;
    }


    /*protected ResultBuilder respond() {
        return result;
    }
    protected void request() {
        
    }*/
    
    // index is always available
    public void index() { };


    /**
     * Renders results with a template.
     * This method is called from within an action execution.
     *
     * This call must be the last call in the action. All subsequent calls to assign values, render or respond will generate
     * {@link IllegalStateException}.
     *
     * @param template - template name, can be "list"  - for a view whose name is different than the name of this action, or
     *             "/another_controller/any_view" - this is a reference to a view from another controller. The format of this
     * parameter should be either a single word or two words separated by slash: '/'. If this is a single word, than
     * it is assumed that template belongs to current controller, if there is a slash used as a separator, then the
     * first word is assumed to be a name of another controller.
     * @return 
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected NewRenderBuilder render(String template) {
        String targetTemplate = template.startsWith("/")? template: RouterHelper.getReverseRouteFast(getClass())
                + "/" + template;

        return internalRender(targetTemplate);
    }

    /**
     * Use this method in order to override a layout, status code, and content type.
     * @return 
     *
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected NewRenderBuilder render(){
        String template = RouterHelper.getReverseRouteFast(getClass()) + "/" + getRoute().getActionName();
        return internalRender(template);
    }


    /**
     * Renders results with a template.
     *
     * This call must be the last call in the action.
     *
     * @param template - template name, must be "absolute", starting with slash,
     * such as: "/controller_dir/action_template".
     * @param values map with values to pass to view. 
     * @return 
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected NewRenderBuilder render(String template, Map<String, Object> values) {
        view(values);
        return internalRender(template);
    }
    
    private NewRenderBuilder internalRender(String template) {
        result.template(template);
        return new NewRenderBuilder(result);
    }


    protected static class NewRenderBuilder {
        private final Result response;
        NewRenderBuilder(Result response) {
            this.response = response;
        }
        
        public NewRenderBuilder noLayout() {
            response.layout(null);
            return this;
        }
        
        /**
         * If the parameter does not end with ".html", then ".html" is automatically appended, as that is the standard
         * layout ending.
         * @param layout
         * @return
         */
        public NewRenderBuilder layout(String layout) {
            response.layout(layout);
            return this;
        }
        
        public NewRenderBuilder template(String template) {
            response.template(template);
            return this;
        }
    }

    protected String servletPath() {
        return path();
    }

    protected boolean isContentType(String type) {
        return contentType().contains(type);
    }
    
    protected boolean isJson() {
        return contentType().contains(MediaType.APPLICATION_JSON);
    }


    /**
     * Returns hardcoded value "text/html". Override this method to set default content type to a different value across
     * all actions in controller and its subclasses. This is a convenient method for building REST webservices. You can set
     * this value once to "text/json", "text/xml" or whatever else you need.
     *
     * @return hardcoded value "text/html"
     */
    protected String getContentType() {
        return "text/html";
    }

    /**
     * Checks if the action supports an HTTP method, according to its configuration.
     *
     * @param actionMethodName name of action method.
     * @param httpMethod http method
     * @return true if supports, false if does not.
     */
    public boolean actionSupportsHttpMethod(String actionMethodName, HttpMethod httpMethod) {
        return standardActionSupportsHttpMethod(actionMethodName, httpMethod);
    }

    protected boolean standardActionSupportsHttpMethod(String actionMethodName, HttpMethod httpMethod){
        for (HttpMethod m : allowedMethodsForAction(actionMethodName)) {
            if (m == httpMethod)
                return true;
        }
        return false;
    }
    
    /**
     * Controllers can override this method to return encoding they require. Encoding set in method {@link #setEncoding(String)}
     * trumps this setting.
     *
     * @return null. If this method is not overridden and encoding is not set from an action or filter,
     * encoding will be set according to container implementation.
     */
    protected String getEncoding(){
        return null;
    }
    
    /**
     * @author MTD
     * @param actionMethodName
     * @return
     */
    protected boolean isAllowedAction(String actionMethodName) {
        //MTD: find the method name case insensitive
        String lowerCaseActionName = actionMethodName.toLowerCase();
        for (Method method : getClass().getMethods()) {
            if (lowerCaseActionName.equals( method.getName().toLowerCase() )) {
                return true;
            }
        }
        return false;
    }

    protected List<HttpMethod> allowedMethodsForAction(String actionMethodName) {
        try {
            /*for (Method method : getClass().getMethods()) {
                if (method.getName().toLowerCase().endsWith(actionMethodName)) {
                    //TODO finish
                }
            }*/
            
            Method method = getClass().getMethod(actionMethodName);
            Annotation[] annotations = method.getAnnotations();

            //default behavior: GET method!
            if (annotations.length == 0) {
                return Collections.singletonList(HttpMethod.GET);
            } else {
                List<HttpMethod> res = new ArrayList<HttpMethod>();
                for (Annotation annotation : annotations) {
                    res.add(HttpMethod.valueOf(annotation.annotationType().getSimpleName()));
                }
                return res;
            }
        } catch (NoSuchMethodException e) {
            throw new ActionNotFoundException(e);
        }
    }
    
    protected Map<String, Object> values() {
//        return context.getViewObjects();//getValues();
//        return context.getNewControllerResponse().getViewObjects();
        return result.getViewObjects();
    }

    /**
     * Assigns value that will be passed into view.
     *
     * @param name name of object to be passed to view
     * @param value object to be passed to view
     */
    protected void view(String name, Object value) {
        result.addViewObject(name, value);
    }


    /**
     * Convenience method, calls {@link #view(String, Object)} internally.
     * The keys in the map are converted to String values.
     *
     * @param values map with values to pass to view.
     */
    protected void view(Map<String, Object> values){
        for(String key:values.keySet() ){
            view(key, values.get(key));
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void view(Object value) {
        if (value instanceof Map) {
            view((Map<String, Object>)value);
        } else if (value instanceof String) {
            view(value, value);
        } else if (value instanceof Object[]) {
            throw new ControllerException("Name of the value could not be guessed");
        } else {
            view(value.getClass().getSimpleName(), value);
        }
    }
    
    /**
     * Convenience method, calls {@link #view(Map)} internally.
     *
     * @param values An even list of key/value pairs
     */
    protected void view(Object ... values) {
        view(CollectionUtil.map(values));
    }

    /**
     * Convenience method, takes in a map of values to flash.
     *
     * @see #flash(String, String)
     *
     * @param values values to flash.
     */
    protected void flash(Map<String, String> values){
        for(Object key:values.keySet() ){
            flash(key.toString(), values.get(key));
        }
    }

    /**
     * Sets a flash name for a flash with  a body.
     * Here is a how to use a tag with a body:
     *
     * <i>flash</i> keyword
     *
     * <pre>
     * $if(flash.&lt;name&gt;)$
     *   &lt;div class=&quot;warning&quot;&gt;$flash.&lt;name&gt;$&lt;/div&gt;
     * $endif$
     * </pre>
     *
     * If body refers to variables (as in this example), then such variables need to be passed in to the template as usual using
     * the {@link #view(String, Object)} method.
     *
     * @param name name of a flash
     */
    protected void flash(String name){
        flash(name, name);
    }
    
    /**
     * Sends value to flash. Flash survives one more request.  Using flash is typical
     * for POST->REDIRECT->GET pattern,
     *
     * @param name name of value to flash
     * @param value value to live for one more request in current session.
     */
    protected void flash(String name, String value) {
        /*if (session().get(Context.FLASH_SESSION_KEYWORD) == null) {
            session().put(Context.FLASH_SESSION_KEYWORD, new HashMap<String, Object>());
        }
        ((Map<String, Object>) session().get(Context.FLASH_SESSION_KEYWORD)).put(name, value);*/
        //context.setFlash(name, value);
        context.getFlash().put(name, value);
    }
    

    protected final ResultBuilder respond() {
        return new ResultBuilder(this);
    }

    
    /**
     * Redirects to a an action of this controller, or an action of a different controller.
     * This method does not expect a full URL.
     * 
     * Sets the 'Location' header to the specified <code>path</code>
     *
     * @param path - expected to be a path within the application.
     * @return instance of {@link HttpSupport.HttpBuilder} to accept additional information.
     */
    protected void redirect(String path) {
        result = ResultBuilder.redirect(path);
    }

    /**
     * Redirects to another URL (usually another site).
     *
     * @param url absolute URL: <code>http://domain/path...</code>.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected void redirect(URL url) {
        redirect(url.toString());
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the <code>defaultReference</code>.
     *
     * @param defaultReference where to redirect - can be absolute or relative; this will be used in case
     * the request does not provide a "Referrer" header.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected void redirectToReferrer(String defaultReference) {
        String referrer = context.requestHeader("Referer");
        referrer = referrer == null? defaultReference: referrer;
        redirect(referrer);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the root of the application.
     *
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected void redirectToReferrer() {
        String referrer = context.requestHeader("Referer");
        referrer = referrer == null? context.contextPath()/*injector.getInstance(DeploymentInfo.class).getContextPath()*/ : referrer;
        redirect(referrer);
    }


    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link Controller}
     * @param action action to redirect to.
     * @param id id to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends Controller> void redirect(Class<T> controllerClass, String action, Object id){
        redirect(controllerClass, CollectionUtil.map("action", action, "id", id));
    }

    // always recurses..
//    /**
//     * Convenience method for {@link #redirect(Class, java.util.Map)}.
//     *
//     * @param controllerClass controller class where to send redirect.
//     * @param <T> class extending {@link Controller}
//     * @param id id to redirect to.
//     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
//     */
//    protected <T extends Controller> /*HttpBuilder*/void redirect(Class<T> controllerClass, Object id){
//        /*return */redirect(controllerClass, CollectionUtil.map("id", id));
//    }
    
    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link Controller}
     * @param action action to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends Controller> void redirect(Class<T> controllerClass, String action){
        redirect(controllerClass, CollectionUtil.map("action", action));
    }

    /**
     * Redirects to the same controller, and action "index". This is equivalent to
     * <pre>
     *     redirect(BooksController.class);
     * </pre>
     * given that the current controller is <code>BooksController</code>.
     *
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected void redirect() {
        redirect(getRoute().getController());
    }
    
    /**
     * @see #redirect()
     * 
     * @param params map with request parameters.
     * @return
     * @author MTD
     */
    protected void redirect(Map<String, String> params) {
        redirect(getRoute().getController(), params);
    }

    /**
     * Redirects to given controller, action "index" without any parameters.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link Controller}
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends Controller> void redirect(Class<T> controllerClass){
        redirect(controllerClass, new HashMap<String, String>(0));
    }

    /**
     * Redirects to a controller, generates appropriate redirect path. There are two keyword keys expected in
     * the params map: "action" and "id". Both are optional. This method will generate appropriate URLs for regular as
     * well as RESTful controllers. The "action" and "id" values in the map will be treated as parts of URI such as:
     * <pre>
     * /controller/action/id
     * </pre>
     * for regular controllers, and:
     * <pre>
     * /controller/id/action
     * </pre>
     * for RESTful controllers. For RESTful controllers, the action names are limited to those described in
     * {@link net.javapla.jawn.annotations.RESTful} and allowed on a GET URLs, which are: "edit_form" and "new_form".
     *
     * <p>
     * The map may contain any number of other key/value pairs, which will be converted to a query string for
     * the redirect URI. Example:
     * <p>
     * Method:
     * <pre>
     * redirect(app.controllers.PersonController.class,  org.javalite.common.Collections.map("action", "show", "id", 123, "format", "json", "restrict", "true"));
     * </pre>
     * will generate the following URI:
     * <pre>
     * /person/show/123?format=json&amp;restrict=true
     * </pre>
     *
     * This method will also perform URL - encoding of special characters if necessary.
     *
     *
     * @param controllerClass controller class
     * @param <T> class extending {@link Controller}
     * @param params map with request parameters.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends Controller> /*HttpBuilder*/void redirect(Class<T> controllerClass, Map<String, String> params){
        String controllerPath = RouterHelper.getReverseRouteFast(controllerClass);
        String contextPath = context.contextPath();
        String action = params.get("action") != null? params.get("action") : null;
        String id = params.get("id") != null? params.get("id") : null;
        params.remove("action");
        params.remove("id");//TODO make a damn class to hold "action", "id", etc so we can have some type safety
        
        //MTD
//        String lang = language() != null ? "/" + language() : "";
        String anchor = params.get("#") != null ? "#" + params.get("#") : "";
        params.remove("#");

        String uri = contextPath +/*injector
            .getInstance(DeploymentInfo.class)
            .translateIntoContextPath(*//*+ lang + */RouterHelper.generate(controllerPath, action, id, params) + anchor/*)*/;
        redirect(uri);
    }
    
    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    /*protected List<Param> params(String name) {
        if (name.equals("id")) {
            String id = getIdString();
            return id != null ? Arrays.asList(new Param(id)) : Collections.emptyList();
        } else {
            String[] values = context.requestParameterValues(name);
            if (values == null) return new ArrayList<>();
            
            List<Param> valuesList = new ArrayList<>(values.length + 1);
            Arrays.asList(values).stream().map(val -> new Param(val)).forEach(param -> valuesList.add(param));
            String routeParameter = context.getRouteParam(name);
            if(routeParameter != null){
                valuesList.add(new Param(routeParameter));
            }
            return valuesList;
        }
    }*/
    
    /**
     * Convenience method to get parameters in case <code>multipart/form-data</code> request was used.
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return multiple request values for a name.
     */
    protected List<String> params(String name, List<FormItem> formItems){
        List<String> vals = new ArrayList<String>();
        for (FormItem formItem : formItems) {
            if(!formItem.isFile() && name.equals(formItem.getFieldName())){
                vals.add(formItem.getStreamAsString());
            }
        }
        return vals;
    }

    /**
     * Returns an instance of {@link MultiList} containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     *
     * @return an instance {@link MultiList} containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    protected MultiList<String> params(){
        return context.params();
    }
    

    /**
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of request parameter.
     * @see ContextImpl#param(String)
     */
    protected Param param(String name) {
        return new Param(context.param(name));//RequestUtils.param(name));
    }
    
    /**
     * Creates a URL-ready String of the current parameters.
     * This includes the starting '?' and '&amp;' as delimiter.
     * Example:
     *  Parameters: key1=[param1, param12], key2=[param2]
     * This translates into:
     *  ?key1=param1&amp;key1=param12&amp;key2=param2
     * 
     * @return URL String of the parameters
     * @author MTD
     */
    protected String paramsAsUrlString() {
        StringBuilder bob = new StringBuilder();
        bob.append('?');
        MultiList<String> params = params();
        for (String key : params.keySet()) {
            for (String value : params.list(key)) {
                bob.append(key);
                bob.append('=');
                bob.append(value);
                bob.append('&');
            }
        }
        bob.deleteCharAt(bob.length()-1); //removes the last '&'
        return bob.toString();
    }
    

    /**
     * Convenience method to get parameters in case <code>multipart/form-data</code> request was used.
     *
     * Returns a {@link MultiList} where keys are names of all parameters.
     *
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return a {@link MultiList} where keys are names of all parameters.
     */
    protected MultiList<String> params(List<FormItem> formItems) {
        MultiList<String> params = new MultiList<>();
        for (FormItem formItem : formItems) {
            if(!formItem.isFile() && !params.contains(formItem.getFieldName())){
                params.put(formItem.getFieldName(), formItem.getStreamAsString());
            }
        }
        return params;
    }
    
    /**
     * Tests if a request parameter exists. Disregards the value completely - this
     * can be empty string, but as long as parameter does exist, this method returns true.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    protected boolean exists(String name){
        return param(name) != null;
    }
    
    /**
     * Synonym of {@link #exists(String)}.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    protected boolean requestHas(String name){
        return param(name) != null;
    }


    /**
     * Returns local host name on which request was received.
     * 
     * @return local host name on which request was received.
     */
    protected String host() {
        return context.host();
    }
    protected String scheme() {
        return context.scheme();
    }
    protected String serverName() {
        return context.serverName();
    }


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    protected String ipAddress() {
        return context.remoteIP();
    }

    /**
     * This method returns a protocol of a request to web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Proto</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #protocol()} method is used.
     *
     * @return protocol of web server request if <code>X-Forwarded-Proto</code> header is found, otherwise current
     * protocol.
     */
    protected String getRequestProtocol(){
        String protocol = header("X-Forwarded-Proto");
        return StringUtil.blank(protocol)? protocol(): protocol;
    }


    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    protected int port(){
        return context.port();
    }


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    protected String protocol(){
        return context.protocol();
    }

    /**
     * This method returns a host name of a web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Host</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #host()} method is used.
     *
     * @return host name of web server if <code>X-Forwarded-Host</code> header is found, otherwise local host name.
     */
    protected String getRequestHost() {
        String forwarded = header("X-Forwarded-Host");
        if (StringUtil.blank(forwarded)) {
            return host();
        }
        String[] forwards = forwarded.split(",");
        return forwards[0].trim();
    }
    
    /**
     * This method returns a port of a web server if this Java container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Port</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #port()} method is used.
     *
     * @return port of web server request if <code>X-Forwarded-Port</code> header is found, otherwise port of the Java container.
     */
    protected int getRequestPort(){
        String port = header("X-Forwarded-Port");
        return StringUtil.blank(port)? port(): Integer.parseInt(port);
    }

    /**
     * Returns IP address that the web server forwarded request for.
     *
     * @return IP address that the web server forwarded request for.
     */
     protected String ipForwardedFor() {
        String h = header("X-Forwarded-For");
        return !StringUtil.blank(h) ? h : remoteIP();
    }

    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    protected Param getId(){
        return new Param(context.param("id"));//new Param(getIdString());
    }
    /*private String getIdString() {
        String paramId = context.getParameter("id");
        if(paramId != null && context.getAttribute("id") != null){
            logger.warn("WARNING: probably you have 'id' supplied both as a HTTP parameter, as well as in the URI. Choosing parameter over URI value.");
        }

        String theId;
        if(paramId != null){
            theId =  paramId;
        }else{
            Object id = context.getAttribute("id");
            theId =  id != null ? id.toString() : null;
        }
        return StringUtil.blank(theId) ? null : theId;
    }*/
    

//    /**
//     * Returns a collection of uploaded files from a multi-part port request.
//     * Uses request encoding if one provided, and sets no limit on the size of upload.
//     *
//     * @return a collection of uploaded files from a multi-part port request.
//     */
//    protected Iterator<FormItem> uploadedFiles() {
//        return uploadedFiles(null, -1);
//    }

//    /**
//     * Returns a collection of uploaded files from a multi-part port request.
//     * Sets no limit on the size of upload.
//     *
//     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
//     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
//     * the platform default encoding is used.
//     *
//     * @return a collection of uploaded files from a multi-part port request.
//     */
//    protected Iterator<FormItem> uploadedFiles(String encoding) {
//        return uploadedFiles(encoding, -1);
//    }


//    /**
//     * Returns a collection of uploaded files from a multi-part port request.
//     *
//     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
//     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
//     * the platform default encoding is used.
//     * @param maxFileSize maximum file size in the upload in bytes. -1 indicates no limit.
//     *
//     * @return a collection of uploaded files from a multi-part port request.
//     */
//    protected Iterator<FormItem> uploadedFiles(String encoding, long maxFileSize) {
//        HttpServletRequest req = Context.getHttpRequest();
//
//        Iterator<FormItem> iterator;
//
////        if(req instanceof AWMockMultipartHttpServletRequest){//running inside a test, and simulating upload.
////          //MTD: iterator = ((AWMockMultipartHttpServletRequest)req).getFormItemIterator();
////        }else{
//            if (!ServletFileUpload.isMultipartContent(req))
//                throw new ControllerException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");
//
//            ServletFileUpload upload = new ServletFileUpload();
//            if(encoding != null)
//                upload.setHeaderEncoding(encoding);
//            upload.setFileSizeMax(maxFileSize);
//            try {
//                FileItemIterator it = upload.getItemIterator(Context.getHttpRequest());
//                iterator = new FormItemIterator(it);
//            } catch (Exception e) {
//                throw new ControllerException(e);
//            }
////        }
//        return iterator;
//    }


    /**
    * Convenience method to get file content from <code>multipart/form-data</code> request.
    *
    * @param fieldName name of form field from the  <code>multipart/form-data</code> request corresponding to the uploaded file.
    * @param formItems form items retrieved from <code>multipart/form-data</code> request.
    * @return <code>InputStream</code> from which to read content of uploaded file.
     * @throws IOException, WebException  
    * @throws WebException in case field name is not found in the request.
    */
    protected InputStream getFileInputStream(String fieldName, List<FormItem> formItems) throws IOException, WebException {
        for (FormItem formItem : formItems) {
            if(formItem.isFile() && formItem.getFieldName().equals(fieldName)){
                return formItem.openStream();
            }
        }
        throw new WebException("File with field named: '" + fieldName + "' not found");
    }


    /**
     * Convenience method, calls {@link #multipartFormItems(String)}. Does not set encoding before reading request.
     *
     * MTD: changed to Map&lt;String, List&lt;FormItem&gt;&gt; instead of Map&lt;String, FormItem&gt;
     * 
     * @see #multipartFormItems(String)
     * @return a collection of uploaded files/fields from a multi-part request.
     */
    protected MultiList<FormItem> /*Map<String, List<FormItem>>*/ multipartFormItems() {
        return multipartFormItems(null);
    }


    /**
     * Returns a collection of uploaded files and form fields from a multi-part request.
     * This method uses <a href="http://commons.apache.org/proper/commons-fileupload/apidocs/org/apache/commons/fileupload/disk/DiskFileItemFactory.html">DiskFileItemFactory</a>.
     * As a result, it is recommended to add the following to your web.xml file:
     *
     * <pre>
     *   &lt;listener&gt;
     *      &lt;listener-class&gt;
     *         org.apache.commons.fileupload.servlet.FileCleanerCleanup
     *      &lt;/listener-class&gt;
     *   &lt;/listener&gt;
     *</pre>
     *
     * For more information, see: <a href="http://commons.apache.org/proper/commons-fileupload/using.html">Using FileUpload</a>
     *
     * The size of upload defaults to max of 20mb. Files greater than that will be rejected. If you want to accept files
     * smaller of larger, create a file called <code>activeweb.properties</code>, add it to your classpath and
     * place this property to the file:
     *
     * <pre>
     * #max upload size
     * maxUploadSize = 20000000
     * </pre>
     *
     * MTD: changed to Map&lt;String, List&lt;FormItem&gt;&gt; instead of Map&lt;String, FormItem&gt;
     *
     * @param encoding specifies the character encoding to be used when reading the headers of individual part.
     * When not specified, or null, the request encoding is used. If that is also not specified, or null,
     * the platform default encoding is used.
     *
     * @return a collection of uploaded files from a multi-part request.
     */
    protected MultiList<FormItem> multipartFormItems(String encoding) {

        if (!context.isRequestMultiPart())
            throw new MediaTypeException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");

        MultiList<FormItem> parts = new MultiList<>();
        try {
            context.parseRequestMultiPartItems(encoding)
                .ifPresent(items -> {
                    for (FormItem item : items) {
                        parts.put(item.getFieldName(), item);
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            throw new ControllerException(e);
        }
        return parts;
    }
    
    
    
    /**
     * @author MTD
     * @param item
     * @return the path to the saved image
     */
    protected ImageHandlerBuilder image(FormItem item) throws ControllerException {
        return new ImageHandlerBuilder(this, injector.getInstance(DeploymentInfo.class), item);
    }
    protected ImageHandlerBuilder image(File file) throws PathNotFoundException, ControllerException {
        if (!file.canRead())
            throw new PathNotFoundException(file.getPath());
        return new ImageHandlerBuilder(this, injector.getInstance(DeploymentInfo.class), file);
    }
    protected ImageHandlerBuilder image(String name) throws PathNotFoundException, ControllerException {
        File file = new File(getRealPath(name));
        if (!file.canRead())
            throw new PathNotFoundException(file.getPath());
        return new ImageHandlerBuilder(this, injector.getInstance(DeploymentInfo.class), file);
    }
    protected ImageHandlerBuilder image(byte[] bytes, String fileName) throws ControllerException {
        return new ImageHandlerBuilder(this, injector.getInstance(DeploymentInfo.class), bytes, fileName);
    }
    
    
    protected Modes mode() {
        return context.mode();
    }
    
    
    /**
     * Returns a map parsed from a request if parameter names have a "hash" syntax:
     *
     *  <pre>
     *  &lt;input type=&quot;text&quot; name=&quot;account[name]&quot; /&gt;
        &lt;input type=&quot;text&quot; name=&quot;account[number]&quot; /&gt;
     * </pre>
     *
     * will result in a map where keys are names of hash elements, and values are values of these elements from request.
     * For the example above, the map will have these values:
     *
     * <pre>
     *     { "name":"John", "number": "123" }
     * </pre>
     *
     * @param hashName - name of a hash. In the example above, it will be "account".
     * @return map with name/value pairs parsed from request.
     */
    public Map<String, String> getMap(String hashName) {
        MultiList<String>  params = params();
        Map<String, String>  hash = new HashMap<String, String>();
        for(String key:params.keySet()){
            if(key.startsWith(hashName)){
                String name = parseHashName(key);
                if(name != null){
                    hash.put(name, param(key).param);
                }
            }
        }
        return hash;
    }

    /**
     * Convenience method to get parameter map in case <code>multipart/form-data</code> request was used.
     *
     * Returns a map parsed from a request if parameter names have a "hash" syntax:
     *
     *  <pre>
     *  &lt;input type=&quot;text&quot; name=&quot;account[name]&quot; /&gt;
     *  &lt;input type=&quot;text&quot; name=&quot;account[number]&quot; /&gt;
     * </pre>
     *
     * will result in a map where keys are names of hash elements, and values are values of these elements from request.
     * For the example above, the map will have these values:
     *
     * <pre>
     *     { "name":"John", "number": "123" }
     * </pre>
     *
     * @param hashName - name of a hash. In the example above, it will be "account".
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return map with name/value pairs parsed from request.
     */
    public Map<String, String> getMap(String hashName, List<FormItem> formItems) {

        Map<String, String>  hash = new HashMap<String, String>();
        for(FormItem item:formItems){
            if(item.getFieldName().startsWith(hashName)){
                String name = parseHashName(item.getFieldName());
                if(name != null){
                    hash.put(name, item.getStreamAsString());
                }
            }
        }
        return hash;
    }
    
    private static Pattern hashPattern = Pattern.compile("\\[.*\\]");

    /**
     * Parses name from hash syntax.
     *
     * @param param something like this: <code>person[account]</code>
     * @return name of hash key:<code>account</code>
     */
    private static String parseHashName(String param) {
        Matcher matcher = hashPattern.matcher(param);
        String name = null;
        while (matcher.find()){
            name = matcher.group(0);
        }
        return name == null? null : name.substring(1, name.length() - 1);
    }
    

    /**
     * Sets character encoding for request. Has to be called before reading any parameters of getting input
     * stream.
     * @param encoding encoding to be set.
     *
     * @throws UnsupportedEncodingException
     */
    protected void setRequestEncoding(String encoding) throws UnsupportedEncodingException {
        context.setRequestCharacterEncoding(encoding);//getHttpRequest().setCharacterEncoding(encoding);
    }

    protected void setResponseEncoding(String encoding) {
        context.setEncoding(encoding);
    }
    protected void encoding(String encoding) {
        setResponseEncoding(encoding);
    }

    /**
     * Returns reference to a current session. Creates a new session of one does not exist.
     * @return reference to a current session.
     */
    protected Session session(){
        return context.getSession(/*true*/);
    }
    /**
     * Convenience method, sets an object on a session. Equivalent of:
     * <pre>
     * <code>
     *     session().put(name, value)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @param value object itself.
     */
    protected void session(String name, Serializable value){
        session().put(name, value);
    }

    /**
     * Session object of that name
     *
     * @param name name of session attribute
     * @return value of session attribute of null if not found
     */
    protected Object session(String name){
        Object val = session().get(name);
        return val == null ? null : val;
    }


    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     String val = (String)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected String sessionString(String name){
        return ConvertUtil.toString(session(name));
    }



    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Integer val = (Integer)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Integer sessionInteger(String name){
        return ConvertUtil.toInteger(session(name));
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Boolean val = (Boolean)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Boolean sessionBoolean(String name){
        return ConvertUtil.toBoolean(session(name));
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Double val = (Double)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Double sessionDouble(String name){
        return ConvertUtil.toDouble(session(name));
    }

    /**
     * Convenience method, returns object from session, equivalent of:
     * <pre>
     * <code>
     *     Long val = (Long)session().get(name)
     * </code>
     * </pre>
     *
     * @param name name of object
     * @return value
     */
    protected Long sessionLong(String name){
        return ConvertUtil.toLong(session(name));
    }

    /**
     * Returns true if session has named object, false if not.
     *
     * @param name name of object.
     * @return true if session has named object, false if not.
     */
    protected boolean sessionHas(String name){
        return session().get(name) != null;
    }

    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
    public Map<String, Cookie> cookies(){
        return context.getCookies();
    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
    public Cookie cookie(String name){
        return context.getCookie(name);
    }
    
    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    protected String cookieValue(String name){
        Cookie cookie = cookie(name);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param cookie cookie to send.
     */
    public void sendCookie(Cookie cookie){
        context.addCookie(cookie);
    }

    /**
     * Sends cookie to browse with response.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    public void sendCookie(String name, String value) {
        context.addCookie(Cookie.builder(name, value).build());
    }


    /**
     * Sends long to live cookie to browse with response. This cookie will be asked to live for 20 years.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    public void sendPermanentCookie(String name, String value) {
        context.addCookie(Cookie.builder(name, value).setMaxAge(Cookie.ONE_YEAR * 20).build());
    }
    
    public void sendExpireCookie(String name) {
        context.addCookie(Cookie.builder(context.getCookie(name)).setExpires(Cookie.EPOCH).build());
    }

    /**
     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
     * Example: <code>/controller/action/id</code>
     *
     * @return a path of the request.
     */
    protected String path(){
        return context.path();

    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    protected  String queryString(){
        return context.queryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    protected String method(){
        return context.method();
    }

//    /**
//     * True if this request uses HTTP GET method, false otherwise.
//     *
//     * @return True if this request uses HTTP GET method, false otherwise.
//     */
//    protected boolean isGet() {
//        return "GET".equalsIgnoreCase(method());
//    }
//
//
//    /**
//     * True if this request uses HTTP POST method, false otherwise.
//     *
//     * @return True if this request uses HTTP POST method, false otherwise.
//     */
//    protected boolean isPost() {
//        return "POST".equalsIgnoreCase(method());
//    }
//
//
//    /**
//     * True if this request uses HTTP PUT method, false otherwise.
//     *
//     * @return True if this request uses HTTP PUT method, false otherwise.
//     */
//    protected boolean isPut() {
//        return "PUT".equalsIgnoreCase(method());//return RequestUtils.isPut();
//    }
//
//
//    /**
//     * True if this request uses HTTP DELETE method, false otherwise.
//     *
//     * @return True if this request uses HTTP DELETE method, false otherwise.
//     */
//    protected boolean isDelete() {
//        return "DELETE".equalsIgnoreCase(method());///return RequestUtils.isDelete();
//    }
//
//
//    /*private boolean isMethod(String method){
//        return RequestUtils.isMethod(method);
//    }*/
//
//
//    /**
//     * True if this request uses HTTP HEAD method, false otherwise.
//     *
//     * @return True if this request uses HTTP HEAD method, false otherwise.
//     */
//    protected boolean isHead() {
//        return "HEAD".equalsIgnoreCase(method());//RequestUtils.isHead();
//    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    protected String context(){
        return /*injector.getInstance(DeploymentInfo.class).getContextPath();*/context.contextPath();//RequestUtils.context();
    }


    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    protected String remoteHost(){
        return context.remoteHost();
    }

    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    protected String remoteIP(){
        return context.remoteIP();
    }



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    protected String header(String name){
        return context.requestHeader(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    protected MultiList<String> headers(){
        return context.requestHeaders();
    }

    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, String value){
        context.setHeader(name, value);
    }

    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, Object value){
        if(value == null) throw new NullPointerException("value cannot be null");

        header(name, value.toString());
    }
    
    protected Object contextAttribute(String name) {
        return context.getAttribute(name);
    }

    /**
     * Streams content of the <code>reader</code> to the HTTP client.
     *
     * @param in input stream to read bytes from.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected /*HttpBuilder*/void streamOut(InputStream in) {
//        StreamResponse resp = new StreamResponse(context, in);
//        context.setControllerResponse(resp);
//        return new HttpBuilder(resp);
        //TODO finish and TEST the god damn method
        //------------------
        Result r = ResultBuilder.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .renderable(in);
//        context.setControllerResponse(r);
        result = r;
    }


    /**
     * Returns a String containing the real path for a given virtual path. For example, the path "/index.html" returns
     * the absolute file path on the server's filesystem would be served by a request for
     * "http://host/contextPath/index.html", where contextPath is the context path of this ServletContext.
     * 
     * <p>The real path returned will be in a form appropriate to the computer and operating system on which the servlet
     * container is running, including the proper path separators. This method returns null if the servlet container
     * cannot translate the virtual path to a real path for any reason (such as when the content is being made
     * available from a .war archive).</p>
     *
     * <p>
     * JavaDoc copied from: <a href="http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29">
     * http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29</a>
     * </p>
     *
     * @param path a String specifying a virtual path
     * @return a String specifying the real path, or null if the translation cannot be performed
     */
    protected String getRealPath(String path) {
        return injector.getInstance(DeploymentInfo.class).getRealPath(path);//context.getRealPath(path);
    }

    /**
     * Use to send raw data to HTTP client. Content type and headers will not be set.
     * Response code will be set to 200.
     *
     * @return instance of output stream to send raw data directly to HTTP client.
     */
    protected OutputStream outputStream(){
        return outputStream(null, null, 200);
    }

    /**
     * Use to send raw data to HTTP client. Status will be set to 200.
     *
     * @param contentType content type
     * @return instance of output stream to send raw data directly to HTTP client.
     */
    protected OutputStream outputStream(String contentType) {
        return outputStream(contentType, null, 200);
    }


    /**
     * Use to send raw data to HTTP client.
     *
     * @param contentType content type
     * @param headers set of headers.
     * @param status status.
     * @return instance of output stream to send raw data directly to HTTP client.
     */
    public OutputStream outputStream(String contentType, Map<String, String> headers, int status) {
        Result result = ResultBuilder.noBody(status).contentType(contentType);
        headers.entrySet().forEach(header -> result.addHeader(header.getKey(), header.getValue()));
        setControllerResult(result);
        
        try {
            //return context.responseOutputStream(); //TODO not possible, is it?
            return context.responseOutputStream(result);
        } catch(Exception e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Produces a writer for sending raw data to HTTP clients.
     *
     * Content type content type not be set on the response. Headers will not be send to client. Status will be
     * set to 200.
     * @return instance of a writer for writing content to HTTP client.
     */
    protected Writer writer(){
        return writer(null, null, 200);
    }

    /**
     * Produces a writer for sending raw data to HTTP clients.
     *
     * @param contentType content type. If null - will not be set on the response
     * @param headers headers. If null - will not be set on the response
     * @param status will be sent to browser.
     * @return instance of a writer for writing content to HTTP client.
     */
    public Writer writer(String contentType, Map<String, String> headers, int status){
        Result result = ResultBuilder.noBody(status).contentType(contentType);
        headers.entrySet().forEach(header -> result.addHeader(header.getKey(), header.getValue()));
        setControllerResult(result);
        //TODO TEST
        try {
            return context.responseWriter(result);
            //return context.responseWriter(); //TODO not possible, is it?
        } catch(Exception e) {
            throw new ControllerException(e);
        }
    }


    /**
     * Returns true if this request is Ajax.
     *
     * @return true if this request is Ajax.
     */
    protected boolean isXhr(){
        return header("X-Requested-With") != null || header("x-requested-with") != null;
    }



    /**AppContext context,
     * Helper method, returns user-agent header of the request.
     *
     * @return user-agent header of the request.
     */
    protected String userAgent(){
        String camel = header("User-Agent");
        return camel != null ? camel : header("user-agent");
    }

//    /**
//     * Returns instance of {@link AppContext}.
//     *
//     * @return instance of {@link AppContext}.
//     */
//    protected AppContext appContext(){
//        return context.createAppContext();//context.getAppContext();
//    }

//    /**
//     * Returns a format part of the URI, or null if URI does not have a format part.
//     * A format part is defined as part of URI that is trailing after a last dot, as in:
//     *
//     * <code>/books.xml</code>, here "xml" is a format.
//     *
//     * @return format part of the URI, or nul if URI does not have it.
//     */
//    protected String format(){
//        return context.getRouteFormat();
//    }


    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    protected Route getRoute(){
        return context.getRoute();
    }
    
    /**
     * Will merge a template and return resulting string. This method is used for just merging some text with dynamic values.
     * Once you have the result, you can send it by email, external web service, save it to a database, etc.
     *
     * @param template name of template - same as in regular templates. Example: <code>"/email-templates/welcome"</code>.
     * @param values values to be merged into template.
     * @return merged string
     */
    //TODO TEST this mofo
    protected String merge(String template, Map<String, Object> values){
        try (ResponseStream stream = new ResponseStreamWriter()) {
        
            TemplateEngineOrchestrator manager = injector.getInstance(TemplateEngineOrchestrator.class);
            TemplateEngine engine = manager.getTemplateEngineForContentType(MediaType.TEXT_HTML);
            
            engine.invoke(
                    context, 
                    ResultBuilder.ok().addAllViewObjects(values).template(template).layout(null), 
                    stream);
            
            
    //        Configuration.getTemplateManager().merge(values, template, stringWriter);
            return stream.getWriter().toString();
        } catch (IOException notPossible) { }
        return "";
    }
    
    /*
     * MTD section
     */
//    protected String language() {
//        return context.getRouteLanguage();
//    }
    
    protected String contentType() {
        return context.requestContentType();
    }
    
    /**
     * Converts the request input into an object of the specified class in case of <code>application/json</code> request.
     *  
     * @param clazz A representation of the expected JSON
     * @return The object of the converted JSON, or <code>throws</code> if the JSON could not be correctly deserialized,
     *         or the media type was incorrect. 
     * @throws ParsableException If the parsing from JSON to class failed
     * @throws MediaTypeException If the mediatype of the request was not "application/json"
     * @author MTD
     */
    //TODO not correctly formulated doc
    protected <T> T parseBody(Class<T> clazz) throws ParsableException, MediaTypeException {
        String contentType = context.requestContentType();
        
        // if the content type header was not provided, we throw
        if (contentType == null || contentType.isEmpty()) {
            throw new MediaTypeException("Missing media type header");
        }
        
        // extract the actual content type in case charset is also a part of the string
        contentType = HttpHeaderUtil.getContentTypeFromContentTypeAndCharacterSetting(contentType);
        
        ParserEngine engine = injector.getInstance(ParserEngineManager.class).getParserEngineForContentType(contentType);
        
        if (engine == null) {
            throw new MediaTypeException("An engine for media type ("+contentType+") was not found");
        }
        
        try {
            return engine.invoke(context.requestInputStream(), clazz);
        } catch (IOException e) {
            throw new ParsableException(e);
        }
    }
}
