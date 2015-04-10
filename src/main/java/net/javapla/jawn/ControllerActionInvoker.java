package net.javapla.jawn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.inject.Inject;
import com.google.inject.Injector;

import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.WebException;

class ControllerActionInvoker {

//    private AppController controller;
    private boolean useCachedController;
    private Injector injector;
    
    @Inject
    public ControllerActionInvoker(/*Class<? extends AppController> controller, boolean useCachedController*/Injector injector, PropertiesImpl properties) {
        
            //README this might be necessary if the overhead of creating controllers on runtime exceeds the memory of holding the controller on the Route
            // if we want to reload the controller, this is a good time to do it
//            if (!useCachedController) {
//                controller = loadController(route.getController().getClass().getName(), useCachedController);
//            } else {
//                controller = route.getController();
//            }
            
        this.injector = injector;
        this.useCachedController = properties.isProd();
            
    }
    
    public ControllerResponse executeAction(/*Injector injector, */Context context) {
        Route route = context.getRoute();

        try{
            AppController controller = loadController(route.getController().getName(), useCachedController);
        
            injectControllerWithContext(controller, context, injector);
        
            
            //find the method name and run it
            String methodName = route.getAction().toLowerCase();
            for (Method method : controller.getClass()/*route.getController()*/.getMethods()) {
                if (methodName.equals( method.getName().toLowerCase() )) {
                    method.invoke(controller);//route.getController());
                    return controller.getControllerResponse();
                }
            }
            throw new ControllerException(String.format("Action name (%s) not found in controller (%s)", route.getAction(), route.getController().getSimpleName()));
            
//            Method m = controller.getClass().getMethod(actionName);
//            m.invoke(controller);
        }catch(InvocationTargetException e){
            if(e.getCause() != null && e.getCause() instanceof  WebException){
                throw (WebException)e.getCause();                
            }else if(e.getCause() != null && e.getCause() instanceof RuntimeException){
                throw (RuntimeException)e.getCause();
//            }else if(e.getCause() != null){
            }
                throw new ControllerException(e.getCause());
        }catch(WebException e){
            throw e;
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }
    
    
    protected AppController loadController(String controllerClassName, boolean useCachedController) throws ClassLoadException {
        try {
            return  DynamicClassFactory.createInstance(controllerClassName, AppController.class, useCachedController);
        } catch (ClassLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    private void injectControllerWithContext(HttpSupport controller, Context context, Injector injector) {
        controller.init(context, injector);
        
        //Inject dependencies
        if (injector != null) {
            injector.injectMembers(controller);
        }
    }
    
    public static boolean isAllowedAction(Class<? extends AppController> controller, String actionMethodName) {
        String lowerCaseActionName = actionMethodName.toLowerCase();
        for (Method method : controller.getMethods()) {
            if (lowerCaseActionName.equals( method.getName().toLowerCase() )) {
                return true;
            }
        }
        //README: it might not actually be necessary to do this lowercasing
        return false;
    }
}
