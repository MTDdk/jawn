package net.javapla.jawn.core.routes;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.http.Context;

@FunctionalInterface
public interface ResponseFunction {
    
    //Object handle(Req req, Resp resp);
    Result handle(Context context);

}
