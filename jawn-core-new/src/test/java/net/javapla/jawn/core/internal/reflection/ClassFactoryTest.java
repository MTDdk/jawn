package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

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
}
