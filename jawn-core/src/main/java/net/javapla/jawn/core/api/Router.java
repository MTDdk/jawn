package net.javapla.jawn.core.api;

import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.routes.Route;

public interface Router {
    /*RouteBuilder GET();
    RouteBuilder POST();
    RouteBuilder PUT();
    RouteBuilder DELETE();
    RouteBuilder HEAD();*/
    
    Route retrieveRoute(HttpMethod method, String url);
}
