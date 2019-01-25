package net.javapla.jawn.core.internal.template.stringtemplate;

import com.google.inject.Scopes;

import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;

public class StringTemplateBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(TemplateRendererEngine.class).to(StringTemplateTemplateEngine.class).in(Scopes.SINGLETON);
    }

}
