package net.javapla.jawn.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import app.controllers.UnitTestController;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.routes.RouteBuilder;

public class RouteBuilderTest {


    @Test(expected=RouteException.class)
    public void should_throw_because_action_not_found() {
        RouteBuilder.get().route("/").to(UnitTestController.class, "nothing").build(new FiltersHandler(), null);
    }
    
    @Test
    public void should_return_route() {
        Route route = RouteBuilder.get().to(UnitTestController.class, "bolle").route("/kage/bolle").build(new FiltersHandler(), null);
        
        assertNotNull(route);
        assertEquals("getBolle", route.getAction());
    }

}
