package net.javapla.jawn.core.api;

import net.javapla.jawn.core.ApplicationConfig;

/**
 * Used by modules to hook into the framework
 */
public interface ApplicationBootstrap {

    void bootstrap(ApplicationConfig config);
    void destroy();
}
