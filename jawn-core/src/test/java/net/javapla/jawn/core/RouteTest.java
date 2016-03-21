package net.javapla.jawn.core;

import static org.junit.Assert.*;

import java.util.Map;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.routes.Route;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import app.controllers.KageController;
import app.controllers.henning.more.Kage2Controller;

public class RouteTest {
    private static Controller c;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        c = new KageController();
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
        Route r = new Route(uri3, HttpMethod.GET, c.getClass(), "getBolle", "bolle", null);
        
        assertTrue(r.matches(HttpMethod.GET, "/kage/bolle"));
        System.out.println(r.getPathParametersEncoded("/kage/bolle"));
    }
    
    @Test
    public void should_match_uri_with_package() {
        Kage2Controller c2 = new Kage2Controller();
        String uri4 = "/{package: .*?}/{controller}/{action}";
        Route r = new Route(uri4, HttpMethod.GET, c2.getClass(), "getBolle", "bolle", null);
        
        assertTrue(r.matches(HttpMethod.GET, "/henning/more/kage/bolle"));
    }
    
    @Test
    public void should_fetch_uri_parameters() {
        Kage2Controller c2 = new Kage2Controller();
        String uri4 = "/{package: .*?}/{controller}/{action}";
        Route r = new Route(uri4, HttpMethod.GET, c2.getClass(), "getBolle", "bolle", null);
        
        Map<String, String> params = r.getPathParametersEncoded("/henning/more/kage/bolle");
        assertEquals("henning/more", params.get("package"));
        assertEquals("kage", params.get("controller"));
        assertEquals("bolle", params.get("action"));
    }
    
    @Test
    public void should_match_custom_uri() {
        String uri2 = "/kage/{action}/{henn-i_ng}/controller/{long_id: .*?}";
        Route r = new Route(uri2, HttpMethod.GET, c.getClass(), "getBolle", "bolle", null);
        
        assertTrue(r.matches(HttpMethod.GET, "/kage/bolle/henningkool/controller/whaaaat/kalle/111"));
        System.out.println(r.getPathParametersEncoded("/kage/bolle/henningkool/controller/whaaaat/kalle/111"));
        assertTrue(r.matches(HttpMethod.GET, "/kage/bolle/something/controller/444"));
    }
    
    @Test
    public void should_only_match_numbers() {
        String uri = "/{controller}/{action}/{id: [0-9]+}";
        Route r = new Route(uri, HttpMethod.GET, c.getClass(), "index", "index", null);
        
        String requestGood = "/kage/index/111";
        assertTrue(r.matches(HttpMethod.GET, requestGood));
        Map<String, String> params = r.getPathParametersEncoded(requestGood);
        assertFalse(params.isEmpty());
        assertTrue(params.get("controller").equals("kage"));
        assertTrue(params.get("action").equals("index"));
        assertTrue(params.get("id").equals("111"));
        
        String requestBad = "/kage/index/letters";
        assertFalse(r.matches(HttpMethod.GET, requestBad));
        assertTrue(r.getPathParametersEncoded(requestBad).isEmpty());
    }

}
