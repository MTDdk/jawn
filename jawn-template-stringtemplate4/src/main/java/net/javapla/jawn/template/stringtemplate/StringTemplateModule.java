package net.javapla.jawn.template.stringtemplate;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.TemplateRenderer;

public class StringTemplateModule implements Plugin {
    
    @Override
    public void install(Application config) {
        
        StringTemplateTemplateRenderer renderer = new StringTemplateTemplateRenderer(new ViewTemplateLoader());
        
        config.renderer(MediaType.HTML, renderer);
        
        config.registry().register(TemplateRenderer.class, renderer);
    }

}
