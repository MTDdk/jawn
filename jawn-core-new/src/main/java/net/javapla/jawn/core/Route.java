package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.javapla.jawn.core.util.URLCodec;

public interface Route {
    
    
    public static class Chain {
        
        
        Result next(Context context) {
            
            return null;
        }
    }

    interface Filter extends Before, After {
//        void handle(Context context, Route.Chain chain);
    }
    /*interface VoidFilter extends Filter {
        @Override
        default Optional<Result> before(Context context) {
            before();
            return Optional.empty();
        }
        
        @Override
        default Result after(Context context, Result result) {
            return result;
        }
        
        void before();
        void after();
    }*/
    
    interface Before /*extends Filter*/ /*extends Handler*/ {
        /*@Override
        default Result handle(final Context context) {
            before(context);
            return null;
        }*/
        /*@Override
        default void handle(Context context, Chain chain) {
            before(context);
            chain.next(context);
        }*/
        
        /**
         * Execute the filter
         * 
         * @param context
         *          The context for the request
         */
        void before(Context context, Chain chain);
    }
    
    
    interface After {
        Result after(final Context context, final Result result);
    }
    /*interface VoidAfter extends After {
        @Override
        default Result after(Context context, Result result) {
            after(context);
            return result;
        }
        void after(Context context);
    }*/
    
    @FunctionalInterface
    interface Handler {
        Result handle(Context context);
    }
    
    @FunctionalInterface
    interface  ZeroArgHandler extends Handler {
        @Override
        default Result handle(Context context) {
            return handle();
        }
        
        // could also be just returning Object, and always assume status 200 type HTML
        Result handle();
    }
    
    interface RouteHandler extends Route, Route.Handler {}
    
    /**
     * Public part of the Route.Builder
     */
    interface Filtering<T> {//Perhaps called RouteBuilder ?

        T filter(final Filter filter);

        //T before(final Handler handler);
        /*default */T before(final Before handler)/* {
            return before((Handler) handler);
        }*/;
        /*default T before(final Runnable handler) {
            return before(c -> {handler.run();return null;});
        }*/
        /*default T before(final Supplier<Result> handler) {
            return before(c -> {return handler.get();});
        }*/
        /*default T before(final Result result) {
            return before(c -> Optional.of(result));
        }*/

        T after(final After handler);
        default T after(final Runnable handler) {
            return after((c,r) -> {handler.run();return r;});
        }
    }
    
