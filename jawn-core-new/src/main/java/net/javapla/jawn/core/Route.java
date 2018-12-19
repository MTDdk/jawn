package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.util.URLCodec;

public interface Route {

    
    
    public interface Chain /*extends FChain */{
        //void handle(Request req, Response resp, Route.Chain chain);
        /**
         * Filter the request. Filters should invoke the {@link FilterChain#before(Context)}
         * method if they wish the request to proceed.
         * 
         * @param chain
         *      The filter chain
         * @param context
         *      The context
         * @return
         *      A response if anything needs to be redirected or 404'd
         */
        /*default Result before(final Route.Filter chain, final Context context) {
            try {
                Result result = before(context);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                onException(e);
            }
            
            return chain.before(context);
        }*/
        
        /**
         * Called by framework after executing a controller.
         * 
         * <p>Response headers and the like should be added in {@linkplain #before(FilterChain, Context)}
         * or by the controller, as the response is most likely already started at this point, which
         * means that headers are already sent to the browser/caller.
         */
        /*default void after(final Route.Filter chain, final Context context) {
            chain.after(context);
        }*/
        
        /**
         * Called by framework in case there was an exception inside a controller
         *
         * @param e exception.
         */
        /*default void onException(Route.Chain chain, Exception e) {
            chain.onException(e);
        }*/
        
        void nextBefore(Context context);
        void nextAfter(Context context);
    }
    
    /*class FilterChain implements Chain {
        final Chain chain;
        final Filter filter;
        FilterChain(final Chain chain, final Filter filter) {
            this.chain = chain;
            this.filter = filter;
        }
    }*/
    
    interface Filter {
        static final Logger logger = LoggerFactory.getLogger(Route.Filter.class);
        
        void before(/*Route.Chain next, */Context context);
        void after(/*Route.Chain next, */Context context);
        default void onException(/*Route.Chain chain, */Exception e) {
            logger.error("Filter chain broke", e);
        }
        
        
//        /**
//         * Pass the request to the next filter
//         * 
//         * @param context
//         *          The context for the request
//         */
//        Result before(Context context);
//        
//        /**
//         * Remember that you cannot effectively add headers after the response has been sent.
//         * 
//         * @param context
//         *          The context for the request
//         */
//        void after(Context context);
//        
//        void onException(Exception e);
    }
    
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
    
    interface RouteHandler extends Route, Route.Handler {

        /**
         * @return might be null if no filters
         */
        /*FilterChain chain();
        
        void putFilterAtHead(Chain f);*/
    }
    
    class Builder {
        protected final static Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");
        /**
         * This regex matches everything in between path slashes.
         */
        final static String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]*)";
        
        private final HttpMethod method;
        private String uri;
        private Route.Handler handler;
        
        public Builder(final HttpMethod method) {
            this.method = method;
        }
        
        Builder path(final String path) {
            this.uri = (path.charAt(0) != '/') ? "/" + path : path;
            return this;
        }
        
        Builder handler(final Route.Handler handler) {
            this.handler = handler;
            return this;
        }
        
        Builder handler(final Route.ZeroArgHandler handler) {
            this.handler = handler;
            return this;
        }
        
        RouteHandler build() {
            final ArrayList<String> parameters = parseParameters(uri);
            final Pattern regex = Pattern.compile(convertRawUriToRegex(uri));
            
            return new RouteHandler() {
                //FilterChain chain;
                
                @Override
                public HttpMethod method() {
                    return method;
                }
                
                @Override
                public String path() {
                    return uri;
                }
                
                @Override
                public Result handle(final Context context) {
                    return handler.handle(context);
                }
                
                @Override
                public boolean isUrlFullyQualified() {
                    return parameters.isEmpty();
                }
                
                /*@Override
                public FilterChain chain() {
                    return chain;
                }
                
                @Override
                public void putFilterAtHead(final Chain f) {
                    chain = new FilterChain(f, chain);
                }*/
                
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
                    return uri;
                }
                
                @Override
                public int hashCode() {
                    return uri.hashCode();
                }
                
                @Override
                public boolean equals(Object obj) {
                    return uri.equals(obj.toString());
                }
                
            };
        }
        
        private static ArrayList<String> parseParameters(String uri) {
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
        private static String convertRawUriToRegex(String rawUri) {

            Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(rawUri);

            StringBuffer stringBuffer = new StringBuffer();

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
                matcher.appendReplacement(stringBuffer, namedVariablePartOfORouteReplacedWithRegex);

            }

            // .. and we append the tail to complete the stringBuffer
            matcher.appendTail(stringBuffer);

            return stringBuffer.toString();
        }
        
        private static Map<String, String> mapPathParameters(String requestUri) {
            ArrayList<String> parameters = parseParameters(requestUri);
            
            Pattern regex = Pattern.compile(convertRawUriToRegex(requestUri));
            Matcher m = regex.matcher(requestUri);
            
            return mapParametersFromPath(requestUri, parameters, m);
        }
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
