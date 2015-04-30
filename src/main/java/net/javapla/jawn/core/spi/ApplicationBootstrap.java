package net.javapla.jawn.core.spi;

import net.javapla.jawn.core.ApplicationConfig;

public interface ApplicationBootstrap {

    void bootstrap(ApplicationConfig config);
    void destroy();
}
