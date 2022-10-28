package net.javapla.jawn.core;

public interface Router {

    Route.Handler retrieve(HttpMethod httpMethod, String requestUri);// throws Up.RouteMissing, Up.RouteFoundWithDifferentMethod;

}
