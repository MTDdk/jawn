package net.javapla.jawn.core.internal.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.Path;

public class MvcRouter {
    
    private static final Set<Class<? extends Annotation>> VERBS = new HashSet<>(Arrays.asList(GET.class));

    public static List<Route.Builder> extract(final Class<?> routeClass) {
        
        final String rootPath = path(routeClass);
        
        //TODO what if a controller has two actions with the same VERB, but no Paths, or the same Path for both actions?
        
        // collect all methods
        Map<Method, List<Class<? extends Annotation>>> actions = new HashMap<>();
        routes(routeClass.getMethods(), actions::put);
        
        ArrayList<Route.Builder> defs = new ArrayList<>();
        
        actions.keySet().forEach(action -> {
            List<Class<? extends Annotation>> verbs = actions.get(action);
            
            String path = mergePaths(rootPath, action);
            
            for (Class<? extends Annotation> verb : verbs) {
                HttpMethod http = HttpMethod.valueOf(verb.getSimpleName());
                
                defs.add(new Route.Builder(http)
                    .path(path)
                    .handler(new MvcMethodHandler(action, routeClass)));
            }
        });
        
        return defs;
    }
    
    static void methods(final Class<?> routeClass, final Consumer<Method[]> callback) {
        if (routeClass != Object.class) {
            callback.accept(routeClass.getDeclaredMethods());
            methods(routeClass.getSuperclass(), callback);
        }
    }
    
    static void routes(final Method[] methods, final BiConsumer<Method, List<Class<? extends Annotation>>> annotationsCallback) {
        for (Method method : methods) {
            ArrayList<Class<? extends Annotation>> annotations = new ArrayList<>(VERBS.size()); // set to the number of possible annotations
            
            VERBS.forEach(type -> {
                if (method.getAnnotation(type) != null) { // might be a bit faster than <code>method.isAnnotationPresent(type);</code> 
                    annotations.add(type);
                }
            });
            
            if (annotations.size() > 0) {
                annotationsCallback.accept(method, annotations);
            } else if (method.isAnnotationPresent(Path.class)) {
                // Assume GET
                annotationsCallback.accept(method, Collections.singletonList(GET.class));
            }
        }
    }
    
    static String path(final AnnotatedElement elm) {
        Path path = elm.getAnnotation(Path.class);
        if (path == null) return null;
        
        if (path.value().charAt(0) != '/') return '/' + path.value();
        return path.value();
    }
    
    static String mergePaths(final String rootPath, final Method method) {
        String action = path(method);
        if (rootPath == null) {
            if (action == null) {
                throw new IllegalArgumentException("No path found for: " + method);
            }
            return action;
        }
        
        if (action == null) {
            return rootPath;
        }
        
        return rootPath + action;
    }
}
