package net.javapla.jawn.core.server;

public interface HttpHandler {

    void handle(final Request request, final Response response) throws Exception;
}
