package net.javapla.jawn.core.internal.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.annotation.DELETE;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.HEAD;
import net.javapla.jawn.core.annotation.OPTIONS;
import net.javapla.jawn.core.annotation.POST;
import net.javapla.jawn.core.annotation.PUT;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.annotation.Produces;

public class AnnotationScanner {
    static final List<Class<? extends Annotation>> VERBS = Arrays.asList(GET.class, POST.class, PUT.class, DELETE.class, HEAD.class, OPTIONS.class);
    static final List<Class<? extends Annotation>> GET_VERB = Collections.singletonList(GET.class);

    
    public static List<Route.Builder> scan(Class<?> controller, Registry registry) {
        final String rootPath = path(controller);
        final MediaType rootProduces = produces(controller);
        
        // map all methods and their verbs
        Map<Method, List<Class<? extends Annotation>>> actions = methods(controller);
        
        LinkedList<Route.Builder> routes = new LinkedList<>();
        
        actions.forEach((action, verbs) -> {
            String path = mergePaths(rootPath, action);
            if (path == null) return;
            
            MediaType produces = produces(action);
            
            for (Class<? extends Annotation> annotation : verbs) {
                HttpMethod method = HttpMethod.valueOf(annotation.getSimpleName());
                
                routes.add(
                    new Route.Builder(method, path, new ActionHandler(action, controller, registry))
                    .produces(produces != null ? produces : rootProduces)
                );
            }
        });
        
        return routes;
    }
    
    static String path(AnnotatedElement elm) {
        Path path = elm.getAnnotation(Path.class);
        if (path == null) return null;
        return ensureLeadingSlash(path);
    }
    
    static String ensureLeadingSlash(Path path) {
        if (path.value().charAt(0) != '/') 
            return '/' + path.value();
        else 
            return path.value();
    }
    
    static MediaType produces(AnnotatedElement elm) {
        Produces annotation = elm.getAnnotation(Produces.class);
        
        if (annotation != null) {
            return MediaType.valueOf(annotation.value());
        }
        
        return null;
    }
    
    static Map<Method, List<Class<? extends Annotation>>> methods(Class<?> controller) {
        return methods(controller, new HashMap<>());
    }
    
    /**
     * Locate method with the following properties:
     * - public
     * - non-static
     * 
     * @param controller
     * @return 
     * @return 
     */
    static Map<Method, List<Class<? extends Annotation>>> methods(Class<?> controller, Map<Method, List<Class<? extends Annotation>>> actions) {
        if (controller != Object.class) return actions; // stop condition for the recursive call
        
        Method[] methods = controller.getDeclaredMethods();
        
        for (Method method : methods) {
            if ( Modifier.isPublic(method.getModifiers()) &&
                !Modifier.isStatic(method.getModifiers())) {
                
                // method is public and non-static
                List<Class<? extends Annotation>> verbs = verbs(method);
                if (verbs != null)
                    actions.put(method, verbs);
            }
        }
        
        return methods(controller.getSuperclass(), actions);
    }

    /**
     * Locates all the annotation verbs on the methods input
     * @param methods
     * @return 
     */
    static List<Class<? extends Annotation>> verbs(Method method) {
        
        LinkedList<Class<? extends Annotation>> annotations = new LinkedList<>();
        
        VERBS.forEach(type -> {
            if (method.isAnnotationPresent(type))
                annotations.add(type);
        });
        
        if (annotations.size() > 0) {
            return annotations;
        } else if (method.isAnnotationPresent(Path.class)) {
            // assume GET
            return GET_VERB;
        }
        
        return null;
    }
    
    static String mergePaths(final String rootPath, final Method method) {
        String action = path(method);
        
        if (rootPath == null) {
            if (action == null) {
                //throw new IllegalArgumentException("No path found for: " + method);
                return null;
            }
            return action;
        }
        
        if (action == null) {
            return rootPath;
        }
        
        if (rootPath.equals("/")) { // the 'index' / standard case
            return action;
        } else {
            return rootPath + action;
        }
    }
}
