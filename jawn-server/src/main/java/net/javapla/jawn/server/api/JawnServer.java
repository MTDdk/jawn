package net.javapla.jawn.server.api;

import net.javapla.jawn.server.ServerConfig;

public interface JawnServer {

    void setupAndStartServer(ServerConfig config) throws Exception;
}
