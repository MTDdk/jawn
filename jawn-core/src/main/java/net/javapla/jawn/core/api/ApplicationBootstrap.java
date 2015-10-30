package net.javapla.jawn.core.api;

import net.javapla.jawn.core.ApplicationConfig;

public interface ApplicationBootstrap {

    void bootstrap(ApplicationConfig config);
    void destroy();
}
