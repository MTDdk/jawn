package net.javapla.jawn.core.server;

import java.util.Optional;
import java.util.concurrent.Executor;

public interface Server {

    void start(/*ServerConfig serverConfig*/) throws Exception;
    void stop() throws Exception;
    void join() throws InterruptedException;
    Optional<Executor> executor();
}
