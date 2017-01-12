package net.javapla.jawn.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;

import app.controllers.MockController;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.Modes;

public class ControllerTest {
    
    private static Controller controller;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JawnConfigurations(Modes.DEV);
        
        Context context = mock(Context.class);
        when(context.contextPath()).thenReturn("");
        controller = new MockController();
        controller.init(context, null);
    }

    @Test
    public void redirect302_with_action() {
        String action = "test";
        controller.redirect(MockController.class, action);
        
        Result result = controller.getControllerResult();
        assertEquals(302, result.status());
        assertEquals("/mock/"+ action, result.headers().get("Location"));
    }
    
    @Test
    public void redirect302_with_params() {
        controller.redirect(MockController.class, CollectionUtil.map("param1","value1","param2","value2"));
        Result result = controller.getControllerResult();
        assertEquals(302, result.status());
        assertEquals("/mock?param1=value1&param2=value2", result.headers().get("Location"));
    }
    
    @Test
    public void redirect302_with_encodedParams() {
        controller.redirect(MockController.class, CollectionUtil.map("param1","value1","param2","value2","param3","{ }"));
        Result result = controller.getControllerResult();
        assertEquals(302, result.status());
        assertEquals("/mock?param1=value1&param2=value2&param3=%7B+%7D", result.headers().get("Location"));
    }
    
}
