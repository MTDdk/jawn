package net.javapla.jawn.core.internal.image;

import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;

public class ImagesBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(RendererEngine.class).to(ImageRendererEngine.class);
        config.binder().bind(Images.class);
    }

}
