package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static net.javapla.jawn.core.AssertionsHelper.*;

import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.POST;
import net.javapla.jawn.core.annotation.PUT;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.internal.injection.Injector;

class MvcCompilerTest {

    @Test
    void paths() throws NoSuchMethodException, SecurityException {
        String path = MvcCompiler.path(ControllerT1.class);
        assertEquals("/cookie", path);
        
        path = MvcCompiler.path(ControllerT1.class.getDeclaredMethod("action"));
        assertEquals("/import", path);
        
        assertEquals("/flash", MvcCompiler.path(ControllerT2.class));
        path = MvcCompiler.path(ControllerT2.class.getMethod("action"));
        assertEquals("/import", path);
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
        
        List<String> paths = builders.stream().map(bob -> bob.path).collect(Collectors.toList());
        ass(paths, "/cookie/import", "/cookie/port","/cookie/outport");
        
        
        // extends
        builders = MvcCompiler.compile(ControllerT2.class, registry);
        assertEquals(3, builders.size());
        
        paths = builders.stream().map(bob -> bob.path).collect(Collectors.toList());
        ass(paths, "/flash/import", "/flash/port","/flash/outport");
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
    
}
