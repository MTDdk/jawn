package net.javapla.jawn.core.api;

import net.javapla.jawn.core.routes.RouteBuilder;

public interface Routes {
    RouteBuilder GET();
    RouteBuilder POST();
    RouteBuilder PUT();
    RouteBuilder DELETE();
    RouteBuilder HEAD();
}
