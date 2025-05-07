package net.javapla.jawn.template.stringtemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.TemplateRenderer;
import net.javapla.jawn.core.View;

class ViewLoadTest {
    
    static StringTemplateTemplateRenderer renderer;
    
    @BeforeAll
    static void setup() {
        renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader(new DeploymentInfo()));
    }
    
    @Test
    void test() throws Exception {
        Context context = mock(Context.class);
        
        byte[] render = renderer.render(context, new View("simple"));
        assertEquals("ViewLoadTest", new String(render));
    }
    
    @Test
    void inject() throws Exception {
        Context context = mock(Context.class);
        
        byte[] render = renderer.render(context, new View("simple_inject").put("methodname", "inject()"));
        
        String methodname = Thread.currentThread().getStackTrace()[1].getMethodName(); // this is absolutely not necessary for test - purely for the fun of it
        assertEquals("ViewLoadTest " + methodname + "()", new String(render));
    }
    
    @Test
    void evaluateTemplateFromVariable() throws Exception {
        
        byte[] render = renderer.render(null, new View("simple_template_eval")
            .put("templatename", "simple_inject")
            .put("methodname", "evaluateTemplateFromVariable()"));
        assertEquals("ViewLoadTest ViewLoadTest evaluateTemplateFromVariable()", new String(render));
    }

    @Test
    void differentViewsLocation() throws Exception {
        System.setProperty(TemplateRenderer.ENV_RESOURCES_PATH_LOCATION, "src/test/resources/different_views_location");
        System.setProperty(TemplateRenderer.ENV_TEMPLATE_PATH_NAME, TemplateRenderer.DEFAULT_TEMPLATE_NAME);
        
        StringTemplateTemplateRenderer renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader(new DeploymentInfo()));
        
        byte[] render = renderer.render(null, new View("index").put("somevalue", "simple string output"));
        
        assertEquals("testing injectables simple string output", new String(render));
    }
    
    @Test
    void differentViewsFolderName() throws Exception {
        System.setProperty(TemplateRenderer.ENV_RESOURCES_PATH_LOCATION, "src/test/resources/different_views_location");
        System.setProperty(TemplateRenderer.ENV_TEMPLATE_PATH_NAME, "notviews");
        
        StringTemplateTemplateRenderer renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader(new DeploymentInfo()));
        
        byte[] render = renderer.render(null, new View("index.html").put("somevalue", "inject()"));
        
        assertTrue(new String(render).replaceAll("\n", "").endsWith("</body></html>"));
    }
    
    @Test
    void simpleObjectAsViewModel() throws Exception {
        record Something(int number, String string) {};
        
        byte[] render = renderer.render(null, new View("objects.html").put("object", new Something(77, "stringstring")));
        
        assertEquals(
            """
            77
            stringstring
            Something[number=77, string=stringstring]""", new String(render));
    }
    
    @Test
    void findLayoutInAnotherFolder() throws Exception {
        byte[] render = renderer.render(null, new View("routing.html"));
        assertEquals("information", new String(render));
    }
    
    @Test
    void findTemplateInAnotherFolder() throws Exception {
        byte[] render = renderer.render(null, new View("routing/index.html"));
        assertEquals("routing layout", new String(render));
    }
}
