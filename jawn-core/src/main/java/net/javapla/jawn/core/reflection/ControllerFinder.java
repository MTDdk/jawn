package net.javapla.jawn.core.reflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.javapla.jawn.core.exceptions.MethodNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Might be replaced by https://github.com/ronmamo/reflections
 * 
 * This class is very much not optimised for speed
 * 
 * @author mtd
 */
public final class ControllerFinder {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final static char DOT = '.';
    private final static char SLASH = '/';
    private final static String CLASS_SUFFIX = ".class";
    private final static String CONTROLLER_SUFFIX = "Controller";
    private final static String CONTROLLER_CLASS_SUFFIX = CONTROLLER_SUFFIX + CLASS_SUFFIX;
    private final static String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the given '%s' package exists?";
    
    public final Map<String, Class<?>> controllers;
    
    /**
     * Key:     The name of a controller
     * Value:   Map of methods sorted by arguments
     * 
     *          Key:    Number of arguments for a method
     *          Value:  A map of methods with the given number of arguments.
     *          
     *                  Key: Method name
     *                  Value: Method itself
     */
    public final Map<String, Map< Integer, Map<String,               Method>>> controllerMethods;
    //          controller   args    (httpmethod + methodname)  method
    
    public final Map<String, Set<String>> controllerActions;
    
    public ControllerFinder(String scanPackage) {
        controllers = find(scanPackage);
        
        controllerMethods = new HashMap<String, Map<Integer, Map<String,Method>>>();
        controllerActions = new HashMap<>();
        
        // case: first we find the controller, then we filter the methods on number of arguments, 
        // and lastly we see if there exists a method with the name, we are looking for, prepended with a given httpmethod (GET,POST,..)
        for (Entry<String, Class<?>> controller : controllers.entrySet()) {
            Map<Integer, Map<String,Method>> arguments = new HashMap<>();
            HashSet<String> actions = new HashSet<>();
            
            Method[] methods = controller.getValue().getMethods();//getDeclaredMethods(); 
            // README
            // declaredMethods only takes found methods by the class itself.
            // It does not expose any parent methods, which *could* be a problem
            // when controllers try to inherit from each other
            
            for (Method method : methods) {
                if (!Modifier.isPublic(method.getModifiers()) || !method.getReturnType().equals(Void.TYPE)) continue;
                
                int count = method.getParameterCount();
                
                if (!arguments.containsKey(count))
                    arguments.put( count , new HashMap<>());
                
                //README we might not want the methodname to be lowercase
                arguments.get(count).put(method.getName().toLowerCase(), method);
                
                actions.add(method.getName());
            }
            
            controllerMethods.put( controller.getKey(), arguments);
            controllerActions.put(controller.getKey(), actions);
        }
    }
    
    public boolean controllerExists(String controller) {
        return controllers.containsKey(controller);
    }
    
    public Method getControllerMethod(String controller, int arguments, String methodName) throws MethodNotFoundException {
        Map<Integer, Map<String, Method>> args = controllerMethods.get(controller);
        if (args != null) {
            Map<String, Method> methods = args.get(arguments);
            if (methods != null) {
                Method method = methods.get(methodName);
                if (method == null) throw new MethodNotFoundException(String.format("%s.%s(%d)", controller, methodName, arguments));
                return method;
            }
        }
        throw new MethodNotFoundException(methodName);
    }
    /*public Method getControllerMethod(UrlPath urlpath) {
        return getControllerMethod(urlpath.controller, urlpath.args.size(), urlpath.method);
    }*/

    private final Map<String, Class<?>> find(final String scannedPackage) throws IllegalArgumentException {
        final String scannedPath = scannedPackage.replace(DOT, SLASH);
        
        final Enumeration<URL> resources = retrieveResourcesFromPath(scannedPackage, scannedPath);
        
        final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
        while (resources.hasMoreElements()) {
            final File file = new File(resources.nextElement().getFile());
            if (file != null) {
                if (file.isDirectory())
                    findClassInDir(file, scannedPackage, classes);
                else
                    findClass(file, scannedPackage, "", classes);
            }
        }
        return classes;
    }

    private Enumeration<URL> retrieveResourcesFromPath(final String scannedPackage, final String scannedPath) throws IllegalArgumentException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            File scannedFile = new File(classLoader.getResource(scannedPath).getFile());
            if (scannedFile.isDirectory())
                return classLoader.getResources(scannedPath);
            else  {//we assume the classpath is inside a jar
                try (JarLoader jar = new JarLoader(scannedPath)) {
                    return jar.getResourcesFromJarFile();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage), e);
        }
    }
    
    private final void findClassInDir(final File file, final String scannedPackage, final Map<String, Class<?>> classes) {
        File[] files = file.listFiles();
        if (files != null)
            for (File nestedFile : files) {
                findClass(nestedFile, scannedPackage, "", classes);
            }
    }

    private final void findClass(final File file, final String scannedPackage, final String subpackage, final Map<String, Class<?>> classes) {
        final String resource = scannedPackage + DOT + subpackage + file.getName();
        if (file.isDirectory()) {
            // file is a package
            for (File nestedFile : file.listFiles()) {
                findClass(nestedFile, scannedPackage, subpackage + file.getName()+ DOT , classes);
            }
        } else if (resource.endsWith(CONTROLLER_CLASS_SUFFIX)) {
            final String className = extractClassName(resource);
            final String controllerName = subpackage.replace(DOT, SLASH) + extractControllerName(file);
            
            try {
                classes.put(controllerName, Class.forName(className));
            } catch (ClassNotFoundException ignore) { log.debug("Class not found: {}-{}",controllerName, className); }
        }
    }
    
    private final static String extractClassName(String resource) {
        final int classEndIndex = resource.length() - CLASS_SUFFIX.length();
        return resource.substring(0, classEndIndex);
    }
    
    private final static String extractControllerName(final File file) {
        final int controllerEndIndex = file.getName().length() - CONTROLLER_CLASS_SUFFIX.length();
        return file.getName().substring(0, controllerEndIndex).toLowerCase();
    }
    
}