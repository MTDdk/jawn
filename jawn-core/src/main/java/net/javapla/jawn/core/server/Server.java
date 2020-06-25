package net.javapla.jawn.core.server;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

public interface Server {

    void start(ServerConfig.Impl serverConfig) throws Exception;
    void stop() throws Exception;
    void join() throws InterruptedException;
    Optional<Executor> executor();
    
    
    static boolean connectionResetByPeer(final Throwable cause) {
        return Optional.ofNullable(cause)
            .filter(IOException.class::isInstance)
            .map(x -> x.getMessage())
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .map(message -> message.contains("reset by peer") || message.contains("broken pipe"))
            .orElse(cause instanceof ClosedChannelException || cause instanceof EOFException);
    }
}
