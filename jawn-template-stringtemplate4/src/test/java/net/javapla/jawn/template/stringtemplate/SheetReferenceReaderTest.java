package net.javapla.jawn.template.stringtemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Context.Response;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.util.AsyncCharArrayWriter;

class SheetReferenceReaderTest {
    
static StringTemplateTemplateRenderer renderer;
    
    @BeforeAll
    static void setup() {
        renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader(new DeploymentInfo()));
    }

    @Test
    void test() throws Exception {
        
        Context context = mock(Context.class);
        Response response = mock(Context.Response.class);
        when(context.resp()).thenReturn(response);
        
        AsyncCharArrayWriter writer = new AsyncCharArrayWriter();
        when(response.writer()).thenReturn(new PrintWriter(writer));
        
        renderer.render(context, new View("sheets/subpage_with_references"));
        System.out.println(writer.toString());
        //assertEquals("ViewLoadTest", new String(render));
        
    }

}
