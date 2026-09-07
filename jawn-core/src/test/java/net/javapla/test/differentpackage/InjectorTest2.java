package net.javapla.test.differentpackage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Registry.Key;
import net.javapla.jawn.core.internal.injection.Injector;

class InjectorTest2 {

    @Test
    void packagePrivateClass() {
        Injector injector = new Injector();
        PackagePrivateClass c = injector.require(Key.of(PackagePrivateClass.class));
        assertEquals("test_PackagePrivateClass", c.returnSomething());
    }

}
