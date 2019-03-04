package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;

import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.internal.TestValueFactory;

public class ActionParameterTest {


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
        Parameter param = action("lookItUp", Long.class).getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy");
        
        Value value = Value.of("400");
        
        Context context = mock(Context.class);
        when(context.param("lmgtfy")).thenReturn(value);
        
        Object result = actionParameter.value(context);
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(400);
    }
    
    @Test
    public void convertToListValue() throws NoSuchMethodException, SecurityException {
        Parameter param = action("lookItUp", List.class).getParameters()[0];
        System.out.println(param);
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy");
        System.out.println(actionParameter);
        
        Value value = Value.of("400", "cookie", "value");
        
        Context context = mock(Context.class);
        when(context.param("lmgtfy")).thenReturn(value);
        
        Object result = actionParameter.value(context);
        assertThat(result).isInstanceOf(List.class);
        assertThat((List<String>)result).containsExactly("400","cookie","value");
    }
    
    @Test
    public void convertToRequest() throws NoSuchMethodException, SecurityException {
        Parameter param = action("lookItUp", Context.Request.class).getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "req");
        //TODO
    }
    
    
    
    public void lookItUp(@Named("lmgtfy") String s) {}
    public void lookItUp(@Named("lmgtfy") Optional<String> s) {}
    public void lookItUp(@Named("lmgtfy") Long l) {}
    public void lookItUp(@Named("lmgtfy") List<String> l) {}
    public void lookItUp(@Named("req") Context.Request r) {}

    private static Parameter firstParam(String name) throws NoSuchMethodException, SecurityException {
        return action(name, String.class).getParameters()[0];
    }
    
    private static Method action(final String name, Class<?> ... parameters) throws NoSuchMethodException, SecurityException {
        return ActionParameterTest.class.getDeclaredMethod(name, parameters);
    }
}
