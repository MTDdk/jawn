package net.javapla.jawn.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.Up;

final class RouterImpl implements Router {
    
    private final RouteTrie trie;
    
    /** Used for wildcard routes */
    private final List<Route> routes = new ArrayList<>();
    
    RouterImpl() {
        
        trie = new RouteTrie();
        
        //compileRoutes(routes);
        
        /*
         Right now, we use the _routes_ list for all wildcard routes, 
         and we cache the route for each URL that matches the wildcard routes.
         This should serve as a quick lookup if we ever stumble upon that same
         path again.
        
         Even though these extra cached routes in the trie should not strain the
         memory much, as the trie merely stores references to, perhaps, multiple
         route objects, but this behaviour has not been tested throughly in real life
         and theoretically we might want to store the wildcard routes AS wildcards
         in the trie to minimise stored routes, if the trie ends up storing too many
         routes and TrieNodes
         
         
         UPDATE:
         Now storing complex routes as simple wildcarded routes.
         Complex = "/users/{username: [a-zA-Z][a-zA-Z_0-9]}" -> wildcarded = "/users/*"
         This means that any information regarding the regex of the route is not a part
         of the lookup in the trie, but will be used when matching after
         retrieval.
         
         Multiple complex routes that ends up with the same wildcarded path, could
         potentially be handled by having a list of the same wildcarded routes
         in the trie and simply go through each of them for matching during
         lookup.
         Just like the buckets in a HashMap.. which the Trie then essentially
         would become..
         - That's for a later iteration
        */
        
    }
    
    RouterImpl(List<Route> routes) {
        this();
        compileRoutes(routes);
    }
    
    
    @Override
    public Route retrieve(final int httpMethod, final String requestUri) /*throws Up.RouteMissing, Up.RouteFoundWithDifferentMethod*/ {
        //final HttpMethod httpMethod = context.req().httpMethod();
        //final String requestUri = context.req().path();
        
        //final char[] uri = requestUri.toCharArray();
        
        //try {
        // first, take a look in the trie
        Route route = trie.findExact(requestUri.toCharArray(), httpMethod);
        if (route != null) return route;
            
        // try with wildcard search
        route = trie.findRoute(requestUri.toCharArray(), httpMethod);
        if (route != null && route.matches(requestUri)) {
            
            trie.insert(requestUri, route); // cache it for later fast look-up
            return route;
            
        } else {
            // The trie did not have any for us..
            // Have a look in the custom routes then
            return goThroughCustom(HttpMethod.values()[httpMethod], requestUri);//, () -> Up.RouteMissing("Failed to map resource to URI: " + httpMethod.name() + " : " + requestUri));
        }
            /*if (route == null) {
                
            }
            
            return route;
            */
        /*} catch (Up.RouteFoundWithDifferentMethod e) {
            
            return goThroughCustom(httpMethod, requestUri);
            
            // Not even the custom routes had any,
            // so it seems the original assessment of "route found" was correct,
            // which is why we re-throw Up
        }*/
    }
    
    private Route goThroughCustom(final HttpMethod httpMethod, final String requestUri) {
        for (Route r : routes) {
            if (r.matches(requestUri)) {
                
                if (r.method() == httpMethod || HttpMethod.HEAD == httpMethod) {
                    trie.insert(requestUri, r); // cache it for later fast look-up
                    return r;
                }
                
                // so we actually found something
                //throwThisIfNothingFound = () -> new Up.RouteFoundWithDifferentMethod(httpMethod.name());
                return Route.METHOD_NOT_ALLOWED;
            }
        }
        
        //throw throwThisIfNothingFound.get();
        return Route.NOT_FOUND;//ctx -> ctx.resp().respond(Status.NOT_FOUND);
    }
    
    RouterImpl compileRoutes(final List<Route> routes) throws Up.RouteAlreadyExists {
        
        for (final Route route : routes) {
            
            addRoute(route);
        }
        return this;
    }
    

    @Override
    public void addRoute(final Route route) {
        Route lookup = trie.findRoute(route.wildcardedPath().toCharArray(), route.method().ordinal());
        if (lookup != null && route.method() != HttpMethod.HEAD && route.wildcardedPath().equals(lookup.wildcardedPath())) {
            throw Up.RouteAlreadyExists(lookup.toString());
        }
        
        if (TriePathParser.hasParams(route.path())) {
            
        }
        
        if (route.isUrlFullyQualified()) {
            trie.insert(route.path(), route);
        } else {
            trie.insert(route.wildcardedPath(), route);
            this.routes.add(route);
        }
    }
    
