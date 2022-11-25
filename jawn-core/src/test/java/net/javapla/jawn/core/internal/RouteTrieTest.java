package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;

class RouteTrieTest {

    @Test
    void exact() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/to/redemption";
        Route route = route(HttpMethod.GET, path);
        
        trie.insert(path, route);
        
        Route exact = trie.findExact(path, HttpMethod.GET);
        assertNotNull(exact);
        assertEquals(path, exact.path());
        
        Route exact2 = trie.findExact(path.toCharArray(), HttpMethod.GET);
        assertEquals(exact, exact2);
    }
    
    @Test
    public void wildcardInMiddle() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/*/redemption";
        Route route = route(HttpMethod.GET, path);
        
        trie.insert(path, route);
        
        Route r = trie.findRoute("/route/to/redemption", HttpMethod.GET);
        assertNotNull(r);
        assertEquals(route.path(), r.path());
        
        r = trie.findRoute("/route/along/the/way/to/redemption", HttpMethod.GET);
        System.out.println(r);
        /*assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());*/
        
        r = trie.findRoute("/route/along_the_way_to/redemption", HttpMethod.GET);
        System.out.println(r);
        /*assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());*/
    }


    static Route route(HttpMethod method, String path) {
        return new Route.Builder(method, path, (ctx) -> ctx).build();
    }
}
