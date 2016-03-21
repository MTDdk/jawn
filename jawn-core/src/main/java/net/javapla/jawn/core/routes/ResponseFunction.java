package net.javapla.jawn.core.routes;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;

@FunctionalInterface
public interface ResponseFunction {
    
    //Object handle(Req req, Resp resp);
    Response handle(Context context);

}
