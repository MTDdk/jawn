package net.javapla.jawn.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.WebException;

import com.google.inject.Inject;
import com.google.inject.Injector;

class ControllerActionInvoker {

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
    }
    
    public ControllerResponse executeAction(Context context) {
        Route route = context.getRoute();

        try {
            long time = System.currentTimeMillis();
            AppController controller = loadController(route.getController());
            System.out.println("ControllerActionInvoker loadController() " + (System.currentTimeMillis() - time));

            time = System.currentTimeMillis();
            injectControllerWithContext(controller, context, injector);
            System.out.println("ControllerActionInvoker injectControllerWithContext() " + (System.currentTimeMillis() - time));

            //find the method name and run it
            time = System.currentTimeMillis();
            String methodName = route.getAction().toLowerCase();
            for (Method method : controller.getClass().getMethods()) {
                if (methodName.equals( method.getName().toLowerCase() )) {
                    method.invoke(controller);//route.getController());
                    System.out.println("ControllerActionInvoker getControllerResponse() " + (System.currentTimeMillis() - time));
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
    
    
    private AppController loadController(Class<? extends AppController> controller) throws ClassLoadException {
        try {
            return DynamicClassFactory.createInstance(controller);
        } catch (ClassLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    private void injectControllerWithContext(AppController controller, Context context, Injector injector) {
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
