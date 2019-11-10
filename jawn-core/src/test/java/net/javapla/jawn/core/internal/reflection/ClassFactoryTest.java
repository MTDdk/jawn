package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Up;

public class ClassFactoryTest {
    
    static String thisPackage;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        thisPackage = ClassFactoryTest.class.getPackageName();
    }

    @Test
    public void packageName() {
        assertThat(ClassFactory.packageName(ClassFactoryTest.class.getName())).isEqualTo(thisPackage);
    }

    @Test
    public void packageNameWithClass() {
        assertThat(ClassFactory.packageName(ClassFactoryTest.class.getName() + ".class")).isEqualTo(thisPackage);
    }
    
    @Test
    public void createInstance_illegal() {
        assertThrows(Up.UnloadableClass.class, () -> ClassFactory.createInstance(ClassFactory.class)); // ClassFactory is abstract AND has a private constructor
        
        assertThrows(Up.UnloadableClass.class, () -> ClassFactory.createInstance(ClassLocator.class)); // ClassLocator does not have a default constructor
    }
    
    @Test
    public void createInstance_noCache() {
        Object instance = ClassFactory.createInstance("test.classlocator.TestJawnClass.class", Jawn.class, false);
        
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(Jawn.class);
        
        instance = ClassFactory.createInstance("test.classlocator.TestJawnClass", Jawn.class, false);
        assertThat(instance).isInstanceOf(Jawn.class);
    }
    
    @Test
    public void createInstance_withCache() {
        Object instance = ClassFactory.createInstance("test.classlocator.TestJawnClass.class", Jawn.class, true);
        
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(Jawn.class);
        
        instance = ClassFactory.createInstance("test.classlocator.TestJawnClass", Jawn.class, true);
        assertThat(instance).isInstanceOf(Jawn.class);
    }
    
    @Test
    public void createInstance_incorrectType() {
        assertThrows(Up.UnloadableClass.class, () -> ClassFactory.createInstance("test.classlocator.LocatableClass1.class", Jawn.class, false));
        assertThrows(Up.Compilation.class, () -> ClassFactory.createInstance("test.classlocator.NonExisting.class", Jawn.class, false));
        assertThrows(Up.Compilation.class, () -> ClassFactory.createInstance("test.classlocator.NonExisting.class", Jawn.class, true));
    }
}
