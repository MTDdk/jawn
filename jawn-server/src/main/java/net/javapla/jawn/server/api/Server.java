package net.javapla.jawn.server.api;

public interface Server {

    void start() throws Exception;
    void stop() throws Exception;
    void join() throws InterruptedException;
}
