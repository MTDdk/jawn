package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.Test;

public class ActionParameterNameTest {

    @Test
    public void test() throws NoSuchMethodException, SecurityException {
        Method method = ActionParameterNameTest.class.getDeclaredMethod("testMethodFTW", String.class);
        Parameter parameter = method.getParameters()[0];
        
        assertThat(ActionParameterName.name(parameter)).isEqualTo("actionParameterOfAwesome");
    }

    
    // this is what we are looking for
    public void testMethodFTW(final String actionParameterOfAwesome) { }
}
