package net.javapla.jawn.plugins.modules;

import net.javapla.jawn.core.ApplicationConfig;
import net.javapla.jawn.core.spi.ApplicationBootstrap;
import net.javapla.jawn.security.SecurityModule;

public class SecurityConfig implements ApplicationBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.registerModules(new SecurityModule());
    }

    @Override
    public void destroy() {
    }

}
