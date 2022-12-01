package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router.RoutePath;

class RouterImplTest {

    @Test
    void simple() {
        List<Route> routes = Arrays.asList(route(HttpMethod.GET, "/simple"));
        RouterImpl router = new RouterImpl(routes);
        
        RoutePath route = router.retrieve(HttpMethod.GET, "/simple");
        assertNotNull(route);
        assertEquals("/simple", route.route().path());
        assertEquals(HttpMethod.GET, route.route().method());
        
        route = router.retrieve(HttpMethod.GET, "/nothing");
        assertEquals(RouterImpl.NOT_FOUND, route);
    }
    
    @Test
    void samePathNotSameMethod() {
        List<Route> routes = Arrays.asList(route(HttpMethod.GET, "/first"));
        RouterImpl router = new RouterImpl(routes);
        
        RoutePath route = router.retrieve(HttpMethod.POST, "/first");
        assertEquals(RouterImpl.NOT_FOUND, route);
        
        route = router.retrieve(HttpMethod.PUT, "/first");
        assertEquals(RouterImpl.NOT_FOUND, route);
        
        route = router.retrieve(HttpMethod.DELETE, "/first");
        assertEquals(RouterImpl.NOT_FOUND, route);
    }
    
    @Test
    void pathParamEnd() {
        List<Route> routes = Arrays.asList(route(HttpMethod.GET, "/path/{param}"));
        RouterImpl router = new RouterImpl(routes);
        
        RoutePath route = router.retrieve(HttpMethod.GET, "/path/something");
        assertNotNull(route);
        assertNotEquals(RouterImpl.NOT_FOUND, route);
        assertTrue(route.pathParameters().containsKey("param"));
        assertEquals("something", route.pathParameters().get("param"));
    }
    
    @Test
    void multipePathParams() {
        List<Route> routes = Arrays.asList(route(HttpMethod.GET, "/path/{param}/{param2}"));
        RouterImpl router = new RouterImpl(routes);
        
        RoutePath route = router.retrieve(HttpMethod.GET, "/path/something/else");
        System.out.println(route);
        assertNotNull(route);
        assertNotEquals(RouterImpl.NOT_FOUND, route);
        assertEquals("something", route.pathParameters().get("param"));
        assertEquals("else", route.pathParameters().get("param2"));
        
    }

    static Route route(HttpMethod method, String path) {
        return new Route.Builder(method, path, (ctx) -> ctx).build();
    }
}
