package net.javapla.jawn.template.stringtemplate;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.TemplateRenderer;

public class StringTemplateModule implements Plugin {
    
    @Override
    public void install(Application config) {
        
        //config.registry().require(DeploymentInfo.class)
        DeploymentInfo info = new DeploymentInfo();
        config.registry().register(info);
        
        StringTemplateTemplateRenderer renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader(info));
        
        config.renderer(MediaType.HTML, renderer);
        
        config.registry().register(TemplateRenderer.class, renderer);
    }

}
