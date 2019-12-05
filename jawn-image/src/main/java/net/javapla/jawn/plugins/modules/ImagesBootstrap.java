package net.javapla.jawn.plugins.modules;

import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.image.ImageRendererEngine;
import net.javapla.jawn.image.Images;

public class ImagesBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(RendererEngine.class).to(ImageRendererEngine.class);
        config.binder().bind(Images.class);
    }

}
