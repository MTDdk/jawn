package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.annotation.PathParam;
import net.javapla.jawn.core.internal.reflection.ClassMeta;
import net.javapla.jawn.core.internal.reflection.ClassSource;

class ActionParameterExtractorTest {
    ClassMeta meta = new ClassMeta(new ClassSource());

    @Test
    void context() throws NoSuchMethodException, SecurityException {
        
        ActionParameterExtractor extractor = new ActionParameterExtractor(meta, Controller.class);
        ActionParameter[] parameters = extractor.parameters(Controller.class.getMethod("injectable", Context.class));
        
        assertEquals("ctx", parameters[0].name);
    }
    
    @Test
    void request() throws NoSuchMethodException, SecurityException {
        
        ActionParameterExtractor extractor = new ActionParameterExtractor(meta, Controller.class);
        ActionParameter[] parameters = extractor.parameters(Controller.class.getMethod("injectable", Context.Request.class));
        
        assertEquals("req", parameters[0].name);
    }
    
    @Test
    void pathParam_noValue() throws NoSuchMethodException, SecurityException {
        ActionParameterExtractor extractor = new ActionParameterExtractor(meta, Controller.class);
        ActionParameter[] parameters = extractor.parameters(Controller.class.getMethod("injectable", String.class));
        
        assertEquals("anno", parameters[0].name);
    }
    
    @Test
    void pathParam() throws NoSuchMethodException, SecurityException {
        ActionParameterExtractor extractor = new ActionParameterExtractor(meta, Controller.class);
        ActionParameter[] parameters = extractor.parameters(Controller.class.getMethod("pathparam", String.class));
        
        assertEquals("somethingelse", parameters[0].name);
    }

    
/* 
 * ************
 * TEST CLASSES
 * ************
 */
    public static class Controller {
        public void injectable(Context ctx) {
            
        }
        
        public void injectable(Context.Request req) {
            
        }
        
        // should take the same name as the parameter name
        public void injectable(@PathParam String anno) {
            System.out.println(anno);
        }
        
        public void pathparam(@PathParam("somethingelse") String anno) {
            System.out.println(anno);
        }
    }
}
