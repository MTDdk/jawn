package net.javapla.jawn.plugins.modules;

import com.google.inject.Scopes;

import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.server.netty.NettyServer;

public class NettyBootstrap implements ModuleBootstrap {

    @Override
    public void bootstrap(ApplicationConfig config) {
        config.binder().bind(Server.class).to(NettyServer.class).in(Scopes.SINGLETON);
    }

}
