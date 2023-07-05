package net.javapla.jawn.core.internal.mvc;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

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

    public void action(@PathParam String cookie) {}
    public void nopath(String cookie) {}
}
