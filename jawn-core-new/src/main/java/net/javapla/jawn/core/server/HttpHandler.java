package net.javapla.jawn.core.server;

public interface HttpHandler {

    void handle(final ServerRequest request, final ServerResponse response) throws Exception;
}
