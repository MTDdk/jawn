package net.javapla.jawn.template.stringtemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.SessionStore;
import net.javapla.jawn.core.TemplateRenderer;
import net.javapla.jawn.core.internal.Bootstrapper;

class InstallModuleTest {

    @Test
    void test() {
        Bootstrapper bootstrapper = new Bootstrapper();
        bootstrapper.boot(reg -> Stream.empty(), SessionStore.EMPTY);
        
        TemplateRenderer renderer = bootstrapper.registry().require(TemplateRenderer.class);
        
        assertNotNull(renderer);
        assertTrue(renderer instanceof StringTemplateTemplateRenderer);
    }
    
    /*@Test
    void install() {
        Jawn jawn = new Jawn();
        
        TemplateRenderer renderer = jawn.require(TemplateRenderer.class);
        
        assertNotNull(renderer);
        assertTrue(renderer instanceof StringTemplateTemplateRenderer);
    }*/

    /*@Test
    void fullJawn() {
        
    }*/
}
