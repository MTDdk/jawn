package net.javapla.jawn.server.api;

import net.javapla.jawn.core.http.Req;
import net.javapla.jawn.core.http.Resp;

public interface HttpHandler {

    void handle(final Req request, final Resp response) throws Exception;
}