    final class Builder implements Filtering<Builder> {
        private final static Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");
        /**
         * This regex matches everything in between path slashes.
         */
        private final static String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]*)";
        
        private final HttpMethod method;
        private String uri;
        private Route.Handler handler;
        private LinkedList<Route.Before> before = new LinkedList<>();
        private LinkedList<Route.Before> globalBefore = new LinkedList<>();
        private LinkedList<Route.After> after = new LinkedList<>();
        private LinkedList<Route.After> globalAfter = new LinkedList<>();
        
        public Builder(final HttpMethod method) {
            this.method = method;
        }
        
        public Builder path(final String path) {
            if (path == null) throw new NullPointerException("Path is null");
            if (path.isEmpty()) throw new IllegalArgumentException("Path is empty");
            
            this.uri = (path.charAt(0) != '/') ? "/" + path : path;
            return this;
        }
        
        public Builder handler(final Route.Handler handler) {
            this.handler = handler;
            return this;
        }
        
        public Builder handler(final Route.ZeroArgHandler handler) {
            this.handler = handler;
            return this;
        }
        
        @Override
        public Builder filter(final Filter filter) {
            this.before.add(filter);
            this.after.addFirst(filter);
            return this;
        }
        
        /*public Builder filter(final Class<? extends Filter> filter) {
            // for this to work, the Route.Builder needs an Injector at some point
            // - perhaps we do not want this, but if we do, then this method should be
            // completely isolated to Jawn/Bootstrap
        }*/
        
        @Override
        public Builder before(final Route.Before handler) {
            this.before.add(handler);
            return this;
        }
        
        @Override
        public Builder after(final Route.After handler) {
            this.after.add(handler);
            return this;
        }
        
        void globalFilter(final Route.Filter handler) {
            this.globalBefore.add(handler);
            this.globalAfter.addFirst(handler);
        }
        void globalBefore(final Route.Before handler) {
            this.globalBefore.add(handler);
        }
        /*void globalBefore(final Route.Handler handler) {
            this.globalBefore.add(handler::handle);
        }*/
        void globalAfter(final Route.After handler) {
            this.globalAfter.add(handler);
        }

        public RouteHandler build() {
            return new RouteHandler() {
                
                private final Route.Handler routehandler = handler;
                private final ArrayList<String> parameters = parseParameters(uri);
                private final Pattern regex = Pattern.compile(convertRawUriToRegex(uri));
                
                private final Handler[] befores = before.isEmpty() && globalBefore.isEmpty() ? null : Stream.concat(globalBefore.stream(), before.stream()).toArray(Handler[]::new);
                private final After[] afters = after.isEmpty() && globalAfter.isEmpty() ? null : Stream.concat(after.stream(), globalAfter.stream()).toArray(After[]::new);
                
                @Override
                public HttpMethod method() {
                    return method;
                }
                
                @Override
                public String path() {
                    return uri;
                }
                
                @Override
                public Result handle(final Context context) /*throws Exception*/ {
                    Result result = null;
                    int i = 0;
                    
                    // Before filters
                    if (befores != null) {
                        do {
                            /*result = */befores[i].handle(context);
                        } while (/*result == null &&*/ ++i < befores.length);
                    }
                    
                    // execute
                    if (result == null) {
                        result = routehandler.handle(context);
                    }
                    
                    // After filters
                    if (afters != null) {
                        for (i = 0; i < afters.length; i++) {
                            result = afters[i].after(context, result);
                        }
                    }
                    
                    return result;
                }
                
                @Override
                public boolean isUrlFullyQualified() {
                    return parameters.isEmpty();
                }
                
                @Override
                public Before[] before() {
                    return (Before[]) befores;
                }
                
                @Override
                public After[] after() {
                    return afters;
                }
                
                @Override
                public boolean matches(String requestUri) {
                    Matcher matcher = regex.matcher(requestUri);
                    return matcher.matches();
                }
                
                @Override
                public Map<String, String> getPathParametersEncoded(String requestUri) {
                    Matcher m = regex.matcher(requestUri);
                    return mapParametersFromPath(requestUri, parameters, m);
                }
                
                @Override
                public String toString() {
                    return method + uri;
                }
                
                @Override
                public int hashCode() {
                    return toString().hashCode();
                }
                
                @Override
                public boolean equals(Object obj) {
                    return toString().equals(obj.toString());
                }

            };
        }
        
        private static ArrayList<String> parseParameters(final String uri) {
            ArrayList<String> params = new ArrayList<>();
            
            Matcher m = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(uri);
        
            while (m.find()) {
                // group(1) is the name of the group. Must be always there...
                // "/assets/{file}" and "/assets/{file: [a-zA-Z][a-zA-Z_0-9]}" 
                // will return file.
                params.add(m.group(1));
            }
            
            return params;
        }
        
        /**
         * Gets a raw uri like "/{name}/id/*" and returns "/([^/]*)/id/*."
         *
         * Also handles regular expressions if defined inside routes:
         * For instance "/users/{username: [a-zA-Z][a-zA-Z_0-9]}" becomes
         * "/users/([a-zA-Z][a-zA-Z_0-9])"
         *
         * @return The converted regex with default matching regex - or the regex
         *          specified by the user.
         */
        private static String convertRawUriToRegex(final String rawUri) {

            Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(rawUri);

            StringBuilder stringBuilder = new StringBuilder();

            while (matcher.find()) {

                // By convention group 3 is the regex if provided by the user.
                // If it is not provided by the user the group 3 is null.
                String namedVariablePartOfRoute = matcher.group(3);
                String namedVariablePartOfORouteReplacedWithRegex;
                
                if (namedVariablePartOfRoute != null) {
                    // we convert that into a regex matcher group itself
                    namedVariablePartOfORouteReplacedWithRegex 
                        = "(" + Matcher.quoteReplacement(namedVariablePartOfRoute) + ")";
                } else {
                    // we convert that into the default namedVariablePartOfRoute regex group
                    namedVariablePartOfORouteReplacedWithRegex 
                        = VARIABLE_ROUTES_DEFAULT_REGEX;
                }
                // we replace the current namedVariablePartOfRoute group
                matcher.appendReplacement(stringBuilder, namedVariablePartOfORouteReplacedWithRegex);

            }

            // .. and we append the tail to complete the stringBuffer
            matcher.appendTail(stringBuilder);

            return stringBuilder.toString();
        }
        
        /*private static Map<String, String> mapPathParameters(String requestUri) {
            ArrayList<String> parameters = parseParameters(requestUri);
            
            Pattern regex = Pattern.compile(convertRawUriToRegex(requestUri));
            Matcher m = regex.matcher(requestUri);
            
            return mapParametersFromPath(requestUri, parameters, m);
        }*/
        private final static HashMap<String, String> mapParametersFromPath(String requestUri, ArrayList<String> parameters, Matcher m) {
            HashMap<String, String> map = new HashMap<>();
            if (m.matches()) {
                for (int i = 1; i < m.groupCount() + 1; i++) {
                    map.put(parameters.get(i - 1), m.group(i));
                }
            }
            return map;
        }
        
    }
    
    /**
     * @return Current HTTP method.
     */
    HttpMethod method();
    
    Before[] before();

    After[] after();

    boolean isUrlFullyQualified();

    /**
     * @return Current request path.
     */
    String path();
    
    /**
     * Matches /index to /index or /person/1 to /person/{id}
     *
     * @return True if the actual route matches a raw route. False if not.
     */
    boolean matches(String requestUri);
    
    /**
     * This method does not do any decoding / encoding.
     *
     * If you want to decode you have to do it yourself.
     *
     * {@linkplain URLCodec}
     *
     * @param requestUri The whole encoded uri.
     * @return A map with all parameters of that uri. Encoded in => encoded out.
     */
    Map<String, String> getPathParametersEncoded(String requestUri);
}
