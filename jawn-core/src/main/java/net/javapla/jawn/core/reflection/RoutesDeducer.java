package net.javapla.jawn.core.reflection;

import java.text.MessageFormat;
import java.util.Optional;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.routes.RouteBuilder;
import net.javapla.jawn.core.routes.RouteTrie;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.PropertiesConstants;
import net.javapla.jawn.core.util.StringUtil;

public class RoutesDeducer {
    
    private String controller = "/{0}";
    private String controller_action = "/{0}/{1}";
    
    RouteTrie trie = new RouteTrie();
    private final FiltersHandler filters;
//    private Injector injector;
    private final ActionInvoker invoker;

    public RoutesDeducer(/*,*/ FiltersHandler filters, ActionInvoker invoker/*, Injector injector*/) {
        this.filters = filters;
//        this.injector = injector;
        this.invoker = invoker;
    }
    
    public RoutesDeducer deduceRoutesFromControllers() {
        
        //ControllerFinder finder = new ControllerFinder(PropertiesConstants.CONTROLLER_PACKAGE);
        ControllerLocator locator = new ControllerLocator(PropertiesConstants.CONTROLLER_PACKAGE);
        
//        System.out.println(finder.controllerActions.keySet());
//        System.out.println(locator.controllerActions.keySet());
        
        /*finder*/locator.controllerActions
            .forEach((controllername,actions) -> {
                // /{controller}
                constructControllerRoute(locator, controllername);
                
                // /{controller}/{action}
                actions.forEach(action -> {
                 // find the httpmethod
                    extractHttpMethod(action).ifPresent(method -> {
                        String actionName = action.substring(method.name().length());
                        actionName = StringUtil.underscore(actionName);
                        
                        /*String uri = MessageFormat.format(controller_action, controllername, actionName);
                        System.out.println(uri + " -> " + controllername + " / " + actionName + " <- " + action + " " + method);
                        Route actionroute = RouteBuilder
                                .method(method)
                                .to((Class<? extends Controller>) finder.controllers.get(controllername), actionName)
                                .route(uri)
                                .build(filters, injector);
                        trie.insert(uri, actionroute, method);*/
                        constructActionRoute(locator, controllername, actionName, method);
                    });
                    /*try {
                    HttpMethod method;
                        method = HttpMethod.valueOf(StringUtil.firstPartOfCamelCase(action).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // the first part of the action was not a HTTP method and was thusly
                        // not an actual action
                        return;
                    }*/
                    
                });
            });
        
        // insert IndexController
        if (locator.containsControllerPath(Constants.ROOT_CONTROLLER_NAME)) {//finder.controllerExists(Constants.ROOT_CONTROLLER_NAME)) {
            constructRoute(locator, "/", Constants.ROOT_CONTROLLER_NAME, Constants.DEFAULT_ACTION_NAME, HttpMethod.GET);
        }
        
        return this;
    }
    
    public RouteTrie getRoutes() {
        return trie;
    }
    
    
    private void constructControllerRoute(ControllerLocator locator, String controllername) {
        String uri = MessageFormat.format(controller, controllername);
        constructRoute(locator, uri, controllername, Constants.DEFAULT_ACTION_NAME, HttpMethod.GET);
    }
    
    private void constructActionRoute(ControllerLocator locator, String controllername, String actionName, HttpMethod method) {
        String uri = MessageFormat.format(controller_action, controllername, actionName);
        constructRoute(locator, uri, controllername, actionName, method);
    }

    @SuppressWarnings("unchecked")
    private void constructRoute(ControllerLocator locator, String uri, String controllername, String actionName, HttpMethod method) {
//        System.out.println("+ " +uri + " -> " + controllername + " / " + actionName + " <- "  + method);
        Route route = RouteBuilder
            .method(method)
            .to((Class<? extends Controller>) locator.controllers.get(controllername), actionName)
            .route(uri)
            .build(filters, invoker/*injector*/);
        trie.insert(uri, route);
    }
    
    private Optional<HttpMethod> extractHttpMethod(String action) {
        try {
            return Optional.of(HttpMethod.valueOf(StringUtil.firstPartOfCamelCase(action).toUpperCase()));
        } catch (IllegalArgumentException e) {
            // the first part of the action was not a HTTP method and was thusly
            // not an actual action
            return Optional.empty();
        }
    }
}
