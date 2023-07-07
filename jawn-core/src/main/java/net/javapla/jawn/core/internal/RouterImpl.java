package net.javapla.jawn.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Router;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.util.StringUtil;

final class RouterImpl implements Router {
    
    final RouteTrie trie;
    
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
    public RoutePath retrieve(final int httpMethod, final String requestUri) /*throws Up.RouteMissing, Up.RouteFoundWithDifferentMethod*/ {
        // first, take a look in the trie
        TriePath route = trie.findExact(requestUri.toCharArray(), httpMethod);
        if (route != null) return route;
            
        // try with wildcard search
        route = trie.lookForWildcard(requestUri.toCharArray(), httpMethod);
        if (route != null /*&& route.matches(requestUri)*/) {
            
            route = TriePathParser.parseRequest(requestUri, route);
            trie.insert(requestUri, route); // cache it for later fast look-up
            return route;
            
        } else {
            // The trie did not have any for us..
            return NOT_FOUND;
            
            // Have a look in the custom routes then
            //return goThroughCustom(HttpMethod.values()[httpMethod], requestUri);//, () -> Up.RouteMissing("Failed to map resource to URI: " + httpMethod.name() + " : " + requestUri));
        }
    }
    
    /*private TriePath goThroughCustom(final HttpMethod httpMethod, final String requestUri) {
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
        return NOT_FOUND;//Route.NOT_FOUND;//ctx -> ctx.resp().respond(Status.NOT_FOUND);
    }*/
    static final TriePath NOT_FOUND = TriePathParser.parse(Route.NOT_FOUND);
    
    RouterImpl compileRoutes(final List<Route> routes) throws Up.RouteAlreadyExists {
        
        for (final Route route : routes) {
            
            addRoute(route);
        }
        return this;
    }
    

