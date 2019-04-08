package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.mvc.ViewController;

public class ClassLocatorTest {

    private static ClassLocator locator;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        locator = new ClassLocator("test.classlocator");
    }

    @Test
    public void findAllClasses() {
        assertThat(locator.foundClasses()).hasSize(3);
    }
    
    @Test
    public void classBySuffix() {
        Set<Class<?>> classes = locator.foundClassesWithSuffix("Class3");
        assertThat(classes).hasSize(1);
        
        Class<?> class3 = classes.iterator().next();
        assertThat(class3.getSimpleName()).isEqualTo("LocatableClass3");
        assertThat(class3.getPackageName()).isEqualTo(locator.packageToScan);
    }

    @Test
    public void classByAnnotation() {
        Set<Class<?>> classes = locator.withAnnotation(ViewController.class);
        assertThat(classes).hasSize(1);
        
        Class<?> class2 = classes.iterator().next();
        assertThat(class2.getSimpleName()).isEqualTo("LocatableClass2");
        assertThat(class2.getPackageName()).isEqualTo(locator.packageToScan);
    }
}
