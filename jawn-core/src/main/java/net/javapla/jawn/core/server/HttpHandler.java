package net.javapla.jawn.core.server;

import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.Response;

public interface HttpHandler {

    void handle(final Request request, final Response response) throws Exception;
}
