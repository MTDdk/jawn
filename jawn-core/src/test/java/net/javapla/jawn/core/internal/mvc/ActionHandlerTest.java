package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.internal.injection.Injector;
import net.javapla.jawn.core.internal.reflection.ClassMeta;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route.Handler;
import net.javapla.jawn.core.Status;

class ActionHandlerTest {

    @Test
    void test() throws Exception {
        Registry registry = new Injector();
        ActionParameterExtractor controllerExtractor = new ActionParameterExtractor(registry.require(ClassMeta.class), Controller.class);
        Context context = mock(Context.class);
        
        Handler indexHandler = new ActionHandler(Controller.class.getDeclaredMethod("index"), Controller.class, registry, controllerExtractor);
        
        assertEquals(0, Controller.indexCalled);
        indexHandler.handle(context);
        assertEquals(1, Controller.indexCalled);
        
        
        Handler statusHandler = new ActionHandler(Controller.class.getDeclaredMethod("status"), Controller.class, registry, controllerExtractor);
        
        assertEquals(0, Controller.statusCalled);
        Object result = statusHandler.handle(context);
        assertEquals(1, Controller.statusCalled);
        assertEquals(1, Controller.indexCalled);
        assertEquals(Status.ACCEPTED, result);
    }
    
    @Test
    void injectedArguments() throws Exception {
        Registry registry = new Injector();
        ActionParameterExtractor controllerExtractor = new ActionParameterExtractor(registry.require(ClassMeta.class), Controller.class);
        Context context = mock(Context.class);
        
        Handler injectableHandler = new ActionHandler(Controller.class.getDeclaredMethod("injectable", Context.class), Controller.class, registry, controllerExtractor);
        Object result = injectableHandler.handle(context);
        System.out.println(result);
    }

/* 
 * ************
 * TEST CLASSES
 * ************
 */
    public static class Controller {
        public static int indexCalled = 0, statusCalled = 0, injectableCalled = 0;
        public Controller() {}
        
        public void index() {
            indexCalled++;
        }
        
        public Status status() {
            statusCalled++;
            return Status.ACCEPTED;
        }
        
        public void injectable(Context ctx) {
            injectableCalled++;
            System.out.println(ctx);
        }
    }
}
