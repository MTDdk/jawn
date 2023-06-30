package net.javapla.jawn.core.internal.injection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.annotation.Inject;
import net.javapla.jawn.core.annotation.Named;
import net.javapla.jawn.core.annotation.Singleton;

class InjectorTest {

    Injector injector;
    
    @BeforeEach
    void init() {
        injector = new Injector();
    }
    

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
    void multipleNamedInjectables() {
        injector.register(Registry.Key.of(String.class, "appid"), "cookie");
        injector.register(Registry.Key.of(String.class, "secret"), "monster");
        
        LoginBigger login = injector.require(LoginBigger.class);
        assertEquals("cookie", login.appId);
        assertEquals("monster", login.secret);
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
    static class LoginBigger {
        final String appId, secret;
        @Inject
        public LoginBigger(@Named("appid") String appId, @Named("secret") String secret) { this.appId = appId; this.secret = secret; }
    }
    
    static class MissingInjectAnnotation {
        MissingInjectAnnotation(TestClass c) {}
    }
}

