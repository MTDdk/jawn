package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.RouteHandler;
import net.javapla.jawn.core.internal.Router.RouteTrie;

public class RouteTrieTest {


    @Test
    public void simple() {
        RouteTrie trie = new RouteTrie();
        
        String path = "/route/to/redemption";
        RouteHandler route = new Route.Builder(HttpMethod.GET).path(path).build();
        
        trie.insert(path, route);
        
        Route r = trie.findExact(path, HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(path);
    }
    
    @Test
    public void wildcardInMiddle() {
        RouteTrie trie = new RouteTrie();
        
        String path = "/route/*/redemption";
        RouteHandler route = new Route.Builder(HttpMethod.GET).path(path).build();
        
        trie.insert(path, route);
        
        Route r = trie.findRoute("/route/to/redemption".toCharArray(), HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());
        
        r = trie.findRoute("/route/along/the/way/to/redemption".toCharArray(), HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());
        
        r = trie.findRoute("/route/along_the_way_to/redemption".toCharArray(), HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());
    }

    @Test
    public void wildcardAtEnd() {
        RouteTrie trie = new RouteTrie();
        
        String path = "/route/redemption/of/*";
        RouteHandler route = new Route.Builder(HttpMethod.GET).path(path).build();
        
        trie.insert(path, route);
        
        Route r = trie.findRoute("/route/redemption/of".toCharArray(), HttpMethod.GET);
        assertThat(r).isNull();
        
        r = trie.findRoute("/route/redemption/of/anything".toCharArray(), HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());
        
        r = trie.findRoute("/route/redemption/of/12345".toCharArray(), HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(route.path());
    }
}
