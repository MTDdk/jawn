package net.javapla.jawn.core.renderers;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;
import net.javapla.jawn.core.renderers.template.ViewTemplateLoader;
import net.javapla.jawn.core.renderers.template.ViewTemplates;

public class ViewTemplateLoaderTest {
    
    static ViewTemplateLoader templateLoader;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        DeploymentInfo di = new DeploymentInfo(mock(Config.class), StandardCharsets.UTF_8, "");
        
        @SuppressWarnings("unchecked")
        TemplateRendererEngine<TestTemplate> engine = mock(TemplateRendererEngine.class);
        when(engine.getSuffixOfTemplatingEngine()).thenReturn(".template");
        when(engine.readTemplate(anyString())).thenReturn(new TestTemplate());
        
        
        templateLoader = new ViewTemplateLoader(di);
    }

    @Test
    public void test() {
        ViewTemplates template = templateLoader.load(Results.view(), ".st", false);
        System.out.println(template);
    }

    
    public static class TestTemplate extends HashMap<String,String> {}
    
}
