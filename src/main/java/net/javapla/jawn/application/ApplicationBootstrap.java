package net.javapla.jawn.application;

import net.javapla.jawn.core.ApplicationConfig;

public interface ApplicationBootstrap {

    void bootstrap(ApplicationConfig config);
    void destroy();
}
