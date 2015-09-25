package net.javapla.jawn.core.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.WebException;
import net.javapla.jawn.core.http.Context;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ControllerActionInvoker {

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
    
    public Response executeAction(Context context) {
        Route route = context.getRoute();

        try {
            Controller controller = loadController(route.getController());

            injectControllerWithContext(controller, context, injector);

            //find the method name and run it
            String methodName = route.getAction();
            for (Method method : controller.getClass().getMethods()) {
                //if (method.getReturnType().equals(Response.class)) 
                if (methodName.equals( method.getName())) {
                    
                    // handle if the return type is 'void'
                    //if (method.getReturnType().equals(Void.TYPE))
                    //if (response == null) throw 404Exception();
                    
                    method.invoke(controller);//route.getController());
                    return controller.getControllerResponse();
                }
            }
            throw new ControllerException(String.format("Action name (%s) not found in controller (%s)", route.getAction(), route.getController().getSimpleName()));

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
    
    
    private Controller loadController(Class<? extends Controller> controller) throws ClassLoadException {
        try {
            return DynamicClassFactory.createInstance(controller);
        } catch (ClassLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }

    private void injectControllerWithContext(Controller controller, Context context, Injector injector) {
        controller.init(context, injector);
        
        //Inject dependencies
        if (injector != null) {
            injector.injectMembers(controller);
        }
    }
    
    public static boolean isAllowedAction(Class<? extends Controller> controller, String actionMethodName) {
        for (Method method : controller.getMethods()) {
            if (actionMethodName.equals(method.getName())) {
                return true;
            }
        }
        return false;
    }
}
