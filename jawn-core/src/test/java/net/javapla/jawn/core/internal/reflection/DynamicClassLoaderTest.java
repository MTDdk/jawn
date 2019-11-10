package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import net.javapla.jawn.core.Up.Compilation;
import test.classlocator.LocatableClass1;
import test.classlocator.LocatableClass2;
import test.classlocator.LocatableClass3;

public class DynamicClassLoaderTest {

    @Test
    public void loadClass() throws Compilation, ClassNotFoundException {
        DynamicClassLoader loader = new DynamicClassLoader("test.classlocator");
        Class<?> c1 = loader.loadClass("test.classlocator.LocatableClass1");
        Class<?> c2 = loader.loadClass("test.classlocator.LocatableClass2");
        Class<?> c3 = loader.loadClass("test.classlocator.LocatableClass3");
        
        assertThat(c1.getName()).isEqualTo(LocatableClass1.class.getName());
        assertThat(c2.getName()).isEqualTo(LocatableClass2.class.getName());
        assertThat(c3.getName()).isEqualTo(LocatableClass3.class.getName());
    }
    
    @Test
    public void loadClass_withEnding() throws Compilation, ClassNotFoundException {
        DynamicClassLoader loader = new DynamicClassLoader("test.classlocator");
        Class<?> c1 = loader.loadClass("test.classlocator.LocatableClass1.class");
        
        assertThat(c1.getName()).isEqualTo(LocatableClass1.class.getName());
    }

}
