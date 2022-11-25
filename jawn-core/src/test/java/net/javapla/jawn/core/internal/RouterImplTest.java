package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;

class RouterImplTest {

    @Test
    void simple() {
        List<Route> routes = Arrays.asList(route(HttpMethod.GET, "/simple"));
        RouterImpl router = new RouterImpl(routes);
        
        Route route = router.retrieve(HttpMethod.GET, "/simple");
        assertNotNull(route);
        assertEquals("/simple", route.path());
        assertEquals(HttpMethod.GET, route.method());
        
        route = router.retrieve(HttpMethod.GET, "/nothing");
        assertEquals(Route.NOT_FOUND, route);
    }
    
    @Test
    void samePathNotSameMethod() {
        List<Route> routes = Arrays.asList(route(HttpMethod.GET, "/first"));
        RouterImpl router = new RouterImpl(routes);
        
        Route route = router.retrieve(HttpMethod.POST, "/first");
        assertEquals(Route.NOT_FOUND, route);
        
        route = router.retrieve(HttpMethod.PUT, "/first");
        assertEquals(Route.NOT_FOUND, route);
        
        route = router.retrieve(HttpMethod.DELETE, "/first");
        assertEquals(Route.NOT_FOUND, route);
    }

    static Route route(HttpMethod method, String path) {
        return new Route.Builder(method, path, (ctx) -> ctx).build();
    }
}
