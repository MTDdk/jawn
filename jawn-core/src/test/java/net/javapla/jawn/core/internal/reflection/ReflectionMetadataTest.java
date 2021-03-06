package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Handler;
import test.classlocator.LocatableClass1;
import test.classlocator.TestJawnClass;

public class ReflectionMetadataTest {
    
    @Test
    public void isAssignableFrom() {
        assertThat(ReflectionMetadata.isAssignableFrom(Jawn.class, Jawn.class)).isTrue();
        assertThat(ReflectionMetadata.isAssignableFrom(Jawn.class, Route.Filtering.class)).isTrue();
        
        assertThat(ReflectionMetadata.isAssignableFrom(TestJawnClass.class, Jawn.class)).isTrue();
        assertThat(ReflectionMetadata.isAssignableFrom(TestJawnClass.class, Route.Filtering.class)).isTrue();
        
        
        assertThat(ReflectionMetadata.isAssignableFrom(Jawn.class, Handler.class)).isFalse();
        assertThat(ReflectionMetadata.isAssignableFrom(LocatableClass1.class, Jawn.class)).isFalse();
    }
    
    @Test
    public void callingClassName() {
        String name = ReflectionMetadata.callingClassName();
        assertThat(name.substring(name.lastIndexOf('.') + 1)).isEqualTo("ReflectionMetadataTest");
    }

    @Test
    public void callingClass() {
        assertThat(T.call().getName()).isEqualTo(T.class.getName());
    }
    
    private static class T extends Jawn {
        public static Class<?> call() {
            return ReflectionMetadata.callingClass(Jawn.class);
        }
    }
}
