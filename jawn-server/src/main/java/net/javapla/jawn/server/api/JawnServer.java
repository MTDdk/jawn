package net.javapla.jawn.server.api;

import net.javapla.jawn.core.server.ServerConfig;

public interface JawnServer {

    void setupAndStartServer(ServerConfig config) throws Exception;
}
