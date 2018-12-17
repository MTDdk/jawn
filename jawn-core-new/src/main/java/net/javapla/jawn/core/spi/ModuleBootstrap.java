package net.javapla.jawn.core.spi;

/**
 * Used by modules to hook into the framework
 */
public interface ModuleBootstrap {

    void bootstrap(ApplicationConfig config);
}
