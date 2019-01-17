package net.javapla.jawn.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;

import app.controllers.MockController;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.Modes;

public class ControllerTest {
    
    private static Controller controller;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JawnConfigurations(Modes.DEV);
        
        Injector injector = mock(Injector.class);
        DeploymentInfo info = new DeploymentInfo(mock(JawnConfigurations.class), "");
        when(injector.getInstance(DeploymentInfo.class)).thenReturn(info);
        
        
        Context context = mock(Context.class);
        when(context.contextPath()).thenReturn(new ServerConfig().contextPath());
        controller = new MockController();
        controller.init(context, injector);
    }
    
    @Before
    public void setUpBefore() throws Exception {
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
    
    @Test
    public void redirect_with_anchor() {
        String anchor = "h1anchor";
        
        controller.redirect(MockController.class, CollectionUtil.map("#",anchor));
        Result result = controller.getControllerResult();
        assertEquals("/mock#"+anchor, result.headers().get("Location"));
    }
    
    @Test
    public void redirect_with_anchorAndParams() {
        controller.redirect(MockController.class, CollectionUtil.map("param1","value1", "#", "anchor","param2","value2"));
        Result result = controller.getControllerResult();
        assertEquals("/mock?param1=value1&param2=value2#anchor", result.headers().get("Location"));
    }
    
    
    /*@Test
    public void redirect_with_contextPath() {
        Injector injector = mock(Injector.class);
        JawnConfigurations configurations = mock(JawnConfigurations.class);
        when(configurations.getSecure(Constants.PROPERTY_DEPLOYMENT_INFO_CONTEXT_PATH)).thenReturn(Optional.of("/certaincontext"));
        DeploymentInfo info = new DeploymentInfo(configurations);
        when(injector.getInstance(DeploymentInfo.class)).thenReturn(info);
        
        Context context = mock(Context.class);
        Controller controller = new MockController();
        controller.init(context, injector);
        
        controller.redirect(MockController.class);
        Result result = controller.getControllerResult();
        
        assertEquals("/certaincontext/mock", result.headers().get("Location"));
    }*/
}
