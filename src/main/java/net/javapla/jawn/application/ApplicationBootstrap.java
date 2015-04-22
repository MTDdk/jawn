package net.javapla.jawn.application;

import net.javapla.jawn.core.ConfigApp;

public interface ApplicationBootstrap {

    void bootstrap(ConfigApp config);
    void destroy();
}