    @Override
    public void addRoute(final Route route) {
        char[] chars = route.path().toCharArray();
        TriePath lookup = trie.lookForWildcard(chars, route.method().ordinal());
        if (lookup != null && route.method() != HttpMethod.HEAD && Arrays.equals(TriePathParser.parse(route).trieApplicable, lookup.trieApplicable)) {
            throw Up.RouteAlreadyExists(route + " -> " + lookup.toString());
        }
        
        TriePath triePath = TriePathParser.parse(route);
        trie.insert(triePath);
        
        /*if (route.isUrlFullyQualified()) {
            trie.insert(route.path(), route);
        } else {
            trie.insert(route.wildcardedPath(), route);
            this.routes.add(route);
        }*/
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
        
        // According to RFC3986 we have a handful of characters that are reserved
        // in the URI, so we can use these as delimiters in the trie as these ought
        // not be a part of the search
        // https://www.rfc-editor.org/rfc/rfc3986#section-2.2
        public static final char SEGMENT = '#'; // used to denote that a path segment is a variable
        //public static final char WILDCARD = '*'; // rest of the string is valid (true wildcard)
        
        private final TrieNode root;
        
        
        public RouteTrie() {
            root = new TrieNode('!');
        }
        
        public void clear() {
            root.clear();
        }
        
        public void insert(TriePath path) {
            insert(path.trieApplicable, path);
        }
        
        public void insert(String uri, TriePath route) {
            insert(uri.toCharArray(), route);
        }
        
        public synchronized void insert(final char[] input, TriePath route) {
            TrieNode current = root, child;
            for (char c : input) {
                child = current.nodes[c];
                if (child == null) {
                    child = new TrieNode(c);
                    current.nodes[c] = child;
                }
                current = child;
            }
            current.routes[route.method] = route;
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
        public TriePath findExact(final char[] arr, final HttpMethod method) {
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
        
        public TriePath findExact(final char[] arr, final int method) {
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
        
        public TriePath findExact(final CharSequence str, final HttpMethod method) {
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
         * @param path
         * @param method
         * @return
         */
        final TriePath lookForWildcard(final char[] path, int method) {
            TrieNode recursive = recursive(path, 0, root);
            
            if (recursive == null) return null;
            
            return recursive.routes[method];
        }
        
        private TrieNode recursive(char[] path, int i, TrieNode current) {
            char c;
            for (; i < path.length; i++) {
                c = path[i];
                
                if (current.nodes[c] == null) return null;
                current = current.nodes[c]; 
                
                if (c == '/' && current.nodes[SEGMENT] != null) {
                    // Fast forward to the next segment of the input
                    int index = i;
                    while(++index < path.length && path[index] != '/');
                    
                    // There were no more segments..
                    // Just return what we found
                    if (index == path.length) return current.nodes[SEGMENT];
                    
                    TrieNode node = recursive(path, index, current.nodes[SEGMENT]);
                    if (node != null) return node;
                }
                
            }
            return current;
        }
//        
//        /**
//         * Can only handle if a route starts with '/'
//         * @param arr
//         * @return
//         */
//        public final TriePath findRoute(final char[] arr, final int method) {
//            TrieNode current = root, segment = null;
//            char c;
//            for (int i = 0; i < arr.length; i++) {
//                c = arr[i];
//                
//                if (current.nodes[c] == null) { // might be a wildcard or segment
//                    
//                    if (current.nodes[SEGMENT] != null) {
//                        
//                        // We are looking at a single parameter segment.
//                        current = current.nodes[SEGMENT];
//                        
//                        
//                        // Fast forward to the next segment of the input
//                        while(++i < arr.length && arr[i] != '/');
//                        
//                        // There were no more segments..
//                        // Just return what we found
//                        if (i == arr.length) break;
//                        
//                        
//                        // Still more segments in the input, so just continue to the next char in the trie, which should be a slash '/'
//                        current = current.nodes['/'];
//                        // (might be null, but gets caught by the guard outside the ifs)
//                        
//                        
//                    /*} /*else if (current.nodes[WILDCARD] != null) {
//                        // are we at an end?
//                        if (current.nodes[WILDCARD].end) return current.nodes[WILDCARD].routes[method];*/
//                    } else {
//                        
//                        // We have nothing of the sorts already in the trie, so see if we saved a segment
//                        // way back when
//                        current = segment;
//                        // (might be null, but gets caught by the guard outside the ifs)
//                    }
//                    
//                    
//                    // We have nothing else to lookup in the trie
//                    if (current == null) return null;
//                    
//                } else {
//                    current = current.nodes[c];
//                    if (c == '/' && current.nodes[SEGMENT] != null) segment = current.nodes[SEGMENT];
//                }
//                
//            }
//            
//            if (current.routes[method] == null && segment != null) current = segment;
//            return current.routes[method];
//        }
        
        public final TriePath lookForWildcard(final String path, final HttpMethod method) {
            //return findRoute(path.toCharArray(), method.ordinal());
            return lookForWildcard(path.toCharArray(), method.ordinal());
        }
        
        final class TrieNode {
            final TrieNode[] nodes;
            final char content;
            final TriePath[] routes; // a route can exist for GET,POST,PUT,etc
            boolean end = false;
            
            TrieNode(char c) {
                //nodes = new SearchTrie[255];//extended ascii
                nodes = new TrieNode[128];//ascii
                content = c;
                routes = new TriePath[HttpMethod.values().length];
            }
            
            public void clear() {
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = null;
                }
                for (int i = 0; i < routes.length; i++) {
                    routes[i] = null;
                }
            }
            
            public TriePath get(HttpMethod method) /*throws Up.RouteFoundWithDifferentMethod*/ {
                // This *ought to* only be applicable if the Trie is used outside of the context of this Router
                // I.e. as an isolated library
                if (routes[method.ordinal()] == null) {
                    if (end) return null;//throw Up.RouteFoundWithDifferentMethod(method.name());
                }
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
    
    
    /**
     * @author MTD (github/mtddk)
     */
    static final class TriePathParser {
        
        private static final char PARAM_START = '{'; // /{paramname}
        private static final char PARAM_END = '}';
        
        static TriePath parse(Route route) {
            String originalPath = route.path();
            if (!hasParams(originalPath)) return new TriePath(route);
            
            int length = originalPath.length();
            LinkedList<String> pn = new LinkedList<>();
            StringBuilder applicable = new StringBuilder(length);
            
            // Divide into path segments and handle each segment
            StringUtil.split(originalPath.substring(1), '/', segment -> { // using .substring(1) to remove the leading '/' as this will confuse the split
                applicable.append('/'); // keeping segmentation in the resulting applicable path
                
                // segment is a parameter
                if (segment.charAt(0) == PARAM_START) {
                    int end = segment.length() - 1;
                    if (segment.charAt(end) != PARAM_END) end++;
                    
                    // add the parameter name to list
                    pn.add(segment.substring(1, end));
                    applicable.append(RouteTrie.SEGMENT); // replace the parameter with a wildcard
                } else {
                    applicable.append(segment);
                    pn.add(null); // add null to list of parameters for later quick counting/lookup
                }
            });
            
            return new TriePath(route, applicable.toString(), pn);
        }
        
        static boolean hasParams(String path) {
            return path.indexOf(PARAM_START) > 0;
        }
        
        // TODO Currently only for segmented paths and not handling if the path has a true wildcard at the end (or even middle)
        static TriePath parseRequest(String requestPath, TriePath path) {
            if (!path.hasParams) return path;
            
            HashMap<String, String> pathParams = new HashMap<>(1, 0.01f);
            
            // segment the request
            int[] index = {0};
            StringUtil.split(requestPath.substring(1), '/', segment -> {
                String param = path.segments[ index[0]++ ];
                if (param != null)
                    pathParams.put(param, segment);
            });
            
            
            return new TriePath(path, pathParams);
        }

    }
    
    // TODO could be a record
    static class TriePath extends Router.RoutePath {
        /**
         * A path/URI that can go directly into the Trie 
         * (i.e. might contain wildcards that can be handled by the Trie or simply be a static route)
         */
        final char[] trieApplicable;
        
        /**
         * The int value of the corresponding HttpMethod of the contained Route
         */
        final int method;
        
        /**
         * An array of each segment of the original URI.
         * A "segment" is the value between two forward slashes '/'.
         * A segment can either be static, e.g. /static/path, or non-static, e.g. /static/{named_path_param}.
         * 
         * All static segments are simply represented as nulls in the array,
         * and non-static segments are named, i.e. has a string value in the array.
         * 
         * For example:
         * /static/path/{named} becomes -> [null, null, "named"]
         */
        final String[] segments;
        
        /**
         * Indicates whether or not the original URI had any non-static segments.
         */
        final boolean hasParams;
        
        /**
         * Not currently used, but might be valuable for future extensions
         * where it is needed to know whether or not this particular TriePath
         * contains any sort of wildcards in the path or not.
         */
        final boolean isStatic;
        
        
        TriePath(Route r) {
            this(r, r.path(), Collections.emptyList());
        }
        TriePath(Route r, String w, List<String> pn) { // RawTriePath
            super(r);
            trieApplicable = w.toCharArray();
            method = route.method().ordinal();
            segments = pn.toArray(String[]::new);
            hasParams = !pn.isEmpty();
            isStatic = !hasParams;
        }
        TriePath(TriePath tp, Map<String, String> pp) { // ParsedTriePath / StaticTriePath
            super(tp, pp);
            trieApplicable = tp.trieApplicable;
            method = tp.method;
            segments = tp.segments; // not necessary to save the segments as this is now a static TriePath, and the has all the path parameters parsed
            hasParams = tp.hasParams;
            isStatic = true;
        }
    }

}