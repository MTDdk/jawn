package net.javapla.jawn.core.internal;

import java.util.List;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route.RouteHandler;

@Singleton
public class Router {
    
    private final RouteTrie deducedRoutes;
    
    Router(List<RouteHandler> routes) {
        
        deducedRoutes = new RouteTrie();
        
        compileRoutes(routes);
    }
    
    RouteHandler retrieve(HttpMethod httpMethod, String requestUri) {
        
        
        RouteHandler route = deducedRoutes.findExact(requestUri, httpMethod);
        if (route == null) throw new Err.RouteError("Failed to map resource to URI: " + httpMethod.name() + " : " + requestUri);
        
        return route;
    }
    
    private void compileRoutes(List<RouteHandler> routes) {
        routes.stream().forEach(route -> deducedRoutes.insert(route.path(), route));
    }
    
    

    /**
     * @author MTD (github/mtddk)
     */
    final class RouteTrie {
        
        //public static final char WILDCARD = '*';
        
        private final TrieNode root;
        
        
        public RouteTrie() {
            root = new TrieNode('#');
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
            //for(char c : arr) {
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                if(current.nodes[c] == null)
                    return null;
                else
                    current = current.nodes[c];
            }
            return current.routes[method.ordinal()];
        }
        
        public RouteHandler findExact(final String str, final HttpMethod method) {
            TrieNode current = root;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if(current.nodes[c] == null)
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
        /*public final Route findRoute(final char[] arr) {
            TrieNode current = root;
            char c;
            for (int i = 0; i < arr.length; i++) {
                c = arr[i];
                
                if(current.nodes[c] == null) {
                    // might be a wildcard search
                    if (current.nodes[WILDCARD] != null) {
                        // if this is the last part of a possible route, then just return the route
                        if (current.nodes[WILDCARD].routeIndex > -1)
                            return routes.get(current.nodes[WILDCARD].routeIndex);
                        
                        // try the wildcard search
//                        TrieNode node = doWildcardSearch(current.nodes[WILDCARD], arr, i);
//                        if (node != null) return routes.get(node.routeIndex);
                        current = current.nodes[WILDCARD].nodes['/'];
                        do {
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
            return routes.get(current.routeIndex);
        }*/
        
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
            final RouteHandler[] routes;
            
            TrieNode(char c) {
                //nodes = new SearchTrie[255];//extended ascii
                nodes = new TrieNode[128];//ascii
                content = c;
                routes = new RouteHandler[HttpMethod.values().length];
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
