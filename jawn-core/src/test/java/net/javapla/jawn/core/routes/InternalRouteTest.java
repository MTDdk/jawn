package net.javapla.jawn.core.routes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class InternalRouteTest {

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
