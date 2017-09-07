package net.javapla.jawn.plugins.modules;


import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import net.javapla.jawn.core.ApplicationConfig;
import net.javapla.jawn.core.api.ApplicationBootstrap;
import net.javapla.jawn.templates.rocker.RockerTemplateEngine;

public class TemplateRockerConfiguration implements ApplicationBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.registerModules(new AbstractModule() {

            @Override
            protected void configure() {
                bind(RockerTemplateEngine.class).in(Singleton.class);
            }
            
        });
    }

    @Override
    public void destroy() { }

}
