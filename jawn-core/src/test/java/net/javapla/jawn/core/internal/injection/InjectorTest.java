package net.javapla.jawn.core.internal.injection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.javapla.jawn.core.Registry;

class InjectorTest {

    @BeforeEach
    void init() {
        injector = new Injector();
    }
    Injector injector;
    

    @Test
    void instantiateIfNotFound() {
        assertNotNull(injector.require(TestClass.class));
    }
    
    @Test
    void complySingleton() {
        assertEquals(0, SingletonClass.instantiations);
        assertNotNull(injector.require(SingletonClass.class));
        assertEquals(1, SingletonClass.instantiations);
        
        SingletonClass s2 = injector.require(SingletonClass.class);
        assertEquals(1, s2.instance);
        
        SingletonClass s3 = injector.require(SingletonClass.class);
        assertEquals(1, s3.instance);
        
        assertEquals(s2, s3);
    }
    
    @Test
    void injectConstructor() {
        NoDefaultConstructor actual = injector.require(NoDefaultConstructor.class);
        assertNotNull(actual);
        assertNotNull(actual.clzz);
    }
    
    @Test
    void named() {
        injector.register(Registry.Key.of(String.class, "password"), "somegibberish1234");
        
        assertNotNull(injector.require(String.class, "password"));
        assertTrue(injector.require(String.class, "password").endsWith("1234"));
    }

    @Test
    void namedInjectables() {
        injector.register(Registry.Key.of(String.class, "password"), "somegibberish1234");
        
        Login login = injector.require(Login.class);
        assertTrue(login.password.contains("gibberish"));
    }
    
    @Test
    void fail_when_missingSuitableConstructor() {
        assertThrows(Registry.ProvisionException.class, () -> injector.require(MissingInjectAnnotation.class));
    }
    


    
/* 
 * ************
 * TEST CLASSES
 * ************
 */
    static class TestClass {}
    static class NoDefaultConstructor {
        final TestClass clzz;
        @Inject
        NoDefaultConstructor(TestClass clzz) { this.clzz = clzz; }
    }

    @Singleton
    static class SingletonClass {
        static int instantiations = 0;
        final int instance;
        public SingletonClass() { instance = ++instantiations; }
    }
    
    static class Login {
        final String password;
        @Inject
        public Login(@Named("password") String password) { this.password = password; }
    }
    
    static class MissingInjectAnnotation {
        MissingInjectAnnotation(TestClass c) {}
    }
}
