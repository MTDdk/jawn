package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.AssertionsHelper;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.RouterImpl.TriePath;

class RouteTrieTest {

    @Test
    void exact() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/to/redemption";
        TriePath route = new TriePath(route(HttpMethod.GET, path));
        
        trie.insert(route);
        
        TriePath exact = trie.findExact(path, HttpMethod.GET);
        assertNotNull(exact);
        AssertionsHelper.ass(path, exact.trieApplicable);
        
        TriePath exact2 = trie.findExact(path.toCharArray(), HttpMethod.GET);
        assertEquals(exact, exact2);
    }
    
    @Test
    void segmentInMiddle() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/#/redemption";
        TriePath route = new TriePath(route(HttpMethod.GET, path));
        
        trie.insert(route);
        
        TriePath r = trie.findRoute("/route/to/redemption", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
        
        // TODO a wildcard
        /*r = trie.findRoute("/route/along/the/way/to/redemption", HttpMethod.GET);
        assertNotNull(r);
        assertEquals(path, r.route.path());*/
        
        r = trie.findRoute("/route/along_the_way_to/redemption", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
    }
    
    @Test
    void segmentInMiddle_notFound() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/#/redemption";
        TriePath route = new TriePath(route(HttpMethod.GET, path));
        
        trie.insert(route);
        
        // should not be found
        TriePath r = trie.findRoute("/route/redemption", HttpMethod.GET);
        assertNull(r);
        
        r = trie.findRoute("/route/r/edemption", HttpMethod.GET);
        assertNull(r);
    }
    
    @Test
    void segmentEnd() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/to/#";
        TriePath route = new TriePath(route(HttpMethod.GET, path));
        
        trie.insert(route);
        
        TriePath r = trie.findRoute("/route/to/redemption", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
        
        r = trie.findRoute("/route/to/everywhere", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
        
        r = trie.findRoute("/route/to/along_the_way", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
        
        // only applicable to true WILDCARD
        r = trie.findRoute("/route/to/along/the/way", HttpMethod.GET);
        assertNull(r);
    }
    
    @Test
    void segmentEnd_startingWithLongerParam() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/path/#";
        TriePath route = new TriePath(route(HttpMethod.GET, "/path/{param}"), path, Arrays.asList(null, "param"));
        
        trie.insert(route);
        
        // first try, no exact value yet
        TriePath r = trie.findExact("/path/oncelong", HttpMethod.GET);
        assertNull(r);
        
        // first actual try
        r = trie.findRoute("/path/oncelong", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
        
        trie.insert("/path/oncelong", r);
        
        r = trie.findExact("/path/oncelong", HttpMethod.GET);
        assertNotNull(r);
        
        
        // second try, same route, different URI
        r = trie.findExact("/path/once", HttpMethod.GET);
        assertNull(r);
        
        r = trie.findRoute("/path/once", HttpMethod.GET);
        assertNotNull(r);
        AssertionsHelper.ass(path, r.trieApplicable);
        
        
        // third try, same route, appended to same URI
        r = trie.findExact("/path/oncelonger", HttpMethod.GET);
        assertNull(r);
        r = trie.findRoute("/path/oncelonger", HttpMethod.GET);
        assertNotNull(r);
    }
    
    @Test
    void segmentEnd_notFound() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/route/to/#";
        TriePath route = new TriePath(route(HttpMethod.GET, path));
        
        trie.insert(route);
        
        TriePath r = trie.findRoute("/route/redemption", HttpMethod.GET);
        assertNull(r);
        
        r = trie.findRoute("/routing/to/everywhere", HttpMethod.GET);
        assertNull(r);
        
        r = trie.findRoute("/route/to", HttpMethod.GET);
        assertNull(r);
        
        // TODO should this be treated as found?
        /*r = trie.findRoute("/route/to/", HttpMethod.GET);
        assertNull(r);*/
    }
    
    @Test
    void multipleSegmentEnd() {
        RouterImpl.RouteTrie trie = new RouterImpl.RouteTrie();
        
        String path = "/path/#/#";
        TriePath route = new TriePath(route(HttpMethod.GET, path));
        
        trie.insert(route);
        
        TriePath r = trie.findRoute("/path/something/else", HttpMethod.GET);
        assertNotNull(r);
    }


    static Route route(HttpMethod method, String path) {
        return new Route.Builder(method, path, (ctx) -> ctx).build();
    }
}
