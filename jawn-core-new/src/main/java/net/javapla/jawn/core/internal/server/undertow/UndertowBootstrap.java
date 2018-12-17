package net.javapla.jawn.core.internal.server.undertow;

import com.google.inject.Scopes;

import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;

public class UndertowBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(Server.class).to(UndertowServer.class).in(Scopes.SINGLETON);
    }

}
