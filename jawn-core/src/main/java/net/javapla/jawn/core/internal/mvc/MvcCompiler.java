package net.javapla.jawn.core.internal.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import net.javapla.jawn.core.annotation.AfterFilter;
import net.javapla.jawn.core.annotation.BeforeFilter;
import net.javapla.jawn.core.annotation.Consumes;
import net.javapla.jawn.core.annotation.DELETE;
import net.javapla.jawn.core.annotation.Filter;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.HEAD;
import net.javapla.jawn.core.annotation.OPTIONS;
import net.javapla.jawn.core.annotation.POST;
import net.javapla.jawn.core.annotation.PUT;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.annotation.Produces;
import net.javapla.jawn.core.internal.injection.Provider;
import net.javapla.jawn.core.internal.reflection.ClassMeta;

public abstract class MvcCompiler {
    static final List<Class<? extends Annotation>> VERBS = Arrays.asList(
        GET.class,
        POST.class,
        PUT.class,
        DELETE.class,
        HEAD.class,
        OPTIONS.class
    );
    static final List<Class<? extends Annotation>> GET_VERB = Collections.singletonList(GET.class);

    public static List<Route.Builder> compile(Class<?> controller, Registry registry) {
        return compile(controller, registry.provider(Registry.Key.of(controller)), registry);
    }

    public static List<Route.Builder> compile(Class<?> controller, Provider<?> controllerProvider, Registry registry) {
        final String rootPath = path(controller);
        final MediaType rootConsumes  = consumes(controller, null);
        final MediaType rootProduces  = produces(controller, null);
        final Route.Filter rootFilter = filter(controller, registry);
        final Route.After rootAfter   = after(controller, registry);
        final Route.Before rootBefore = before(controller, registry);

        // map all methods and their verbs
        Map<Method, List<Class<? extends Annotation>>> actions = methods(controller);
        ActionParameterExtractor extractor = new ActionParameterExtractor(registry.require(ClassMeta.class), controller);

        LinkedList<Route.Builder> routes = new LinkedList<>();

        actions.forEach((action, verbs) -> {
            String path = mergePaths(rootPath, action);
            if (path == null) return;

            MediaType consumes = consumes(action, rootConsumes);
            MediaType produces = produces(action, rootProduces);
            Route.Filter filter = filter(action, registry);
            Route.After after = after(action, registry);
            Route.Before before = before(action, registry);

            for (Class<? extends Annotation> annotation : verbs) {
                HttpMethod method = HttpMethod.valueOf(annotation.getSimpleName());

                Route.Builder bob = new Route.Builder(method, path, new ActionHandler(action, controllerProvider, extractor));

                if (consumes != null) bob.consumes(consumes);
                if (produces != null) bob.produces(produces);
                
                if (rootBefore != null) bob.before(rootBefore);
                if (before != null) bob.before(before);
                
                if (rootFilter != null) bob.filter(rootFilter);
                if (filter != null) bob.filter(filter);
                
                if (rootAfter != null) bob.after(rootAfter);
                if (after != null) bob.after(after);
                
                processReturnType(bob, action);

                routes.add(bob);
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

    static MediaType consumes(AnnotatedElement elm, MediaType fallback) {
        Consumes annotation = elm.getAnnotation(Consumes.class);

        if (annotation != null) {
            return MediaType.valueOf(annotation.value());
        }

        return fallback;
    }

    static MediaType produces(AnnotatedElement elm, MediaType fallback) {
        Produces annotation = elm.getAnnotation(Produces.class);

        if (annotation != null) {
            return MediaType.valueOf(annotation.value());
        }

        return fallback;
    }
    
    static Route.Filter filter(AnnotatedElement elm, Registry registry) {
        Filter annotation = elm.getAnnotation(Filter.class);
        
        if (annotation != null) {
            Class<? extends net.javapla.jawn.core.Route.Filter> fclass = annotation.value();
            return registry.require(fclass);
        }
        
        return null;
    }
    
    static Route.After after(AnnotatedElement elm, Registry registry) {
        AfterFilter annotation = elm.getAnnotation(AfterFilter.class);
        
        if (annotation != null) {
            Class<? extends net.javapla.jawn.core.Route.After> fclass = annotation.value();
            return registry.require(fclass);
        }
        
        return null;
    }
    
    static Route.Before before(AnnotatedElement elm, Registry registry) {
        BeforeFilter annotation = elm.getAnnotation(BeforeFilter.class);
        
        if (annotation != null) {
            Class<? extends net.javapla.jawn.core.Route.Before> fclass = annotation.value();
            return registry.require(fclass);
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
     */
    static Map<Method, List<Class<? extends Annotation>>> methods(Class<?> controller, Map<Method, List<Class<? extends Annotation>>> actions) {
        if (controller == Object.class) return actions; // stop condition for the recursive call

        Method[] methods = controller.getMethods();
        // .getDeclaredMethods vs .getMethods ->
        // the former returns ONLY declared in this particular class,
        // the latter returns ALL in the hierarchy

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
     * 
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
                // throw new IllegalArgumentException("No path found for: " + method);
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

    static void processReturnType(Route.Builder bob, Method action) {
        bob.returnType(action.getReturnType());
    }
}
