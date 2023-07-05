package net.javapla.jawn.core.internal.reflection;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.UniqueId;

import net.javapla.jawn.core.AssertionsHelper;

class ClassMetaTest {
    
    static ClassSource classSource;
    static ClassMeta meta;
    
    @BeforeAll
    public static void beforeAll() {
        classSource = new ClassSource(ClassMetaTest.class.getClassLoader());
        meta = new ClassMeta(classSource);
    }
    
    @AfterAll
    public static void afterAll() {
        classSource.close();
    }

    @Test
    void test() throws NoSuchMethodException, SecurityException {
        Method method = Assertions.class.getDeclaredMethod("fail", String.class);
        Parameter parameter = method.getParameters()[0];
        assertEquals("arg0", parameter.getName());
        
        String[] parameterNames = meta.parameterNames(method);
        AssertionsHelper.ass(parameterNames, "message");
    }
    
    @Test
    void exceptiontest() throws NoSuchMethodException, SecurityException {
        Constructor<JUnitException> constructor1 = JUnitException.class.getDeclaredConstructor(String.class);
        Constructor<JUnitException> constructor2 = JUnitException.class.getDeclaredConstructor(String.class, Throwable.class);
        
        assertEquals("arg0", constructor1.getParameters()[0].getName());
        
        assertEquals("arg0", constructor2.getParameters()[0].getName());
        assertEquals("arg1", constructor2.getParameters()[1].getName());
        
        Map<Executable, String[]> execs = meta.extractParameterNames(JUnitException.class);
        
        AssertionsHelper.ass(execs.get(constructor1), "message");
        AssertionsHelper.ass(execs.get(constructor2), "message", "cause");
        
    }
    
    @Test
    void internalClass() {
        Map<Executable, String[]> execs = meta.extractParameterNames(UniqueId.Segment.class);
        System.out.println(execs);
        // didn't throw
    }
    
    /*@Test
    void localClass() {
        Map<Executable, String[]> execs = meta.extractParameterNames(LocalClass.class);
        System.out.println(execs);
    }

    
    static class LocalClass {
        public void action(String one, long two, byte three) {
            
        }
    }*/
}
