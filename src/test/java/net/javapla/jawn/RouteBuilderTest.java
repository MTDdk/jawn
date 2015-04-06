package net.javapla.jawn;

import static org.junit.Assert.*;
import net.javapla.jawn.exceptions.ControllerException;

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
        NewRouteBuilder.get().route("/").to(KageController.class, "nothing").build();
    }
    
    @Test
    public void should_return_route() {
        NewRoute route = NewRouteBuilder.get().to(KageController.class, "bolle").route("/kage/bolle").build();
        
        assertNotNull(route);
        assertEquals("getBolle", route.getAction());
    }

}