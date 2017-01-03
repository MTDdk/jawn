package net.javapla.jawn.core.routes;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class InternalRouteTest {

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

    @Test
    public void route_should_containCorrectAction() {
        InternalRoute route = new InternalRoute("/controller/{action}");
        
        String action = "alpha";
        Map<String, String> pathParameters = route.getPathParametersEncoded("/controller/"+ action);
        
        assertTrue(pathParameters.containsKey("action"));
        assertEquals(action, pathParameters.get("action"));
    }
    
    public void route_should_not_haveAnyParameters() {
        InternalRoute route = new InternalRoute("/controller/action");
        assertTrue(route.getPathParametersEncoded("/controller/action").isEmpty());
    }

}
