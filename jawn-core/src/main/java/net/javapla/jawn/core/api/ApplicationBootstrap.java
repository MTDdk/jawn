package net.javapla.jawn.core.api;

import net.javapla.jawn.core.ApplicationConfig;

/**
 * 
 * @author ann
 * @deprecated Use Jawn#use for modules
 */
@Deprecated
public interface ApplicationBootstrap {

    void bootstrap(ApplicationConfig config);
    void destroy();
}
