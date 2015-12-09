package net.javapla.jawn.core;

import static org.junit.Assert.*;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.RouteBuilder;
import net.javapla.jawn.core.exceptions.ControllerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import app.controllers.KageController;

public class RouteBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected=ControllerException.class)
    public void should_throw_because_action_not_found() {
        RouteBuilder.get().route("/").to(KageController.class, "nothing").build(new FiltersHandler()/*, null*/);
    }
    
    @Test
    public void should_return_route() {
        Route route = RouteBuilder.get().to(KageController.class, "bolle").route("/kage/bolle").build(new FiltersHandler()/*, null*/);
        
        assertNotNull(route);
        assertEquals("getBolle", route.getAction());
    }

}
