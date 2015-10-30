package net.javapla.jawn.core.api;

import net.javapla.jawn.core.RouteBuilder;

public interface Routes {
    RouteBuilder GET();
    RouteBuilder POST();
    RouteBuilder PUT();
    RouteBuilder DELETE();
    RouteBuilder HEAD();
}
