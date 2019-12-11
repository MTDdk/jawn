package net.javapla.jawn.plugins.modules;

import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.image.Images;

public class ImagesBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(Images.class);
    }

}
