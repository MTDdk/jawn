package net.javapla.jawn.plugins.modules;


import com.google.inject.Singleton;

import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.templates.rocker.RockerTemplateEngine;

public class TemplateRockerConfiguration implements ModuleBootstrap {
    
    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(RockerTemplateEngine.class).in(Singleton.class);
    }

}
