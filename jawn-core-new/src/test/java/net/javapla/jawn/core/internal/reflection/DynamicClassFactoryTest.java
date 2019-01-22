package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

public class DynamicClassFactoryTest {
    
    static String thisPackage;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        thisPackage = DynamicClassFactoryTest.class.getPackageName();
    }

    @Test
    public void packageName() {
        assertThat(DynamicClassFactory.packageName(DynamicClassFactoryTest.class.getName())).isEqualTo(thisPackage);
    }

    @Test
    public void packageNameWithClass() {
        assertThat(DynamicClassFactory.packageName(DynamicClassFactoryTest.class.getName() + ".class")).isEqualTo(thisPackage);
    }
}
