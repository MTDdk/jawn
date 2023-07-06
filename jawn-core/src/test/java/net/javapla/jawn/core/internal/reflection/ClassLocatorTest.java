package net.javapla.jawn.core.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

class ClassLocatorTest {

    @Test
    void simple() {
        List<Class<?>> list = ClassLocator.list("net.javapla.jawn.core.internal.reflection", ClassLoader.getSystemClassLoader());
        
        assertTrue(list.size() > 3);
    }
    
    @Test
    void jar() {
        // asm-9.x.jar
        List<Class<?>> list = ClassLocator.list(ClassReader.class.getPackageName(), Thread.currentThread().getContextClassLoader());
        assertTrue(list.size() > 0);
    }

}
