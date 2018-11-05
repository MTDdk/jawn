package net.javapla.jawn.core.routes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import app.controllers.UnitTestController;
import app.controllers.testing.more.CakeController;
import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.util.Modes;

public class RouterImplTest {

    private JawnConfigurations configurations;
    private ArrayList<RouteBuilder> builders;
    private static FiltersHandler filters;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        filters = mock(FiltersHandler.class);
    }

    @Before
    public void setUp() throws Exception {
        builders = new ArrayList<>();
        configurations = new JawnConfigurations(Modes.TEST);
    }

    @After
    public void tearDown() throws Exception {
        
    }

    @Test
    public void runStandardTests() {
        standardTests();
    }
    
    @Test
    public void devConfigurationsTest() {
        configurations = new JawnConfigurations(Modes.DEV);
        standardTests();
    }
    
    @Test
    public void prodConfigurationsTest() {
        configurations = new JawnConfigurations(Modes.PROD);
        standardTests();
    }
    
    @Test
    public void customRouteAction_should_mapControllerActions() {
        builders.add(RouteBuilder.get().route("/{action}").to(UnitTestController.class));
        RouterImpl router = setupRouter();
        
        Route route = router.retrieveRoute(HttpMethod.GET, "/simple");
        assertEquals("getSimple", route.getAction());
        assertEquals("simple", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.GET, "/simple_test_action");
        assertEquals("getSimpleTestAction", route.getAction());
        assertEquals("simple_test_action", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
    }
    
    @Test
    public void customRouteAction_should_notBeFound() {
        builders.add(RouteBuilder.get().route("/{action}").to(UnitTestController.class));
        RouterImpl router = setupRouter();
        
        try {
            router.retrieveRoute(HttpMethod.GET, "/notthere");
            fail();
        } catch (RouteException expected) {}
        
        try {
            router.retrieveRoute(HttpMethod.POST, "/simple");
        } catch (RouteException expected) {}
    }
    
    @Test
    public void customRoute_should_mapToStatedAction() {
        builders.add(RouteBuilder.get().route("/{someid}").to(UnitTestController.class, "getLongerAction"));
        RouterImpl router = setupRouter();
        
        try {
            Route route = router.retrieveRoute(HttpMethod.GET, "/7777");
            assertEquals("getLongerAction", route.getAction());
        } catch (RouteException expected) {}
    }
    
    @Test
    public void customRouteController() {
        builders.add(RouteBuilder.get().route("/start/{controller}"));
        builders.add(RouteBuilder.get().route("/start/{package: .*?}/{controller}"));
        RouterImpl router = setupRouter();
        
        Route route = router.retrieveRoute(HttpMethod.GET, "/start/unit_test");
        assertEquals("index", route.getAction());
        assertEquals("index", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.GET, "/start/testing/more/cake");
        assertEquals("index", route.getAction());
        assertEquals("index", route.getActionName());
        assertEquals(CakeController.class.getName(), route.getController().getName());
    }
    
    @Test
    public void customRouteControllerAction() {
        builders.add(RouteBuilder.get().route("/{action}/start/{controller}"));
        builders.add(RouteBuilder.post().route("/{action}/start/{controller}"));
        RouterImpl router = setupRouter();
        
        Route route = router.retrieveRoute(HttpMethod.GET, "/simple/start/unit_test");
        assertEquals("getSimple", route.getAction());
        assertEquals("simple", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.POST, "/simple/start/unit_test");
        assertEquals("postSimple", route.getAction());
        assertEquals("simple", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        // standard route should still be in effect
        route = router.retrieveRoute(HttpMethod.POST, "/unit_test/simple");
        assertEquals("postSimple", route.getAction());
        assertEquals("simple", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
    }
    
    @Test
    public void extremelySimpleJsonResponseAsDEV() {
        builders.add(RouteBuilder.get().route("/test").with(Results.json("test")));
        configurations = new JawnConfigurations(Modes.DEV);
        RouterImpl router = setupRouter();
        
        router.retrieveRoute(HttpMethod.GET, "/test");
        //not throwing
    }

    private void standardTests() {
        RouterImpl router = setupRouter();
        
        Route route = router.retrieveRoute(HttpMethod.GET, "/unit_test");
        assertEquals("index", route.getAction());
        assertEquals("index", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.GET, "/unit_test/simple");
        assertEquals("getSimple", route.getAction());
        assertEquals("simple", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.POST, "/unit_test/simple");
        assertEquals("postSimple", route.getAction());
        assertEquals("simple", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.GET, "/unit_test/simple_test_action");
        assertEquals("getSimpleTestAction", route.getAction());
        assertEquals("simple_test_action", route.getActionName());
        assertEquals(UnitTestController.class.getName(), route.getController().getName());
        
        route = router.retrieveRoute(HttpMethod.GET, "/testing/more/cake");
        assertEquals("index", route.getAction());
        assertEquals("index", route.getActionName());
        assertEquals(CakeController.class.getName(), route.getController().getName());
        
        try {
            router.retrieveRoute(HttpMethod.GET, "/not/findable");
            fail();
        } catch (RouteException expected) {}
        
        try {
            router.retrieveRoute(HttpMethod.POST, "/unit_test"); //no UnitTestController.postIndex
            fail();
        } catch (RouteException expected) {}
    }
    
    private RouterImpl setupRouter() {
        RouterImpl router = new RouterImpl(builders, filters, configurations);
        router.compileRoutes(mock(ActionInvoker.class));
        return router;
    }
}
