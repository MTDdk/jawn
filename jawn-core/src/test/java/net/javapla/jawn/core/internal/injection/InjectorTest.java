package net.javapla.jawn.core.internal.injection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InjectorTest {

    @Test
    void instantiateIfNotFound() {
        Injector injector = new Injector();
        assertNotNull(injector.require(TestClass.class));
    }
    
    public static class TestClass {}

}
