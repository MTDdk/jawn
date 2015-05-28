package net.javapla.jawn.core.spi;

import net.javapla.jawn.core.RouteBuilder;

public interface Routes {
    RouteBuilder GET();
    RouteBuilder POST();
    RouteBuilder PUT();
    RouteBuilder DELETE();
    RouteBuilder HEAD();
}
