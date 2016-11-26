package net.javapla.jawn.core.server;

public interface Server {

    void start() throws Exception;
    void stop() throws Exception;
    void join() throws InterruptedException;
}
