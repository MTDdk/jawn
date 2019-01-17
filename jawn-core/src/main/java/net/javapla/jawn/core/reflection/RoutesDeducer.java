package net.javapla.jawn.core.reflection;

import java.util.Optional;

import org.slf4j.LoggerFactory;

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
    
    private final RouteTrie trie = new RouteTrie();
    private final FiltersHandler filters;
    private final ActionInvoker invoker;

    public RoutesDeducer(FiltersHandler filters, ActionInvoker invoker) {
        this.filters = filters;
        this.invoker = invoker;
    }
    
    public final RoutesDeducer deduceRoutesFromControllers(final String controllerPackage) {
        
        try {
            ControllerLocator locator = new ControllerLocator(controllerPackage);//PropertiesConstants.CONTROLLER_PACKAGE);
        
            locator.controllerActions
                .forEach((controllername,actions) -> {
                    // /{controller}
                    constructControllerRoute(locator, controllername);
                    
                    // /{controller}/{action}
                    actions.forEach(action -> {
                        // find the httpmethod
                        extractHttpMethod(action)
                            .ifPresent(method -> {
                                String actionName = action.substring(method.name().length());
                                actionName = StringUtil.underscore(actionName);
                                constructActionRoute(locator, controllername, action, actionName, method);
                            });
                        
                    });
                });
            
            // insert IndexController
            if (locator.containsControllerPath(Constants.ROOT_CONTROLLER_NAME)) {//finder.controllerExists(Constants.ROOT_CONTROLLER_NAME)) {
                constructRoute(locator, "/", Constants.ROOT_CONTROLLER_NAME, Constants.DEFAULT_ACTION_NAME, HttpMethod.GET);
            }
        
        } catch (IllegalArgumentException e) {
            LoggerFactory
                .getLogger(getClass().getName())
                .error(getClass().getSimpleName() + " did not find any controllers in " + controllerPackage/*PropertiesConstants.CONTROLLER_PACKAGE */+ " - not doing that, then..");
        }
        
        return this;
    }
    
    public RouteTrie getRoutes() {
        return trie;
    }
    
    
    private void constructControllerRoute(ControllerLocator locator, String controllername) {
//        String uri = new StringBuilder()/*.append('/')*/.append(controllername).toString();
        constructRoute(locator, controllername, controllername, Constants.DEFAULT_ACTION_NAME, HttpMethod.GET);
    }
    
    private void constructActionRoute(ControllerLocator locator, String controllername, String action, String actionName, HttpMethod method) {
        String uri = new StringBuilder()/*.append('/')*/.append(controllername).append('/').append(actionName).toString();
        constructRoute(locator, uri, controllername, action, method);
    }

    private void constructRoute(ControllerLocator locator, String uri, String controllername, String action, HttpMethod method) {
//        System.out.println("+ " +uri + " -> " + controllername + " / " + action + " <- "  + method);
        Route route = RouteBuilder
            .method(method)
            .to((Class<? extends Controller>) locator.controllers.get(controllername), action)
            .route(uri)
            .build(filters, invoker);
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
