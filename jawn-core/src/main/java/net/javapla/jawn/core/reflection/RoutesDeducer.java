package net.javapla.jawn.core.reflection;

import java.text.MessageFormat;
import java.util.Optional;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.RouteBuilder;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.RouteTrie;
import net.javapla.jawn.core.util.StringUtil;

import com.google.inject.Injector;

public class RoutesDeducer {
    
    private String controller = "/{0}";
    private String controller_action = "/{0}/{1}";
    
    RouteTrie trie = new RouteTrie();
    private Injector injector;
    private FiltersHandler filters;

    public RoutesDeducer(Injector injector, FiltersHandler filters) {
        this.injector = injector;
        this.filters = filters;
    }
    
    public RoutesDeducer deduceRoutesFromControllers() {
        
        ControllerFinder finder = new ControllerFinder(Constants.CONTROLLER_PACKAGE);
        
        finder.controllerActions
            .forEach((controllername,actions) -> {
                // /{controller}
                constructControllerRoute(finder, controllername);
                
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
                        constructActionRoute(finder, controllername, actionName, method);
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
        if (finder.controllerExists(Constants.ROOT_CONTROLLER_NAME)) {
            constructRoute(finder, "/", Constants.ROOT_CONTROLLER_NAME, Constants.DEFAULT_ACTION_NAME, HttpMethod.GET);
        }
        
        return this;
    }
    
    public RouteTrie getRoutes() {
        return trie;
    }
    
    
    private void constructControllerRoute(ControllerFinder finder, String controllername) {
        String uri = MessageFormat.format(controller, controllername);
        constructRoute(finder, uri, controllername, Constants.DEFAULT_ACTION_NAME, HttpMethod.GET);
    }
    
    private void constructActionRoute(ControllerFinder finder, String controllername, String actionName, HttpMethod method) {
        String uri = MessageFormat.format(controller_action, controllername, actionName);
        constructRoute(finder, uri, controllername, actionName, method);
    }

    @SuppressWarnings("unchecked")
    private void constructRoute(ControllerFinder finder, String uri, String controllername, String actionName, HttpMethod method) {
//        System.out.println("+ " +uri + " -> " + controllername + " / " + actionName + " <- "  + method);
        Route route = RouteBuilder
            .method(method)
            .to((Class<? extends Controller>) finder.controllers.get(controllername), actionName)
            .route(uri)
            .build(filters, injector);
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
