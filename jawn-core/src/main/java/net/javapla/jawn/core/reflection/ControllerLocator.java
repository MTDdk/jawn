package net.javapla.jawn.core.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.routes.RouterHelper;
import net.javapla.jawn.core.util.Constants;

public class ControllerLocator extends ClassLocator {
    
    public final static String CONTROLLER_SUFFIX = "Controller";
    public final static String[] httpMethods;
    static {
        String[] listHttpMethods = HttpMethod.listHttpMethods();
        httpMethods = Arrays.copyOf(listHttpMethods, listHttpMethods.length + 1);
        httpMethods[listHttpMethods.length] = Constants.DEFAULT_ACTION_NAME;
    }

    //        controllerPath, actions
    final Map<String, Set<String>> controllerActions;
    final Map<String, Class<? extends Controller>> controllers; // name -> controllers
    
    public ControllerLocator(String packageToScan) throws IllegalArgumentException {
        super(packageToScan);
        
        // build the structures
        controllerActions = new HashMap<>();
        controllers = new HashMap<>();
        
        // Controllers need to be extending Controller and have its name ending with 'Controller'
        Set<Class<? extends Controller>> foundControllers = extractClassesWithSuffix(subtypeOf(Controller.class), CONTROLLER_SUFFIX);
        //Set<Class<?>> foundControllers = foundClassesWithSuffix(CONTROLLER_SUFFIX);
        
        // case: first we find the controller, then we filter the methods on number of arguments, 
        // and lastly we see if there exists a method with the name, we are looking for, prepended with a given httpmethod (GET,POST,..)
        for (Class<? extends Controller> cls : foundControllers) {
            // README
            // Class#getDeclaredMethods only takes found methods by the class itself.
            // It does not expose any parent methods, which *could* be a problem
            // when controllers try to inherit from each other
            controllerActions.put( RouterHelper.getReverseRouteFast(cls)/*extractControllerPath(cls)*/, listActionMethods(cls.getMethods()));
            
            controllers.put( RouterHelper.getReverseRouteFast(cls)/*extractControllerPath(cls)*/, cls);
        }
    }

    private HashSet<String> listActionMethods(final Method[] methods) {
        HashSet<String> actions = new HashSet<>();
        
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers()) || !method.getReturnType().equals(Void.TYPE)) continue;
            
            if (startsWithHttpMethod(method))
                actions.add(method.getName());
        }
        
        return actions;
    }
    
    private boolean startsWithHttpMethod(Method method) {
        for (String httpmethod : httpMethods) {
            if (method.getName().startsWith(httpmethod)) return true;
        }
        return false;
    }
    
    private String extractControllerPath(Class<?> cls) {
        String controllerPath = cls.getName().substring(packageToScan.length() + 1, cls.getName().length() - CONTROLLER_SUFFIX.length());
        controllerPath = controllerPath.replace(DOT, SLASH).toLowerCase();
        return controllerPath;
    }
    
    public boolean containsControllerPath(String controller) {
        return controllerActions.containsKey(controller);
    }
    
    @SuppressWarnings("unchecked")
    public Class<? extends Controller>[] controllersAsArray() {
        return controllers.values().toArray(new Class[controllers.size()]);
    }
    
}
