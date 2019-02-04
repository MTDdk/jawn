package net.javapla.jawn.core.internal.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import net.javapla.jawn.core.mvc.DELETE;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.HEAD;
import net.javapla.jawn.core.mvc.OPTIONS;
import net.javapla.jawn.core.mvc.POST;
import net.javapla.jawn.core.mvc.PUT;
import net.javapla.jawn.core.mvc.Path;

public class MvcRouter {
    
    private static final Set<Class<? extends Annotation>> VERBS = new HashSet<>(Arrays.asList(
        GET.class, POST.class, PUT.class, DELETE.class, HEAD.class, OPTIONS.class));

    public static List<Route.Builder> extract(final Class<?> routeClass) {
        
        final String[] rootPaths = paths(routeClass);
        
        //TODO what if a controller has two actions with the same VERB, but no Paths, or the same Path for both actions?
        
        Map<Method, List<Class<? extends Annotation>>> actions = new HashMap<>();
        
        // collect all methods
        methods(routeClass, methods -> 
            routes(methods, actions::put)
        );
        
        ArrayList<Route.Builder> defs = new ArrayList<>();
        
        actions.keySet().forEach(action -> {
            List<Class<? extends Annotation>> verbs = actions.get(action);
            
            String[] paths = mergePaths(rootPaths, action);
            
            for (Class<? extends Annotation> verb : verbs) {
                HttpMethod http = HttpMethod.valueOf(verb.getSimpleName());
                
                for (var path : paths) {
                    defs.add(new Route.Builder(http)
                        .path(path)
                        .handler(new MvcMethodHandler(action, routeClass)));
                }
            }
        });
        
        return defs;
    }
    
    /**
     * Locate methods with the following properties:
     * - public
     * - non-static
     * 
     * @param routeClass
     * @param callback
     */
    static void methods(final Class<?> routeClass, final Consumer<Method[]> callback) {
        if (routeClass != Object.class) {
            Method[] methods = routeClass.getDeclaredMethods();
            Method[] publics = new Method[methods.length];
            int num = 0;
            
            for (Method method : methods) {
                if (    Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {
                    
                    publics[num++] = method;
                }
            }
            
            callback.accept(Arrays.copyOf(publics, num, Method[].class));
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
    
    static String[] paths(final AnnotatedElement elm) {
//        Path path = elm.getAnnotation(Path.class);
//        System.out.println(Arrays.toString(elm.getAnnotationsByType(Path.class)));
//        System.out.println(Arrays.toString(elm.getAnnotationsByType(Path.Paths.class)));
//        System.out.println(elm.getAnnotation(Path.Paths.class));
//        System.out.println(elm.getAnnotation(Path.class));
        
        Path[] paths = elm.getAnnotationsByType(Path.class);
        
        if (paths == null || paths.length == 0) return null;
        
        /*for (Path p : paths.value()) {
            if (p.value().charAt(0) != '/') return addLeadingSlash(paths.value());
        }*/
        
        //if (path.value().charAt(0) != '/') return '/' + path.value();
        //return path.value();
        return addLeadingSlash(paths);
    }
    
    static String[] mergePaths(final String[] rootPaths, final Method method) {
        String[] action = paths(method);
        if (rootPaths == null) {
            if (action == null) {
                throw new IllegalArgumentException("No path found for: " + method);
            }
            return action;
        }
        
        if (action == null) {
            return rootPaths;
        }
        
        //return rootPaths + action;
        String[] result = new String[rootPaths.length * action.length];
        int k = 0;
        for (String base : rootPaths) {
            for (String element : action) {
                result[k] = base + element;
                k += 1;
            }
        }
        return result;
    }
    
    static String[] addLeadingSlash(final Path[] paths) {
        String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            if (paths[i].value().charAt(0) != '/') result[i] = '/' + paths[i].value();
            else result[i] = paths[i].value();
        }
        return result;
    }
}
