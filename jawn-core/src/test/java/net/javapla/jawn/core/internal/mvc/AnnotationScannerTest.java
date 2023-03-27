package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.AssertionsHelper;
import net.javapla.jawn.core.annotation.Path;

class AnnotationScannerTest {

    @Test
    void paths() throws NoSuchMethodException, SecurityException {
        String path = AnnotationScanner.path(ControllerT1.class);
        assertEquals("/cookie", path);
        
        path = AnnotationScanner.path(ControllerT1.class.getDeclaredMethod("action"));
        assertEquals("/import", path);
        
        assertEquals("/flash", AnnotationScanner.path(ControllerT2.class));
    }
    
    

    @Path("/cookie")
    private static class ControllerT1 {
        
        @Path("/import")
        public void action() {
            
        }
    }
    
    @Path("flash")
    private static class ControllerT2 extends ControllerT1 {
        
    }
    
}
