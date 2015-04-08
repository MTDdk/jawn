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
package net.javapla.jawn.trash;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javapla.jawn.AppController;
import net.javapla.jawn.Context;
import net.javapla.jawn.ControllerRegistry;
import net.javapla.jawn.HttpMethod;
//import net.javapla.jawn.HttpSupport;
//import net.javapla.jawn.NewRoute;
//import net.javapla.jawn.ParamCopy;
//import net.javapla.jawn.PropertiesImpl;
//import net.javapla.jawn.RouterHelper;
//import net.javapla.jawn.ControllerRegistry.FilterList;
import net.javapla.jawn.controller_filters.ControllerFilter;
import net.javapla.jawn.exceptions.ActionNotFoundException;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.FilterException;
import net.javapla.jawn.exceptions.WebException;
import net.javapla.jawn.templatemanagers.AbstractTemplateConfig;
import net.javapla.jawn.util.Constants;
import net.javapla.jawn.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * One of the main classes of the framework, responsible for execution of controllers and filters.
 * 
 * @author Igor Polevoy
 */
@Deprecated
class ControllerRunner {

//    private static Logger logger = LoggerFactory.getLogger(ControllerRunner.class.getName());
//    private boolean tagsInjected = false;
//    
//    private final PropertiesImpl properties;
//    private final HttpServletRequest request;
//    private final HttpServletResponse response;
//    private final ControllerRegistry controllerRegistry;
//    
//    private final Context context;
//    
//    //TODO base the injected constructor parameters on what can be injected and let the rest be parameters of run()
//    ControllerRunner(PropertiesImpl properties, HttpServletRequest request, HttpServletResponse response, ControllerRegistry registry, Context context) {
//        this.properties = properties;
//        this.request = request;
//        this.response = response;
//        this.controllerRegistry = registry;
//        
//        this.context = context;
//    }
//    
//    protected void run(NewRoute route, boolean integrateViews) throws Exception {
//        // if we want to reload the controller, this is a good time to do it
//        if (! properties.isProd()) {
//            route.reloadController();
//        }
//        
//        
//        List<ControllerRegistry.FilterList> globalFilterLists = controllerRegistry.getGlobalFilterLists();
//        List<ControllerFilter> controllerFilters = controllerRegistry.getMetaData(route.getController().getClass()).getFilters(route.getAction());
//
//        /*context.getControllerRegistry().*/controllerRegistry.injectFilters(); //will execute once, really filters are persistent
//        
//        
//        ensureResponseEncoding(route.getController());
//
//        try {
//            filterBefore(route.getController(), globalFilterLists, controllerFilters);
//
//            if (context.getControllerResponse() == null) {//execute controller... only if a filter did not respond
//
//                String actionMethod = route.getAction();//Inflector.camelize(route.getActionName().replace('-', '_'), false);
//                if (checkActionMethod(route.getController(), actionMethod)) {
//                    //Configuration.getTemplateManager().
//                    injectControllerWithContext(route.getController());
//                    injectControllerWithUserDependencies(route.getController());
//                    if(properties.getBoolean(Constants.LOG_REQUESTS)){
//                        logger.info("Executing controller: " + route.getController().getClass().getName() + "." + actionMethod);
//                    }
//                    executeAction(route/*.getController(), actionMethod*/);
//                }
//            }
//
//            injectTemplateConfigTags();
//            renderResponse(route, integrateViews);
//            processFlash();
//
//            //run filters in opposite order
//            filterAfter(route.getController(), globalFilterLists, controllerFilters);
//        }
//        catch(ActionNotFoundException e){
//            throw e;
//        }
//        catch (RuntimeException e) {
//            context.setNewControllerResponse(null);//must blow away, as this response is not valid anymore.
//
//            if (exceptionHandled(e, route, globalFilterLists, controllerFilters)) {
//                logger.debug("A filter has called render(..) method, proceeding to render it...");
//                renderResponse(route, integrateViews);//a filter has created an instance of a controller response, need to render it.
//            }else{
//                throw e;//if exception was not handled by filter, re-throw
//            }
//        }
//    }
//
//    /**
//     * Makes sure a encoding has been set on the response.
//     * By getting executed before any filters and controller this method enforces
//     * the following priority:
//     *  <ul>
//     *  <li>action
//     *  <li>controller
//     *  <li>appcontext
//     *  </ul>
//     * @param controller
//     * @author MTD
//     */
//    private void ensureResponseEncoding(AppController controller) {
//        //either the controller or the appcontext sets the encoding.
//        //if a filter or action sets encoding after this method,
//        //that will take precedence, and hence this method needs to run
//        //before anything else
//        if (controller.getEncoding() != null) {
//            response.setCharacterEncoding(controller.getEncoding());
//        } else {
//            response.setCharacterEncoding(context.createAppContext().getEncoding());
//        }
//    }
//
//    /**
//     * Injects FreeMarker tags with dependencies from Guice module.
//     */
//  //TODO is the same possible with StringTemplate?
//    private void injectTemplateConfigTags() {
//        if(!tagsInjected){
//            AbstractTemplateConfig<?> templateConfig = Configuration.getTemplateConfig(); 
//
//            Injector injector = controllerRegistry.getInjector();
//            tagsInjected = true;
//            if(injector == null || templateConfig == null){
//                return;
//            }
//            templateConfig.inject(injector);
//        }
//    }
//
//    /**
//     * Injects controller with dependencies from Guice module.
//     */
//    private void injectControllerWithUserDependencies(AppController controller) {
//        Injector injector = /*context.getControllerRegistry().*/controllerRegistry.getInjector();
//        if (injector != null) {
//            injector.injectMembers(controller);
//        }
//    }
//    
//    private void injectControllerWithContext(HttpSupport controller) {
//        controller.init(context);
//    }
//    
//    private void renderResponse(NewRoute route,  boolean integrateViews) throws InstantiationException, IllegalAccessException {
//        //set headers
//        response.addHeader("X-Powered-By", "java-web-planet / jawn");
//
//        //all layouting is done by convention, so remove the option to define this in the controller
//        ControllerResponse controllerResponse = context.getControllerResponse();
////        String controllerLayout = route.getController().getLayout();
//        if (controllerResponse == null) {
//            createDefaultResponse(route/*, controllerLayout*/);
//        } else if (controllerResponse instanceof RenderTemplateResponse) {
//            configureExplicitResponse(route/*, controllerLayout*/, (RenderTemplateResponse) controllerResponse);
//        }
//
//        controllerResponse = context.getControllerResponse();
//        if (integrateViews && controllerResponse instanceof RenderTemplateResponse) {
//            ParamCopy.copyInto(/*controllerResponse.values(), */request, context, properties);
//            controllerResponse.process();
//        }else if(!(controllerResponse instanceof RenderTemplateResponse)){
//            if(controllerResponse.getContentType() == null){
//                controllerResponse.setContentType(route.getController().getContentType());
//            }
//            controllerResponse.process();
//        }
//    }
//
//    //this is configuration of explicit response. If render() method was called in controller, we already have instance of
//    // response on current thread.
//    private void configureExplicitResponse(NewRoute route/*, String controllerLayout*/, RenderTemplateResponse resp) throws InstantiationException, IllegalAccessException {
////            String responseLayout = resp.getLayout();
////            if(!Configuration.getDefaultLayout().equals(controllerLayout) && Configuration.getDefaultLayout().equals(responseLayout)){
////                resp.setLayout(controllerLayout);
////            }
//            if(resp.getContentType() == null){
//                resp.setContentType(route.getController().getContentType());
//            }
//            resp.setTemplateManager(Configuration.getTemplateManager());
//    }
//
//    // this is implicit processing - default behavior, really
//    private void createDefaultResponse(NewRoute route/*, String controllerLayout*/) throws InstantiationException, IllegalAccessException {
//        String controllerPath = RouterHelper.getReverseRoute(route.getController().getClass());
//        //TODO Route(r).reverseRoute
//        String template =  controllerPath + "/" + route.getActionName();
//        RenderTemplateResponse resp = new RenderTemplateResponse(context, route.getController().values(), template/*, context.getFormat()*/);
////        if(!Configuration.getDefaultLayout().equals(controllerLayout)){
////            resp.setLayout(controllerLayout);//could be a real layout ot null for no layout
////        }
//        if(resp.getContentType() == null){
//            resp.setContentType(route.getController().getContentType());
//        }
//        context.setControllerResponse(resp);
//        resp.setTemplateManager(Configuration.getTemplateManager());
//    }
//
//
//    private void processFlash() {
//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            Object flashObj = session.getAttribute("flasher");
//            if (flashObj != null && flashObj instanceof Map) {
//                @SuppressWarnings("unchecked")
//                Map<String, Object> flasher = (Map<String, Object>) flashObj;
//                if (flasher.get("count") == null) { //just created
//                    flasher.put("count", 0);
//                } else if (flasher.get("count").equals(0)) {
//                    session.removeAttribute("flasher");
//                }
//            }
//        }
//    }
//
//    private boolean checkActionMethod(AppController controller, String actionMethod) {
//        HttpMethod method = HttpMethod.getMethod(request);
//        if (!controller.isAllowedAction(actionMethod)/* .actionSupportsHttpMethod(actionMethod, method)*/) {
//            DirectResponse res = new DirectResponse(context, "");
//            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
//            res.setStatus(405);
//            logger.warn("Requested action does not support HTTP method: " + method.name() + ", returning status code 405.");
//            context.setControllerResponse(res);
//            //see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
//            /*context.getHttpResponse()*/response.setHeader("Allow", StringUtil.join(controller.allowedMethodsForAction(actionMethod), ", "));
//            return false;
//        }
//        return true;
//    }
//
//    private boolean exceptionHandled(Exception e, NewRoute route, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter> filterGroups) throws Exception{
//
//        //first, process global filters and account for exceptions
//        for (ControllerRegistry.FilterList filterList : globalFilterLists) {
//            if (!filterList.excludesController(route.getController())) {
//                List<ControllerFilter> filters = filterList.getFilters();
//                for (ControllerFilter controllerFilter : filters) {
//                    controllerFilter.onException(e);
//                }
//            }
//        }
//
////        for(List<ControllerFilter> filterGroup: filterGroups){
//            for (ControllerFilter controllerFilter : filterGroups) {
//                controllerFilter.onException(e);
//            }
////        }
//        return context.getControllerResponse() != null;
//    }
//
//    private void filterBefore(AppController controller, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter> filterGroups) {
//        try {
//
//            //first, process global filters and account for exceptions
//            for (ControllerRegistry.FilterList filterList : globalFilterLists) {
//                if(!filterList.excludesController(controller)){
//                    List<ControllerFilter> filters = filterList.getFilters();
//                    for (ControllerFilter controllerFilter : filters) {
//                        controllerFilter.before(context);
//                    }
//                }
//            }
//
//            //then process all other filters
////            for (List<ControllerFilter> filterGroup : filterGroups) {
//                for (ControllerFilter controllerFilter : filterGroups/*filterGroup*/) {
//                    if (properties.getBoolean(Constants.LOG_REQUESTS)) {//Configuration.logRequestParams()) {
//                        logger.debug("Executing filter: " + controllerFilter.getClass().getName() + "#before");
//                    }
//                    controllerFilter.before(context);
//                    if (context.getControllerResponse() != null) return;//a filter responded!
//                }
////            }
//        }catch(RuntimeException e){
//            throw e;
//        }catch(Exception e){
//            throw new FilterException(e);
//        }
//    }
//
//    private void filterAfter(AppController controller, List<ControllerRegistry.FilterList> globalFilterLists, List<ControllerFilter>/*...*/ filterGroups) {
//        try {
//
//            //first, process global filters and account for exceptions
//            for (ControllerRegistry.FilterList filterList : globalFilterLists) {
//                if(!filterList.excludesController(controller)){
//                    List<ControllerFilter> filters = filterList.getFilters();
//                    for (ControllerFilter controllerFilter : filters) {
//                        controllerFilter.after(context);
//                    }
//                }
//            }
//
////            for (List<ControllerFilter> filterGroup : filterGroups) {
//                for (int i = filterGroups.size() - 1; i >= 0; i--) {
//                    if(properties.getBoolean(Constants.LOG_REQUESTS)) {
//                        logger.debug("Executing filter: " + filterGroups.get(i).getClass().getName() + "#after" );
//                    }
//                    filterGroups.get(i).after(context);
//                }
////            }
//        } catch (Exception e) {
//            throw  new FilterException(e);
//        }
//    }
//
//    private void executeAction(/*Object controller, String actionName, */NewRoute route) {
//        try{
//            
////            route.getController().getClass().getMethod(route.getAction()).invoke(route.getController());
//            
//            //MTD: find the method name case insensitive
//            //EDIT: this is probably not needed anymore
//            String methodName = route.getAction();//actionName.toLowerCase();
//            for (Method method : route.getController().getClass().getMethods()) {
//                if (methodName.equals( method.getName().toLowerCase() )) {
//                    method.invoke(route.getController());
//                    return;
//                }
//            }
//            throw new ControllerException(String.format("Action name (%s) not found in controller (%s)", route.getAction(), route.getController().getClass().getSimpleName()));
//            
////            Method m = controller.getClass().getMethod(actionName);
////            m.invoke(controller);
//        }catch(InvocationTargetException e){
//            if(e.getCause() != null && e.getCause() instanceof  WebException){
//                throw (WebException)e.getCause();                
//            }else if(e.getCause() != null && e.getCause() instanceof RuntimeException){
//                throw (RuntimeException)e.getCause();
//            }else if(e.getCause() != null){
//                throw new ControllerException(e.getCause());
//            }
//        }catch(WebException e){
//            throw e;
//        }catch(Exception e){
//            throw new ControllerException(e);
//        }
//    }
    
}
