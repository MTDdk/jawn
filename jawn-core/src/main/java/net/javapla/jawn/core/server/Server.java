package net.javapla.jawn.core.server;

public interface Server {

    void start(ServerConfig serverConfig) throws Exception;
    void stop() throws Exception;
    void join() throws InterruptedException;
}
