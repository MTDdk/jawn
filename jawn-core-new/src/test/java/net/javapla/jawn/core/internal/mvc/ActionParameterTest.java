package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import javax.inject.Named;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Context;

public class ActionParameterTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void test() throws NoSuchMethodException, SecurityException {
        Parameter param = firstParam("lookItUp");
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy");
        
        assertThat(actionParameter.optional).isFalse();
        assertThat(actionParameter.type).isEqualTo(String.class);
    }
    
    @Test
    public void optionalParameter() throws NoSuchMethodException, SecurityException {
        Parameter param = action("lookItUp", Optional.class).getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy");
        
        assertThat(actionParameter.optional).isTrue();
        assertThat(actionParameter.type).isEqualTo(com.google.inject.util.Types.newParameterizedType(Optional.class, String.class));
    }
    
    @Test
    public void convertValue() throws NoSuchMethodException, SecurityException {
        Parameter param = firstParam("lookItUp");
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy");
        
        Context context = mock(Context.class);
        
        Object value = actionParameter.value(context);
    }
    
    public void lookItUp(@Named("lmgtfy") String s) {}
    public void lookItUp(@Named("lmgtfy") Optional<String> s) {}

    private static Parameter firstParam(String name) throws NoSuchMethodException, SecurityException {
        return action(name, String.class).getParameters()[0];
    }
    
    private static Method action(final String name, Class<?> ... parameters) throws NoSuchMethodException, SecurityException {
        return ActionParameterTest.class.getDeclaredMethod(name, parameters);
    }
}
