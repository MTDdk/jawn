/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.exceptions.ActionNotFoundException;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.util.StringUtil;


/**
 * Subclass this class to create application controllers. A controller is a main component of a web
 * application. Its main purpose in life is to process web requests. 
 *
 * @author Igor Polevoy
 */
public abstract class AppController extends HttpSupport {
    
    public void index() {}


//    /**
//     * Assigns value that will be passed into view.
//     * 
//     * @param name name of a value.
//     * @param value value.
//     */
//    protected void assign(String name, Object value) {
//        KeyWords.check(name);
//        Context.getValues().put(name, value);
//    }

    /**
     * Alias to {@link #assign(String, Object)}.
     *
     * @param name name of object to be passed to view
     * @param value object to be passed to view
     */
    protected void view(String name, Object value) {
        assign(name, value);
    }
    

//    protected Map<String, Object> values() {
//        return Context.getValues();
//    }
    
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

        String targetTemplate = template.startsWith("/")? template: getControllerPath(getClass())
                + "/" + template;

        return super.render(targetTemplate/*, values()*/);
    }

    /**
     * Use this method in order to override a layout, status code, and content type.
     * @return 
     *
     * @return instance of {@link RenderBuilder}, which is used to provide additional parameters.
     */
    protected NewRenderBuilder render(){

        String template = getControllerPath(getClass()) + "/" + getRoute().getActionName();
        return super.render(template/*, values()*/);
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

    /*protected InputStream getRequestInputStream() throws IOException {
        return Context.getHttpRequest().getInputStream();
    }*/

    /**
     * Alias to {@link #getRequestInputStream()}.
     * @return input stream to read data sent by client.
     * @throws IOException
     */
    /*protected InputStream getRequestStream() throws IOException {
        return Context.getHttpRequest().getInputStream();
    }*/

    /**
     * Reads entire request data as String. Do not use for large data sets to avoid
     * memory issues, instead use {@link #getRequestInputStream()}.
     *
     * @return data sent by client as string.
     * @throws IOException
     */
    /*protected String getRequestString() throws IOException {
        return Util.read(Context.getHttpRequest().getInputStream());
    }*/

    /**
     * Reads entire request data as byte array. Do not use for large data sets to avoid
     * memory issues.
     *
     * @return data sent by client as string.
     * @throws IOException
     */
    /*protected byte[] getRequestBytes() throws IOException {        
        return Util.bytes(Context.getHttpRequest().getInputStream());
    }*/


//    /**
//     * Returns a name for a default layout as provided in  <code>activeweb_defaults.properties</code> file.
//     * Override this  method in a sub-class. Value expected is a fully qualified name of a layout template.
//     * Example: <code>"/custom/custom_layout"</code>
//     *
//     * @return name of a layout for this controller and descendants if they do not override this method.
//     */
//    protected String getLayout(){
//        return Configuration.getDefaultLayout();
//    }

    /**
     * Returns hardcoded value "text/html". Override this method to set default content type to a different value across
     * all actions in controller and its subclasses. This is a convenient method for building REST webservices. You can set
     * this value once to "text/json", "text/xml" or whatever else you need.
     *
     * @return hardcoded value "text/html"
     */
    protected String getContentType(){
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
//        if (restful()) {
//            return restfulActionSupportsHttpMethod(actionMethodName, httpMethod) || standardActionSupportsHttpMethod(actionMethodName, httpMethod);
//        } else {
            return standardActionSupportsHttpMethod(actionMethodName, httpMethod);
//        }
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
        //README: it might not actually be necessary to do this lowercasing
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
    
    /**
     * Generates a path to a controller based on its package and class name. The path always starts with a slash: "/".
     * Examples:
     * <p></p>
     * <ul>
     * <li>For class: <code>app.controllers.Simple</code> the path will be: <code>/simple</code>.</li>
     * <li>For class: <code>app.controllers.admin.PeopleAdmin</code> the path will be: <code>/admin/people_admin</code>.</li>
     * <li>For class: <code>app.controllers.admin.simple.PeopleAdmin</code> the path will be: <code>/admin/simple/people_admin</code>.</li>
     * </ul>
     * <p></p>
     * Class name looses the "Controller" suffix and gets converted to underscore format, while packages stay unchanged.
     *
     * @param controllerClass class of a controller.
     * @param <T> class extending {@link AppController}
     * @return standard path for a controller.
     */
    static <T extends AppController> String getControllerPath(Class<T> controllerClass) {
        String simpleName = controllerClass.getSimpleName();
        if (!simpleName.endsWith("Controller")) {
            throw new ControllerException("controller name must end with 'Controller' suffix");
        }

        String className = controllerClass.getName();
        if (!className.startsWith("app.controllers")) {
            throw new ControllerException("controller must be in the 'app.controllers' package");
        }
        String packageSuffix = className.substring("app.controllers".length(), className.lastIndexOf("."));
        packageSuffix = packageSuffix.replace(".", "/");
        if (packageSuffix.startsWith("/"))
            packageSuffix = packageSuffix.substring(1);

        return (packageSuffix.equals("") ? "" : "/" + packageSuffix) + "/" + StringUtil.underscore(simpleName.substring(0, simpleName.lastIndexOf("Controller")));
    }

//    private boolean restfulActionSupportsHttpMethod(String action, HttpMethod httpMethod) {
//        if (action.equals("index") && httpMethod.equals(HttpMethod.GET)) {
//            return true;
//        } else if (action.equals("newForm") && httpMethod.equals(HttpMethod.GET)) {
//            return true;
//        } else if (action.equals("create") && httpMethod.equals(HttpMethod.POST)) {
//            return true;
//        } else if (action.equals("show") && httpMethod.equals(HttpMethod.GET)) {
//            return true;
//        } else if (action.equals("editForm") && httpMethod.equals(HttpMethod.GET)) {
//            return true;
//        } else if (action.equals("update") && httpMethod.equals(HttpMethod.PUT)) {
//            return true;
//        } else if (action.equals("destroy") && httpMethod.equals(HttpMethod.DELETE)) {
//            return true;
//        } else {
//            log().debug("You might want to execute a non-restful action on a restful controller. It is recommended that you " +
//                    "use the following methods on restful controllers: index, newForm, create, show, editForm, update, destroy");
//            return false;
//        }
//    }



    /**
     * Returns true if this controller is configured to be {@link net.javapla.jawn.annotations.RESTful}.
     * @return true if this controller is restful, false if not.
     */
//    public boolean restful() {
//        return getClass().getAnnotation(RESTful.class) != null;
//    }

//    public static <T extends AppController> boolean restful(Class<T> controllerClass){
//        return controllerClass.getAnnotation(RESTful.class) != null;
//    }
}
