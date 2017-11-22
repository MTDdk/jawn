package net.javapla.jawn.plugins.modules;

import com.google.inject.AbstractModule;

import net.javapla.jawn.core.ApplicationConfig;
import net.javapla.jawn.core.api.ApplicationBootstrap;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.templates.stringtemplate.StringTemplateTemplateEngine;

public class StringTemplateConfiguration implements ApplicationBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.registerModules(new AbstractModule() {
            
            @Override
            protected void configure() {
                bind(TemplateEngine.class).to(StringTemplateTemplateEngine.class);
            }
        });
    }

    @Override
    public void destroy() {
    }

}
