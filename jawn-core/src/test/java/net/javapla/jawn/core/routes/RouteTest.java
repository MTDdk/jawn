package net.javapla.jawn.core.routes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import app.controllers.UnitTestController;
import app.controllers.testing.more.CakeController;
import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.http.HttpMethod;

public class RouteTest {
    
    private final String pack = "/testing/more";
    private final String controller = "/cake";
    private final String action = "/buns";
    private final String method = "getBuns";
    
    private static Controller c;
    

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        c = new UnitTestController();
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

    @Test
    public void matches() {
        
    }
    
    @Test
    public void should_match_uri_with_controller_action() {
        String uri3 = "/{controller}/{action}";
        Route r = new Route(uri3, HttpMethod.GET, c.getClass(), method, action, null);
        
        assertTrue(r.matches(HttpMethod.GET, controller+action));
        System.out.println(r.getPathParametersEncoded(controller+action));
    }
    
    @Test
    public void should_match_uri_with_package() {
        CakeController c2 = new CakeController();
        String uri4 = "/{package: .*?}/{controller}/{action}";
        Route r = new Route(uri4, HttpMethod.GET, c2.getClass(), method, action, null);
        
        assertTrue(r.matches(HttpMethod.GET, pack+controller+action));
    }
    
    @Test
    public void should_fetch_uri_parameters() {
        CakeController c2 = new CakeController();
        String uri4 = "/{package: .*?}/{controller}/{action}";
        Route r = new Route(uri4, HttpMethod.GET, c2.getClass(), method, action, null);
        
        Map<String, String> params = r.getPathParametersEncoded(pack+controller+action);
        assertEquals("testing/more", params.get("package"));
        assertEquals("cake", params.get("controller"));
        assertEquals("buns", params.get("action"));
    }
    
    @Test
    public void should_match_custom_uri() {
        String uri2 = "/cake/{action}/{some-thin_g}/controller/{long_id: .*?}";
        Route r = new Route(uri2, HttpMethod.GET, c.getClass(), method, action, null);
        
        assertTrue(r.matches(HttpMethod.GET, "/cake/buns/daddykool/controller/whaaaat/goingstrong/111"));
        System.out.println(r.getPathParametersEncoded("/cake/buns/daddykool/controller/whaaaat/goingstrong/111"));
        assertTrue(r.matches(HttpMethod.GET, "/cake/buns/something/controller/444"));
    }
    
    @Test
    public void should_only_match_numbers() {
        String uri = "/{controller}/{action}/{id: [0-9]+}";
        Route r = new Route(uri, HttpMethod.GET, c.getClass(), "index", "index", null);
        
        String requestGood = "/cake/index/111";
        assertTrue(r.matches(HttpMethod.GET, requestGood));
        Map<String, String> params = r.getPathParametersEncoded(requestGood);
        assertFalse(params.isEmpty());
        assertTrue(params.get("controller").equals("cake"));
        assertTrue(params.get("action").equals("index"));
        assertTrue(params.get("id").equals("111"));
        
        String requestBad = "/cake/index/letters";
        assertFalse(r.matches(HttpMethod.GET, requestBad));
        assertTrue(r.getPathParametersEncoded(requestBad).isEmpty());
    }

    @Test
    public void route_is_fullyQualified() {
        String uri = "/something/getit";
        Route route = new Route(uri, HttpMethod.GET, null, null, null, null);
        
        assertTrue(route.isUrlFullyQualified());
    }
    
    @Test
    public void route_is_not_fullyQualified() {
        String uri = "/something/getit/{action}";
        Route route = new Route(uri, HttpMethod.GET, null, null, null, null);
        
        assertFalse(route.isUrlFullyQualified());
        
        uri = "/{controller}/getit/{action}";
        route = new Route(uri, HttpMethod.GET, null, null, null, null);
        
        assertFalse(route.isUrlFullyQualified());
        
        uri = "/{controller}/getit/{id}";
        route = new Route(uri, HttpMethod.GET, null, null, null, null);
        
        assertFalse(route.isUrlFullyQualified());
        
        uri = "/{package}/something";
        route = new Route(uri, HttpMethod.GET, null, null, null, null);
        
        assertFalse(route.isUrlFullyQualified());
    }
}
