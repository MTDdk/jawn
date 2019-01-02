package net.javapla.jawn.core.internal;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route.RouteHandler;

@Singleton
final class Router {
    
    private final RouteTrie trie;
    
    /** Used for wildcard routes */
    private final List<RouteHandler> routes = new ArrayList<>();
    
    Router(List<RouteHandler> routes) {
        
        trie = new RouteTrie();
        
        compileRoutes(routes);
        
        // Right now, we use the _routes_ list for all wildcard routes, 
        // and we cache the route for each URL that matches the wildcard routes.
        // This should serve as a quick lookup if we ever stumble upon that same
        // path again.
        //
        // Even though these extra cached routes in the trie should not strain the
        // memory much, as the trie merely stores references to, perhaps, multiple
        // route objects, but this behaviour has not been tested throughly in real life
        // and theoretically we might want to store the wildcard routes AS wildcards
        // in the trie to minimise stored routes, if the trie ends up storing too many
        // routes and TrieNodes
    }
    
    RouteHandler retrieve(final HttpMethod httpMethod, final String requestUri) throws Err.RouteMissing {
        
        // first, take a look in the trie
        RouteHandler route = trie.findExact(requestUri, httpMethod);
        
        // the trie did not have any for us
        if (route == null) {
            for (var r : routes) {
                if (r.matches(requestUri)) {
                    trie.insert(requestUri, r); // cache it
                    return r;
                }
            }
        }
        
        if (route == null) throw new Err.RouteMissing(requestUri, "Failed to map resource to URI: " + httpMethod.name() + " : " + requestUri);
        return route;
    }
    
    private void compileRoutes(List<RouteHandler> routes) {
        
        for (RouteHandler route : routes) {
            if (route.isUrlFullyQualified()) {
                trie.insert(route.path(), route);
            } else {
                this.routes.add(route);
            }
        }
    }
    
    public void recompileRoutes(final List<RouteHandler> newOrAlteredRoutes) {
        this.routes.clear();
        this.trie.clear();
        
        compileRoutes(newOrAlteredRoutes);
    }
    
    /**
     * @author MTD (github/mtddk)
     */
    static final class RouteTrie {
        
        public static final char WILDCARD = '*';
        
        private final TrieNode root;
        
        
        public RouteTrie() {
            root = new TrieNode('#');
        }
        
        public void clear() {
            root.clear();
        }
        
        public void insert(String uri, RouteHandler route) {
            insert(uri.toCharArray(), route);
        }
        
        public synchronized void insert(final char[] input, RouteHandler route) {
            TrieNode current = root, child;
            for (char c : input) {
                child = current.nodes[c];
                if(child == null) {
                    child = new TrieNode(c);
                    current.nodes[c] = child;
                    current.end = false;
                }
                current = child;
            }
            current.routes[route.method().ordinal()] = route;
            current.routes[HttpMethod.HEAD.ordinal()] = route;
        }
        
        public boolean startsWith(final String input) {
            return startsWith(input.toCharArray());
        }
        
        public boolean startsWith(final char[] arr) {
            TrieNode current = root;
            for(char c : arr) {
                if(current.nodes[c] == null)
                    return false;
                else
                    current = current.nodes[c];
            }
            return true;
        }
        
        /**
         * Does this structure contain this <b>exact</b> char sequence
         * @param arr
         * @return
         */
        public RouteHandler findExact(final char[] arr, final HttpMethod method) {
            TrieNode current = root;
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                if (current.nodes[c] == null)
                    return null;
                else
                    current = current.nodes[c];
            }
            return current.routes[method.ordinal()];
        }
        
        public RouteHandler findExact(final CharSequence str, final HttpMethod method) {
            TrieNode current = root;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (current.nodes[c] == null)
                    return null;
                else
                    current = current.nodes[c];
            }
            return current.routes[method.ordinal()];
        }
        
        /**
         * Cannot handle if a route starts with '/'
         * @param arr
         * @return
         */
        public final RouteHandler findRoute(final char[] arr, final HttpMethod method) {
            TrieNode current = root;
            char c;
            for (int i = 0; i < arr.length; i++) {
                c = arr[i];
                
                if (current.nodes[c] == null) {
                    // might be a wildcard search
                    if (current.nodes[WILDCARD] != null) {
                        // if this is the last part of a possible route, then just return the route
                        if (current.nodes[WILDCARD].end)
                            return current.nodes[WILDCARD].routes[method.ordinal()];//routes.get(current.nodes[WILDCARD].routeIndex);
                        
                        // try the wildcard search
//                        TrieNode node = doWildcardSearch(current.nodes[WILDCARD], arr, i);
//                        if (node != null) return routes.get(node.routeIndex);
                        
                        // we are at the wildcard, so we continue to the next char, which should be a slash '/'
                        current = current.nodes[WILDCARD].nodes['/'];
                        do {
                            // jump to next segment
                            while(++i < arr.length && arr[i] != '/');
                            i++;
                        } while (current.nodes[arr[i]] == null);
                        current = current.nodes[arr[i]];
                    } else
                        return null;
                } else {
                    current = current.nodes[c];
                }
            }
            return current.routes[method.ordinal()];
        }
        
        /**
         * Whenever a wildcard is detected during ordinary traversal,
         * continue to the next concrete URL segment - if segment found, continue,
         * if not, repeat.
         * @return 
         */
        /*private final TrieNode doWildcardSearch(final TrieNode node, final char[] arr, final int i) {
            // Start out by jumping to the next URL segment
            int current = i + 1;
            while(current < arr.length && arr[current++] != '/');
            
            // we are at the wildcard, so we continue to the next char, which should be a slash '/'
            TrieNode n = node.nodes['/'];
            
            char c;
            for (; current < arr.length; current++) {
                c = arr[current];
                if (n.nodes[c] == null) {
                    // jump to next segment
                    while(current < arr.length && arr[current++] != '/');
                    // return if no more segments available
                    if (current == arr.length) return null; // this should force the #findRoute to return null
                } else {
                    n = n.nodes[c];
                }
            }
            
            return n;
        }*/
        
        final class TrieNode {
            final TrieNode[] nodes;
            final char content;
            final RouteHandler[] routes; // a route can exist for GET,POST,PUT,etc
            boolean end = true;
            
            TrieNode(char c) {
                //nodes = new SearchTrie[255];//extended ascii
                nodes = new TrieNode[128];//ascii
                content = c;
                routes = new RouteHandler[HttpMethod.values().length];
            }
            
            public void clear() {
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = null;
                }
                for (int i = 0; i < routes.length; i++) {
                    routes[i] = null;
                }
            }
            
            @Override
            public String toString() {
                StringBuilder bob = new StringBuilder();
                for (TrieNode node : nodes) {
                    if (node != null) {
                        bob.append(node.content);
                        bob.append(',');
                    }
                }
                return content + " " + bob.toString();
            }
        }
    }
}