    public void recompileRoutes(final List<Route> newOrAlteredRoutes) {
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
        
        public void insert(String uri, Route route) {
            insert(uri.toCharArray(), route);
        }
        
        public synchronized void insert(final char[] input, Route route) {
            TrieNode current = root, child;
            for (char c : input) {
                child = current.nodes[c];
                if (child == null) {
                    child = new TrieNode(c);
                    current.nodes[c] = child;
                }
                current = child;
            }
            current.routes[route.method().ordinal()] = route;
            current.routes[HttpMethod.HEAD.ordinal()] = route; // TODO should be handled correctly (HEAD == GET ?) and by whatever is retrieving
            current.end = true;
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
        public Route findExact(final char[] arr, final HttpMethod method) {
            TrieNode current = root;
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                if (current.nodes[c] == null)
                    return null;
                else
                    current = current.nodes[c];
            }
            return current.routes[method.ordinal()];//.get(method);
        }
        
        public Route findExact(final char[] arr, final int method) {
            TrieNode current = root;
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                if (current.nodes[c] == null)
                    return null;
                else
                    current = current.nodes[c];
            }
            return current.routes[method];
        }
        
        public Route findExact(final CharSequence str, final HttpMethod method) {
            TrieNode current = root;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (current.nodes[c] == null)
                    return null;
                else
                    current = current.nodes[c];
            }
            return current.routes[method.ordinal()];//.get(method);
        }
        
        /**
         * Can only handle if a route starts with '/'
         * @param arr
         * @return
         */
        public final Route findRoute(final char[] arr, final int method) {
            TrieNode current = root;
            char c;
            for (int i = 0; i < arr.length; i++) {
                c = arr[i];
                
                if (current.nodes[c] == null) {
                    // might be a wildcard search
                    if (current.nodes[WILDCARD] != null) {
                        // if this is the last part of a possible route, then just return the route
                        if (current.nodes[WILDCARD].end)
                            return current.nodes[WILDCARD].routes[method];//routes.get(current.nodes[WILDCARD].routeIndex);
                        
                        // try the wildcard search
//                        TrieNode node = doWildcardSearch(current.nodes[WILDCARD], arr, i);
//                        if (node != null) return routes.get(node.routeIndex);
                        
                        // we are at the wildcard, so we continue to the next char, which should be a slash '/'
                        current = current.nodes[WILDCARD].nodes['/'];
                        do {
                            // jump to next segment
                            while(++i < arr.length && arr[i] != '/');
                            if (i == arr.length) return null; 
                            i++;
                        } while (current.nodes[arr[i]] == null);
                        current = current.nodes[arr[i]];
                    } else
                        return null;
                } else {
                    current = current.nodes[c];
                }
            }
            return current.routes[method];
        }
        
        public final Route findRoute(final String path, final HttpMethod method) {
            return findRoute(path.toCharArray(), method.ordinal());
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
            final Route[] routes; // a route can exist for GET,POST,PUT,etc
            boolean end = false;
            
            TrieNode(char c) {
                //nodes = new SearchTrie[255];//extended ascii
                nodes = new TrieNode[128];//ascii
                content = c;
                routes = new Route[HttpMethod.values().length];
            }
            
            public void clear() {
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = null;
                }
                for (int i = 0; i < routes.length; i++) {
                    routes[i] = null;
                }
            }
            
            public Route get(HttpMethod method) /*throws Up.RouteFoundWithDifferentMethod*/ {
                // This *ought to* only be applicable if the Trie is used outside of the context of this Router
                // I.e. as an isolated library
                /*if (routes[method.ordinal()] == null) {
                    if (end) return null;//throw Up.RouteFoundWithDifferentMethod(method.name());
                }*/
                return routes[method.ordinal()];
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
    
    static class TriePathParser {
        
        // /{paramname}
        private static final char PARAM_START = '{'; 
        private static final char PARAM_END = '}'; 
        
        static TriePath parse(String originalPath) {
            if (!hasParams(originalPath))
                return new TriePath(originalPath);
            
            
            int l = originalPath.length();
            
            LinkedList<String> pn = new LinkedList<>();
            StringBuilder applicable = new StringBuilder(l);
            
            for (int i = 0; i < l; i++) {
                char c = originalPath.charAt(i);
                
                if (c == PARAM_START) {
                    // continue to end
                    int end = i;
                    while (++end < l && (originalPath.charAt(end) != PARAM_END && originalPath.charAt(end) != '/'));
                    
                    
                    pn.add(originalPath.substring(i+1, end));
                    applicable.append(RouteTrie.WILDCARD); // replace the parameter with a wildcard
                    
                    
                    //if (end >= l) end = l-1;
                    if (end < l && originalPath.charAt(end) != PARAM_END) end--; // we found '/' before '}', and we want the next char to be '/'
                    i = end; // jump
                    continue;
                    // foul
                    // throw Up.ParseError("Route seems to be errorneous -> " + originalPath);
                } else {
                    applicable.append(c);
                }
            }
            
            
            return new TriePath(originalPath, applicable.toString(), pn);
        }
        
        static boolean hasParams(String path) {
            return path.indexOf(PARAM_START) > 0;
        }

        // TODO could be a record
        static class TriePath {
            final String original;
            final String trieApplicable;
            final List<String> parameterNames;
            final boolean hasParams;
            
            TriePath(String o) {
                this(o, o, Collections.emptyList());
            }
            TriePath(String o, String w, List<String> pn) {
                original = o;
                trieApplicable = w;
                parameterNames = pn;
                hasParams = !pn.isEmpty();
            }
        }

    }

}