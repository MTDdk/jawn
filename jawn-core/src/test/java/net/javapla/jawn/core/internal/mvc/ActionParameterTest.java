package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Context.Request;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.annotation.PathParam;

class ActionParameterTest {

    @Test
    void pathParam() throws NoSuchMethodException, SecurityException {
        Method method = this.getClass().getMethod("action", String.class);
        ActionParameter parameter = new ActionParameter(method.getParameters()[0], "somename");
        
        assertNotNull(parameter.annotation);
    }
    
    @Test
    void noPathParam() throws NoSuchMethodException, SecurityException {
        Method method = this.getClass().getMethod("nopath", String.class);
        ActionParameter parameter = new ActionParameter(method.getParameters()[0], "somename");
        
        assertNull(parameter.annotation);
    }
    
    @Test
    void strategy_convertLong() throws Exception {
        Request request = mock(Context.Request.class);
        when(request.pathParam(anyString())).thenReturn(Value.of(161616));
        
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        
        
        Method method = this.getClass().getMethod("longaction", long.class);
        ActionParameter parameter = new ActionParameter(method.getParameters()[0], "id");
        
        Object result = parameter.apply(context);
        assertTrue(result instanceof Long);
        assertEquals(161616l, result);
    }
    
    /*@Test
    void strategy_handleConversion() throws Exception {
        Method method = this.getClass().getMethod("longaction", long.class);
        ActionParameter parameter = new ActionParameter(method.getParameters()[0], "id");
        System.out.println((parameter.type instanceof Number));
        System.out.println(parameter.type == long.class);
        System.out.println(Number.class.isNestmateOf((Class<?>) parameter.type));
    }*/
    
    /*@Test
    void strategy_handleConversion_nullStrategy() throws Exception {
        Context context = mock(Context.class);
        
        Strategy strategy = ActionParameter.handleConversion(int.class, null);
        System.out.println(strategy.apply(context, mock(ActionParameter.class)));
    }*/

    public void action(@PathParam String cookie) {}
    public void nopath(String cookie) {}
    public void longaction(@PathParam long id) {}
}
