package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static net.javapla.jawn.core.AssertionsHelper.*;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.annotation.Filter;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.POST;
import net.javapla.jawn.core.annotation.PUT;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.internal.injection.Injector;

class MvcCompilerTest {

    @Test
    void paths() throws NoSuchMethodException, SecurityException {
        String[] path = MvcCompiler.paths(ControllerT1.class);
        ass(path, "/cookie");
        
        path = MvcCompiler.paths(ControllerT1.class.getDeclaredMethod("action"));
        ass(path, "/import");
        
        ass(MvcCompiler.paths(ControllerT2.class), "/flash");
        
        path = MvcCompiler.paths(ControllerT2.class.getMethod("action"));
        ass(path, "/import");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void multipleVerbs() throws NoSuchMethodException, SecurityException {
        Method method = ControllerVerbs.class.getMethod("multiple");
        List<Class<? extends Annotation>> verbs = MvcCompiler.verbs(method);
        ass(verbs, GET.class, POST.class, PUT.class);
    }
    
    @Test
    void methods() {
        Map<Method,List<Class<? extends Annotation>>> methods = MvcCompiler.methods(ControllerT1.class);
        assertNotNull(methods);
        assertTrue(methods.size() == 3);
    }
    
    @Test
    void compile() {
        Registry registry = new Injector();
        List<Builder> builders = MvcCompiler.compile(ControllerT1.class, registry);
        assertEquals(3, builders.size());
        ass(builders, bob -> bob.path, "/cookie/import", "/cookie/port","/cookie/outport");
        
        
        // extends
        builders = MvcCompiler.compile(ControllerT2.class, registry);
        assertEquals(3, builders.size());
        ass(builders, bob -> bob.path, "/flash/import", "/flash/port","/flash/outport");
    }
    
    @Test
    void multiplePaths() {
        Registry registry = new Injector();
        List<Builder> builders = MvcCompiler.compile(MultiplePathsController.class, registry);
        assertEquals(4, builders.size());
        
        ass(builders, bob -> bob.path, "/multiple/first", "/multiple/second","/multiple/third","/multiple/fourth");
    }
    
    @Test
    void filter_annotation() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Registry registry = new Injector();
        List<Builder> builders = MvcCompiler.compile(ControllerFilters.class, registry);
        assertEquals(1, builders.size());

        // read the private field
        Field field = Route.Builder.class.getDeclaredField("post");
        field.setAccessible(true);
        
        Object setFilter = field.get(builders.get(0));
        assertNotNull(setFilter);
        assertTrue(setFilter.getClass().equals(F1.class));
    }
    
    
/* 
 * ************
 * TEST CLASSES
 * ************
 */
    
    @Path("/cookie")
    private static class ControllerT1 {
        
        @Path("/import")
        public void action() { }
        
        @Path("/outport")
        public void action2() { }
        
        @Path("/port")
        public void action3() { }
    }
    
    @Path("flash")
    private static class ControllerT2 extends ControllerT1 {
        
    }
    
    @Path("/verbs")
    private static class ControllerVerbs {
        @GET
        @PUT
        @POST
        public void multiple() { }
    }
    
    @Path("/multiple")
    private static class MultiplePathsController {
        @Path("/first")
        @Path("/second")
        public void multiple() { }
        
        @Path("/third")
        @Path("/fourth")
        public void multiple2() { }
    }
    
    @Filter(F1.class)
    private static class ControllerFilters {
        @Path("/index")
        public void index() {}
    }
    
/* 
 * ************
 * TEST FILTERS
 * ************
 */
    
    public static class F1 extends Route.Filter {
        @Override
        public void after(Context ctx, Object result, Throwable cause) {
            System.out.println(result);
        }
    }
    
}
