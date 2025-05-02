package net.javapla.jawn.template.stringtemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.TemplateRenderer;

class ViewLoadTest {

    @Test
    void test() throws Exception {
        StringTemplateTemplateRenderer renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader());
        
        Context context = mock(Context.class);
        
        byte[] render = renderer.render(context, new TemplateRenderer.Template("simple"));
        assertEquals("ViewLoadTest", new String(render));
    }
    
    @Test
    void inject() throws Exception {
        StringTemplateTemplateRenderer renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader());
        
        Context context = mock(Context.class);
        
        byte[] render = renderer.render(context, new TemplateRenderer.Template("simple_inject").put("methodname", "inject()"));
        
        String methodname = Thread.currentThread().getStackTrace()[1].getMethodName(); // this is absolutely not necessary for test - purely for the fun of it
        assertEquals("ViewLoadTest " + methodname + "()", new String(render));
    }

}
