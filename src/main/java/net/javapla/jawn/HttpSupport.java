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


import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.MediaTypeException;
import net.javapla.jawn.exceptions.PathNotFoundException;
import net.javapla.jawn.exceptions.WebException;
import net.javapla.jawn.templates.TemplateEngine;
import net.javapla.jawn.templates.TemplateEngineManager;
import net.javapla.jawn.util.CollectionUtil;
import net.javapla.jawn.util.ConvertUtil;
import net.javapla.jawn.util.MultiList;
import net.javapla.jawn.util.StringUtil;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author Igor Polevoy
 * @author MTD
 */
class HttpSupport {
    
    //TODO inject Context and replace all context
//    @Inject
    private Context context;
    private Injector injector;
    
    public void init(Context context, Injector injector) {
        System.err.println("CONTEXT CONTEXT CONTEXT CONTEXT CONTEXT CONTEXT CONTEXT " + this.getClass());
        this.context = context;
        this.injector = injector;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    protected Logger log() {
        return logger;
    }


    /**
     * Assigns value that will be passed into view.
     *
     * @param name name of value
     * @param value value.
     */
    protected void assign(String name, Object value) {
        KeyWords.check(name);
//        context.addViewObject(name, value);//getValues().put(name, value);
        context.getNewControllerResponse().addViewObject(name, value);
    }
    
    protected Map<String, Object> values() {
//        return context.getViewObjects();//getValues();
        return context.getNewControllerResponse().getViewObjects();
    }

    /**
     * Alias to {@link #assign(String, Object)}.
     *
     * @param name name of object to be passed to view
     * @param value object to be passed to view
     */
    protected void view(String name, Object value) {
        assign(name, value);
    }


    /**
     * Convenience method, calls {@link #assign(String, Object)} internally.
     * The keys in the map are converted to String values.
     *
     * @param values map with values to pass to view.
     */
    protected void view(Map<String, Object> values){
        for(String key:values.keySet() ){
            assign(key, values.get(key));
        }
    }

    /**
     * Convenience method, calls {@link #assign(String, Object)} internally.
     *
     * @param values An even list of key/value pairs
     */
    protected void view(Object ... values){
        view(CollectionUtil.map(values));
    }

    /**
     * Convenience method, takes in a map of values to flash.
     *
     * @see #flash(String, Object)
     *
     * @param values values to flash.
     */
    protected void flash(Map<String, Object> values){
        for(Object key:values.keySet() ){
            flash(key.toString(), values.get(key));
        }
    }

    /**
     * Convenience method, takes in a vararg of values to flash.
     * Number of values must be even.
     *
     * @see #flash(String, Object)
     * @param values values to flash.
     */
    protected void flash(Object ... values){
        flash(CollectionUtil.map(values));
    }

    /**
     * Sets a flash name for a flash with  a body.
     * Here is a how to use a tag with a body:
     *
     * <pre>
     * &lt;@flash name=&quot;warning&quot;&gt;
     * &lt;div class=&quot;warning&quot;&gt;${message}&lt;/div&gt;
     *  &lt;/@flash&gt;
     * </pre>
     *
     * If body refers to variables (as in this example), then such variables need to be passed in to the template as usual using
     * the {@link #view(String, Object)} method.
     *
     * @param name name of a flash
     */
    protected void flash(String name){
        flash(name, null);
    }
    
    /**
     * Sends value to flash. Flash survives one more request.  Using flash is typical
     * for POST/GET pattern,
     *
     * @param name name of value to flash
     * @param value value to live for one more request in current session.
     */
    @SuppressWarnings("unchecked")
    protected void flash(String name, Object value) {
        if (session().get("flasher") == null) {
            session().put("flasher", new HashMap<String, Object>());
        }
        ((Map<String, Object>) session().get("flasher")).put(name, value);
    }
    //TODO make sure, flash actually works
    
//    @Deprecated
//    protected class ResponseBuilder {
        
        /**
         * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
         * and to support AJAX.
         *
         * @param text text of response.
         * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
         */
//        public HttpBuilder text(String text) {
//            DirectResponse resp = new DirectResponse(context, text);
////            context.setControllerResponse(resp);
//            
//            //----------
//            NewControllerResponse r = new NewControllerResponse(200);
//            r.renderable(text);
//            r.contentType(MediaType.TEXT_PLAIN);
//            context.setNewControllerResponse(r);
//            return new HttpBuilder(resp);
//        }
        
        /**
         * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
         * and to support AJAX.
         * 
         * @param text A string containing &quot;{index}&quot;, like so: &quot;Message: {0}, error: {1}&quot;
         * @param objects A varargs of objects to be put into the <code>text</code>
         * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
         * @see MessageFormat#format
         */
//        public HttpBuilder text(String text, Object...objects) {
//            return text(MessageFormat.format(text, objects));
//        }
        
        /**
         * This method will send a JSON response to the client.
         * It will not use any layouts.
         * Use it to build app.services and to support AJAX.
         * 
         * @author MTD
         * 
         * @param obj
         * @return {@link HttpSupport.HttpBuilder}, to accept additional information. The builder is automatically
         * has its content type set to "application/json"
         */
//        public HttpBuilder json(Object obj) {
//            JsonResponse resp = new JsonResponse(context, obj);
////            context.setControllerResponse(resp);
//            return new HttpBuilder(resp);
//        }
        
        
        /**
         * This method will send a XML response to the client.
         * It will not use any layouts.
         * Use it to build app.services.
         * 
         * @author MTD
         * 
         * @param obj
         * @return {@link HttpSupport.HttpBuilder}, to accept additional information. The builder is automatically
         * has its content type set to "application/xml"
         */
//        public HttpBuilder xml(Object obj) {
//            XmlResponse resp = new XmlResponse(context, obj);
////            context.setControllerResponse(resp);
//            return new HttpBuilder(resp);
//        }
        
        /**
         * This method will send just a status code 
         * @author MTD
         * @return {@link StatusWrapper}, to accept additional information.
         */
//        public StatusWrapper status() {
//            NopResponse resp = new NopResponse(context);
//            context.setControllerResponse(resp);
//            return new HttpBuilder(resp).status();
//        }
        /**
         * This method will send just a status code 
         * @author MTD
         * @return {@link HttpSupport.HttpBuilder}, to accept additional information
         */
//        public HttpBuilder status(int code) {
//            NopResponse resp = new NopResponse(context, code);
////            context.setControllerResponse(resp);
//            return new HttpBuilder(resp);
//        }
//    }

//    protected ResponseBuilder respond() {
//        return new ResponseBuilder();
//    }
    protected NewControllerResponseBuilder respond() {
        return new NewControllerResponseBuilder(context);
    }

//    @Deprecated
//    protected class RenderBuilder extends HttpBuilder {
//
//
//        private RenderBuilder(RenderTemplateResponse response){
//            super(response);
//        }
//
//        /**
//         * Use this method to override a default layout configured.
//         *
//         * @param layout name of another layout.
//         * @return instance of RenderBuilder
//         */
//        public RenderBuilder layout(String layout){
//            getRenderTemplateResponse().setLayout(layout);
//            return this;
//        }
//
//        protected RenderTemplateResponse getRenderTemplateResponse(){
//            return (RenderTemplateResponse)controllerResponse;//(RenderTemplateResponse)getControllerResponse();
//        }
//
//        /**
//         * Call this method to turn off all layouts. The view will be rendered raw - no layouts.
//         * @return instance of RenderBuilder
//         */
//        public RenderBuilder noLayout(){
//            getRenderTemplateResponse().setLayout(null);
//            return this;
//        }
//
//        /**
//         * Sets a format for the current request. This is a intermediate extension for the template file. For instance,
//         * if the name of template file is document.xml.ftl, then the "xml" part is set with this method, the
//         * "document" is a template name, and "ftl" extension is assumed in case you use FreeMarker template manager.
//         *
//         * @param format template format
//         * @return instance of RenderBuilder
//         */
//        public RenderBuilder format(String format){
////            ControllerResponse response = Context.getControllerResponse();
//            if(controllerResponse instanceof RenderTemplateResponse){
//                ((RenderTemplateResponse)controllerResponse).setFormat(format);
//            }
//            return this;
//        }
//    }
    
    protected class NewRenderBuilder {
        private final NewControllerResponse response;
        NewRenderBuilder(NewControllerResponse response) {
            this.response = response;
        }
        
        public NewRenderBuilder noLayout() {
            response.layout(null);
            return this;
        }
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
//    protected RenderBuilder render(String template, Map<String, Object> values) {
//        RenderTemplateResponse resp = new RenderTemplateResponse(context, values, template/*, context.getFormat()*/);
//        context.setControllerResponse(resp);
//        //README replaced with #view(Map<String, Object>) + #render(String)
////        NewControllerResponse r = new NewControllerResponse(200);
//        context.getNewControllerResponse().template(template);
//        return new RenderBuilder(resp);
//    }
    protected NewRenderBuilder render(String template, Map<String, Object> values) {
        view(values);
        return render(template);
    }
    protected NewRenderBuilder render(String template) {
        context.getNewControllerResponse().template(template);
        return new NewRenderBuilder(context.getNewControllerResponse());
    }


//    @Deprecated
//    protected class HttpBuilder {
//        ControllerResponse controllerResponse;
//        private HttpBuilder(ControllerResponse controllerResponse){
//            this.controllerResponse = controllerResponse;
//        }
//    
//        protected ControllerResponse getControllerResponse() {
//            return controllerResponse;
//        }
//    
//        /**
//         * Sets content type of response.
//         * These can be "text/html". Value "text/html" is set by default.
//         *
//         * @param contentType content type value.
//         * @return this
//         */
////        public HttpBuilder contentType(String contentType) {
////            controllerResponse.setContentType(contentType);
////            return this;
////        }
//        
//        /**
//         * Sets a HTTP header on response.
//         *
//         * @param name name of header.
//         * @param value value of header.
//         * @return this
//         */
////        public HttpBuilder header(String name, String value){
////            context.setResponseHeader(name, value); //TODO this ought to be a part of the "Controller"Response
////            return this;
////        }
//    
//        /**
//         * Overrides HTTP status with a different value.
//         * For values and more information, look here:
//         * <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html'>HTTP Status Codes</a>.
//         *
//         * By default, the status is set to 200, OK.
//         *
//         * @param code HTTP status code.
//         * @return this
//         */
////        public HttpBuilder status(int code){
////            controllerResponse.setStatus(code);
////            return this;
////        }
////        /**
////         * Conveniently wraps status codes into simple method calls 
////         * @return wrapper methods
////         */
////        public StatusWrapper status() {
////            return new StatusWrapper(this);
////        }
//    }

    /**
     * Redirects to a an action of this controller, or an action of a different controller.
     * This method does not expect a full URL.
     *
     * @param path - expected to be a path within the application.
     * @return instance of {@link HttpSupport.HttpBuilder} to accept additional information.
     */
    protected /*HttpBuilder*/void redirect(String path) {
//        RedirectResponse resp = new RedirectResponse(context, path);
//        context.setControllerResponse(resp);
//        return new HttpBuilder(resp);
        //-----
        try {
            context.responseSendRedirect(path);
            context.setNewControllerResponse(null);
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Redirects to another URL (usually another site).
     *
     * @param url absolute URL: <code>http://domain/path...</code>.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected /*HttpBuilder*/void redirect(URL url) {
//        RedirectResponse resp = new RedirectResponse(context, url);
//        context.setControllerResponse(resp);
//        return new HttpBuilder(resp);
        //-----
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
    protected /*HttpBuilder*/void redirectToReferrer(String defaultReference) {
        String referrer = context.requestHeader("Referer");
        referrer = referrer == null? defaultReference: referrer;
//        RedirectResponse resp = new RedirectResponse(context, referrer);
//        context.setControllerResponse(resp);
//        return new HttpBuilder(resp);
        //-----
        redirect(referrer);
    }


    /**
     * Redirects to referrer if one exists. If a referrer does not exist, it will be redirected to
     * the root of the application.
     *
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected /*HttpBuilder*/void redirectToReferrer() {
        String referrer = context.requestHeader("Referer");
        referrer = referrer == null? context.contextPath(): referrer;
//        RedirectResponse resp = new RedirectResponse(context, referrer);
//        context.setControllerResponse(resp);
//        return new HttpBuilder(resp);
        //-----
        redirect(referrer);
    }


    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link AppController}
     * @param action action to redirect to.
     * @param id id to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> /*HttpBuilder*/void redirect(Class<T> controllerClass, String action, Object id){
        /*return */redirect(controllerClass, CollectionUtil.map("action", action, "id", id));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link AppController}
     * @param id id to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> /*HttpBuilder*/void redirect(Class<T> controllerClass, Object id){
        /*return */redirect(controllerClass, CollectionUtil.map("id", id));
    }

    /**
     * Convenience method for {@link #redirect(Class, java.util.Map)}.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link AppController}
     * @param action action to redirect to.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> /*HttpBuilder*/void redirect(Class<T> controllerClass, String action){
        /*return */redirect(controllerClass, CollectionUtil.map("action", action));
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
    protected /*HttpBuilder*/void redirect() {
        /*return */redirect(getRoute().getController().getClass());
    }
    
    /**
     * @see #redirect()
     * 
     * @param params map with request parameters.
     * @return
     * @author MTD
     */
    protected /*HttpBuilder*/void redirect(Map<String, String> params) {
        /*return */redirect(getRoute().getController().getClass(), params);
    }

    /**
     * Redirects to given controller, action "index" without any parameters.
     *
     * @param controllerClass controller class where to send redirect.
     * @param <T> class extending {@link AppController}
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> /*HttpBuilder*/void redirect(Class<T> controllerClass){
        /*return */redirect(controllerClass, new HashMap<String, String>());

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
     * @param <T> class extending {@link AppController}
     * @param params map with request parameters.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    protected <T extends AppController> /*HttpBuilder*/void redirect(Class<T> controllerClass, Map<String, String> params){
        String controllerPath = RouterHelper.getReverseRoute(controllerClass);
        String contextPath = context.contextPath();
        String action = params.get("action") != null? params.get("action") : null;
        String id = params.get("id") != null? params.get("id") : null;
//        boolean restful= AppController.restful(controllerClass);
        params.remove("action");
        params.remove("id");
        
        //MTD
        String lang = language() != null ? "/" + language() : "";
        String anchor = params.get("#") != null ? "#" + params.get("#") : "";
        params.remove("#");

        String uri = contextPath + lang + RouterHelper.generate(controllerPath, action, id, params) + anchor;

//        RedirectResponse resp = new RedirectResponse(context, uri);
//        context.setControllerResponse(resp);
//        return new HttpBuilder(resp);
        //------
        redirect(uri);
    }
    
    

    /**
     * This method will send the text to a client verbatim. It will not use any layouts. Use it to build app.services
     * and to support AJAX.
     *
     * @param text text of response.
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information.
     */
    /*protected HttpBuilder respondText(String text){
        DirectResponse resp = new DirectResponse(text);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }*/
    
    /**
     * This method will send a JSON response to the client.
     * It will not use any layouts.
     * Use it to build app.services and to support AJAX.
     * 
     * @author MTD
     * 
     * @param obj
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information. The builder is automatically
     * has its content type set to "application/json"
     */
    /*protected HttpBuilder respondJson(Object obj) {
        JsonResponse resp = new JsonResponse(obj);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }*/
    
    /**
     * 
     * @author MTD
     * 
     * @return {@link HttpSupport.HttpBuilder}, to accept additional information. The builder is automatically
     * has its content type set to "application/json"
     */
    /*protected HttpBuilder respondStatus(int status) {
        NopResponse resp = new NopResponse(status);
        Context.setControllerResponse(resp);
        return new HttpBuilder(resp);
    }*/

    /**
     * Convenience method for downloading files. This method will force the browser to find a handler(external program)
     *  for  this file (content type) and will provide a name of file to the browser. This method sets an HTTP header
     * "Content-Disposition" based on a file name.
     *
     * @param file file to download.
     * @return builder instance.
     * @throws PathNotFoundException thrown if file not found.
     */
    protected /*HttpBuilder*/void sendFile(File file) throws PathNotFoundException {
        try{
//            StreamResponse resp = new StreamResponse(context, new FileInputStream(file));
//            context.setControllerResponse(resp);
            //TODO finish the god damn method
//            HttpBuilder builder = new HttpBuilder(resp);
//            builder.header("Content-Disposition", "attachment; filename=" + file.getName());
//            return builder;
            
            NewControllerResponse r = NewControllerResponseBuilder.ok()
                    .addHeader("Content-Disposition", "attachment; filename=" + file.getName())
                    .renderable(new FileInputStream(file))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
            context.setNewControllerResponse(r);
        }catch(Exception e){
            throw new PathNotFoundException(e);
        }
    }
    
    
    /**
     * Only to be used by POST/PUT as only they can carry extra information
     * 
     * @return A Request object with helper methods
     */
    protected Request request() {
        return context.createRequest();//new RequestImpl(context.getHttpRequest());
    }
    
    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    protected List<String> params(String name) {
        if (name.equals("id")) {
            String id = getIdString();
            return id != null ? asList(id) : Collections.emptyList();
        } else {
            String[] values = context.requestParameterValues(name);
            List<String>valuesList = values == null? new ArrayList<>() : Arrays.asList(values);
            String routeParameter = context.getRouteParam(name);
            if(routeParameter != null){
                valuesList.add(routeParameter);
            }
            return valuesList;
        }
    }
    
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
            if(formItem.isFormField() && name.equals(formItem.getFieldName())){
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
     * @see Context#param(String)
     */
    protected Param param(String name){
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
            if(formItem.isFormField() && !params.contains(formItem.getFieldName())){
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


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    protected String ipAddress() {
        return context.ipAddress();
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

  //TODO: provide methods for: X-Forwarded-Proto and X-Forwarded-Port
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
        return !StringUtil.blank(h) ? h : remoteAddress();
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
    private String getIdString() {
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
    }
    

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
    * @throws WebException in case field name is not found in the request.
    */
    protected InputStream getFileInputStream(String fieldName, List<FormItem> formItems){
        for (FormItem formItem : formItems) {
            if(formItem.isFile() && formItem.getFieldName().equals(fieldName)){
                return formItem.getInputStream();
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
    protected MultiList<FormItem>/*Map<String, List<FormItem>>*/ multipartFormItems(String encoding) {
//        HttpServletRequest req = context.getHttpRequest();

        //MTD removed what seemed to be unused
//        if (req instanceof AWMockMultipartHttpServletRequest) {//running inside a test, and simulating upload.
//            formItems = ((AWMockMultipartHttpServletRequest) req).getFormItems();
//        } else {

            if (!context.isRequestMultiPart())
                throw new MediaTypeException("this is not a multipart request, be sure to add this attribute to the form: ... enctype=\"multipart/form-data\" ...");

//            DiskFileItemFactory factory = new DiskFileItemFactory();
//
//            factory.setSizeThreshold(Configuration.getMaxUploadSize());
//            factory.setRepository(Configuration.getTmpDir());
//
//            ServletFileUpload upload = new ServletFileUpload(factory);
//            if(encoding != null)
//                upload.setHeaderEncoding(encoding);
//            upload.setFileSizeMax(Configuration.getMaxUploadSize());
            
            MultiList<FormItem> parts = new MultiList<>();
            try {
                List<org.apache.commons.fileupload.FileItem> apacheFileItems = context.parseRequestMultiPartItems(encoding);//upload.parseRequest(context.getHttpRequest());
                if (apacheFileItems != null) {
                    for (FileItem apacheItem : apacheFileItems) {
                        parts.put(apacheItem.getFieldName(), new FormItem(new ApacheFileItemFacade(apacheItem)));
                    }
                }
                return parts;
            } catch (Exception e) {
                throw new ControllerException(e);
            }
//        }
//        return formItems;
    }
    
    
    
    /**
     * @author MTD
     * @param item
     * @return the path to the saved image
     */
    protected ImageHandlerBuilder image(FormItem item) throws ControllerException {
        return new ImageHandlerBuilder(context, item);
    }
    protected ImageHandlerBuilder image(File file) throws PathNotFoundException, ControllerException {
        if (!file.canRead())
            throw new PathNotFoundException(file.getPath());
        return new ImageHandlerBuilder(context, file);
    }
    protected ImageHandlerBuilder image(String name) throws PathNotFoundException, ControllerException {
        File file = new File(getRealPath(name));
        if (!file.canRead())
            throw new PathNotFoundException(file.getPath());
        return new ImageHandlerBuilder(context, file);
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


    /**
     * Sets character encoding for response.
     *
     * @param encoding encoding to be set.
     */
    protected void setResponseEncoding(String encoding) {
        context.setEncoding(encoding);
    }



    /**
     * Sets character encoding on the response.
     *
     * @param encoding character encoding for response.
     */
    protected void setEncoding(String encoding){
        setResponseEncoding(encoding);
    }

    /**
     * Synonym for {@link #setEncoding(String)}
     *
     * @param encoding encoding of response to client
     */
    protected void encoding(String encoding){
        setEncoding(encoding);
    }

    /**
     * Sets content length of response.
     *
     * @param length content length of response.
     */
    protected void setContentLength(int length){
        context.responseContentLength(length);//getHttpResponse().setContentLength(length);
    }

    /**
     * Sets locale on response.
     *
     * @param locale locale for response
     */
    protected void locale(Locale locale){
        context.responseLocale(locale);//.getHttpResponse().setLocale(locale);
    }
    /**
     * Returns locale of request.
     *
     * @return locale of request.
     */
    protected Locale locale(){
        return context.requestLocale();//RequestUtils.locale();
    }


    /**
     * Returns reference to a current session. Creates a new session of one does not exist.
     * @return reference to a current session.
     */
    protected SessionFacade session(){
        return context.getSession();
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
    public List<Cookie> cookies(){
        return context.getCookies();//RequestUtils.cookies();
    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
    public Cookie cookie(String name){
        return context.getCookie(name);//RequestUtils.cookie(name);
    }


    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    protected String cookieValue(String name){
        return cookie(name).getValue();//RequestUtils.cookieValue(name);
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
//        context.getHttpResponse().addCookie(Cookie.toServletCookie(new Cookie(name, value)));
        context.addCookie(new Cookie(name, value));
    }


    /**
     * Sends long to live cookie to browse with response. This cookie will be asked to live for 20 years.
     *
     * @param name name of cookie
     * @param value value of cookie.
     */
    public void sendPermanentCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(60*60*24*365*20);
        context.addCookie(cookie);//getHttpResponse().addCookie(Cookie.toServletCookie(cookie));
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
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    protected  String url(){
        return context.requestUrl();//RequestUtils.url();
    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    protected  String queryString(){
        return context.queryString();//RequestUtils.queryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    protected String method(){
        return context.method();//RequestUtils.method();
    }

    /**
     * True if this request uses HTTP GET method, false otherwise.
     *
     * @return True if this request uses HTTP GET method, false otherwise.
     */
    protected boolean isGet() {
        return "GET".equalsIgnoreCase(method()); //RequestUtils.isGet();
    }


    /**
     * True if this request uses HTTP POST method, false otherwise.
     *
     * @return True if this request uses HTTP POST method, false otherwise.
     */
    protected boolean isPost() {
        return "POST".equalsIgnoreCase(method());
    }


    /**
     * True if this request uses HTTP PUT method, false otherwise.
     *
     * @return True if this request uses HTTP PUT method, false otherwise.
     */
    protected boolean isPut() {
        return "PUT".equalsIgnoreCase(method());//return RequestUtils.isPut();
    }


    /**
     * True if this request uses HTTP DELETE method, false otherwise.
     *
     * @return True if this request uses HTTP DELETE method, false otherwise.
     */
    protected boolean isDelete() {
        return "DELETE".equalsIgnoreCase(method());///return RequestUtils.isDelete();
    }


    /*private boolean isMethod(String method){
        return RequestUtils.isMethod(method);
    }*/


    /**
     * True if this request uses HTTP HEAD method, false otherwise.
     *
     * @return True if this request uses HTTP HEAD method, false otherwise.
     */
    protected boolean isHead() {
        return "HEAD".equalsIgnoreCase(method());//RequestUtils.isHead();
    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    protected String context(){
        return context.contextPath();//RequestUtils.context();
    }


    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    protected String uri(){
        return context.requestUri();//RequestUtils.uri();
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
    protected String remoteAddress(){
        return context.remoteAddress();
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
    protected Map<String, String> headers(){
        return context.requestHeaders();
    }

    /**
     * Adds a header to response.
     *
     * @param name name of header.
     * @param value value of header.
     */
    protected void header(String name, String value){
        context.addResponseHeader(name, value);
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
        NewControllerResponse r = NewControllerResponseBuilder.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .renderable(in);
        context.setNewControllerResponse(r);
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
        return context.getRealPath(path);
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
    protected OutputStream outputStream(String contentType, Map<String, String> headers, int status) {
//        context.setControllerResponse(new NopResponse(context, contentType, status));
        //------
        NewControllerResponse r = new NewControllerResponse(200);
        r.contentType(contentType).status(status);
        context.setNewControllerResponse(r);
        
        try {
            if (headers != null) {
                for (String key : headers.keySet()) {
                    if (headers.get(key) != null)
                        context.addResponseHeader(key.toString(), headers.get(key).toString());
                }
            }

            return context.responseOutputStream();
        }catch(Exception e){
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
    protected PrintWriter writer(){
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
    protected PrintWriter writer(String contentType, Map<String, String> headers, int status){
//        context.setControllerResponse(new NopResponse(context, contentType, status));
        //TODO finish the god damn method
        try{
            if (headers != null) {
                for (Object key : headers.keySet()) {
                    if (headers.get(key) != null)
                        context.addResponseHeader(key.toString(), headers.get(key).toString());
                }
            }

            return context.responseWriter();
        }catch(Exception e){
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

    /**
     * Returns instance of {@link AppContext}.
     *
     * @return instance of {@link AppContext}.
     */
    protected AppContext appContext(){
        return context.createAppContext();//context.getAppContext();
    }

    /**
     * Returns a format part of the URI, or null if URI does not have a format part.
     * A format part is defined as part of URI that is trailing after a last dot, as in:
     *
     * <code>/books.xml</code>, here "xml" is a format.
     *
     * @return format part of the URI, or nul if URI does not have it.
     */
    protected String format(){
        return context.getRouteFormat();
    }


    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    protected NewRoute getRoute(){
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
        StringWriter stringWriter = new StringWriter();
        
        TemplateEngineManager manager = injector.getInstance(TemplateEngineManager.class);
        TemplateEngine engine = manager.getTemplateEngineForContentType(MediaType.TEXT_HTML);
        engine.invoke(
                context, 
                NewControllerResponseBuilder.ok().addAllViewObjects(values).template(template), 
                new ResponseStream() {
                    @Override
                    public Writer getWriter() throws IOException {
                        return stringWriter;
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        return null;
                    }
                });
        
        
//        Configuration.getTemplateManager().merge(values, template, stringWriter);
        return stringWriter.toString();
        
    }
    
    /**
     * Returns response headers
     *
     * @return map with response headers.
     */
    protected Map<String, String> getResponseHeaders(){
        Collection<String> names  = context.responseHeaderNames();
        Map<String, String> headers = new HashMap<String, String>();
        for (String name : names) {
            headers.put(name, context.requestHeader(name));
        }
        return headers;
    }
    
    /*
     * MTD section
     */
    protected String language() {
        return context.getRouteLanguage();
    }
    
    protected String contentType() {
        return context.requestContentType();
    }
}