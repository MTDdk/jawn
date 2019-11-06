package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;

public class RouteTrieTest {


    @Test
    public void simple() {
        Router.RouteTrie trie = new Router.RouteTrie();
        
        String path = "/route/to/redemption";
        Route route = new Route.Builder(HttpMethod.GET).path(path).build();
        
        trie.insert(path, route);
        
        Route r = trie.findExact(path, HttpMethod.GET);
        assertThat(r).isNotNull();
        assertThat(r.path()).isEqualTo(path);
        
        Route r2 = trie.findExact(path.toCharArray(), HttpMethod.GET);
        assertThat(r).isEqualTo(r2);
    }
    
    @Test
    public void wildcardInMiddle() {
        Router.RouteTrie trie = new Router.RouteTrie();
        
        String path = "/route/*/redemption";
        Route route = new Route.Builder(HttpMethod.GET).path(path).build();
        
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
        Router.RouteTrie trie = new Router.RouteTrie();
        
        String path = "/route/redemption/of/*";
        Route route = new Route.Builder(HttpMethod.GET).path(path).build();
        
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
    
    @Test
    public void startsWith() {
        Router.RouteTrie trie = new Router.RouteTrie();
        
        String path = "/route/to/redemption";
        Route route = new Route.Builder(HttpMethod.DELETE).path(path).build();
        trie.insert(path, route);
        
        assertThat(trie.startsWith("/route/to")).isTrue();
        assertThat(trie.startsWith("/rout".toCharArray())).isTrue();
        assertThat(trie.startsWith("/route/to/b")).isFalse();
    }
    
}
