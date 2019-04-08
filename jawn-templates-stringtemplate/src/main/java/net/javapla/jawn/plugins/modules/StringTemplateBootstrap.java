package net.javapla.jawn.plugins.modules;

import com.google.inject.Scopes;

import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.templates.stringtemplate.StringTemplateTemplateEngine;

public class StringTemplateBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(TemplateRendererEngine.class).to(StringTemplateTemplateEngine.class).in(Scopes.SINGLETON);
    }

}
