package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static net.javapla.jawn.core.AssertionsHelper.*;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.POST;
import net.javapla.jawn.core.annotation.PUT;
import net.javapla.jawn.core.annotation.Path;

class AnnotationScannerTest {

    @Test
    void paths() throws NoSuchMethodException, SecurityException {
        String path = AnnotationScanner.path(ControllerT1.class);
        assertEquals("/cookie", path);
        
        path = AnnotationScanner.path(ControllerT1.class.getDeclaredMethod("action"));
        assertEquals("/import", path);
        
        assertEquals("/flash", AnnotationScanner.path(ControllerT2.class));
        path = AnnotationScanner.path(ControllerT2.class.getMethod("action"));
        assertEquals("/import", path);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void multipleVerbs() throws NoSuchMethodException, SecurityException {
        Method method = ControllerVerbs.class.getMethod("multiple");
        List<Class<? extends Annotation>> verbs = AnnotationScanner.verbs(method);
        ass(verbs, GET.class, POST.class, PUT.class);
    }
    
    @Test
    void methods() {
        Map<Method,List<Class<? extends Annotation>>> methods = AnnotationScanner.methods(ControllerT1.class);
        assertNotNull(methods);
        assertTrue(methods.size() == 3);
    }
    
    
    @Test
    void scan() {
        List<Builder> builders = AnnotationScanner.scan(ControllerT1.class, null);
        assertEquals(3, builders.size());
        
        List<String> paths = builders.stream().map(bob -> bob.path).collect(Collectors.toList());
        ass(paths, "/cookie/import", "/cookie/outport","/cookie/port");
        
        
        // extends
        builders = AnnotationScanner.scan(ControllerT2.class, null);
        assertEquals(3, builders.size());
        
        paths = builders.stream().map(bob -> bob.path).collect(Collectors.toList());
        ass(paths, "/flash/import", "/flash/outport","/flash/port");
    }
    
    

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
