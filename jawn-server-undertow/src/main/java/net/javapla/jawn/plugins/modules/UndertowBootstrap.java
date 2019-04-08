package net.javapla.jawn.plugins.modules;

import com.google.inject.Scopes;

import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.server.undertow.UndertowServer;

public class UndertowBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(Server.class).to(UndertowServer.class).in(Scopes.SINGLETON);
    }

}
