package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.internal.reflection.ClassMeta;

public class ActionParameterProviderTest {

    private static ActionParameterProvider provider;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        provider = new ActionParameterProvider(new ClassMeta());
    }

    @Test
    public void unnamed() throws NoSuchMethodException, SecurityException {
        assertThat(provider.name( ActionParameterNameTest.firstParam("testMethod") )).isEqualTo("actionParameter");
    }
    
    @Test
    public void unnamedMultiple() throws NoSuchMethodException, SecurityException {
        Method action = action("testMethod", String.class, Context.class, Integer.class);
        Parameter[] parameters = action.getParameters();
        
        assertThat(provider.name( parameters[0] )).isEqualTo("actionParameter");
        assertThat(provider.name( parameters[1] )).isEqualTo("actionContext");
        assertThat(provider.name( parameters[2] )).isEqualTo("actionValue");
    }

    
    public void testMethod(final String actionParameter, final Context actionContext, final Integer actionValue) {}
    
    
    private static Method action(final String name, Class<?> ... parameters) throws NoSuchMethodException, SecurityException {
        return ActionParameterProviderTest.class.getDeclaredMethod(name, parameters);
    }
}
