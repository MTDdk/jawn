package net.javapla.jawn.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.javapla.jawn.core.internal.renderers.JsonRendererEngine;
import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;
import net.javapla.jawn.core.util.URLCodec;

public interface Route {
    
//    @FunctionalInterface
//    interface Chain /*extends Handler*/ {// TODO the next() and handle() is quite confusing when used in filters. Perhaps ditch one of these interfaces
//        //Result next(Context context);
//        Object next(Context context);
//        
//        /*default Result handle(Context context) {
//            return next(context);
//        }*/
//    }
    
    interface Filter extends Before, After { }
    
    @FunctionalInterface
    interface Before {
        void before(Context context);
        
        default Before then(Before next) {
            return ctx -> {
                before(ctx);
                if (!ctx.resp().committed()) {
                    next.before(ctx);
                }
            };
        }
        
        default Handler then(Handler next) {
            return ctx -> {
                before(ctx);
                if (!ctx.resp().committed()) {
                    next.handle(ctx);
                }
                return next;
            };
        }
        
        //Result before(Context context, Chain chain);
        //Object before(Context context, Chain chain);
        
        /*default Before then(Before next) {
            return (ctx, handler) -> {
                return before(ctx, c -> next.before(c, handler));
            };
        }
        
        default Handler then(Handler next) {
            return ctx -> {
                return before(ctx, next::handle);
            };
        }*/
    }
    
    @FunctionalInterface
    interface Handler {
        Object/*Result*/ handle(Context context);
        
        default Handler then(After after) {
            return ctx -> {
                after.after(ctx, handle(ctx));
                return this;
            };
        }
    }
    
    /**
     * Currently, this is called after the {@link Result} has been calculated (either by {@link Handler} or overridden by {@link Before},
     * and <b>not</b> after the entire response has been sent.
     * 
     * <p>This pattern makes it possible to still make changes to the result before executing by a {@link RendererEngine}.
     * <br>E.g.: manipulating headers.
     */
    @FunctionalInterface
    interface After {
        //Result after(final Context context, final Result result);
        void after(final Context context, final Object result);
        
        default After then(After next) {
            return (ctx, result) -> {
                after(ctx, result);
                next.after(ctx, result);
            };
        }
    }
    
    /**
     * Allows for log and cleanup a request. It will be invoked after we send a response.
     * 
     * You are NOT allowed to modify the request and response objects. The <code>cause</code> is an
     * {@link Optional} with a {@link Throwable} useful to identify problems.
     *
     * The goal of the <code>complete</code> handler is to probably cleanup request object and log
     * responses.
     */
    interface PostResponse {
        //TODO not implemented, yet
        void handle(Context context, Optional<Throwable> cause);
    }
    
    interface MethodHandler extends Handler {
        
        // Action
        Method method();
        
        // Controller
        Class<?> routeClass();
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
    
    /**
     * Public part of adding filters to routes
     */
    interface Filtering {

        Filtering filter(final Filter filter);
        Filtering filter(final Class<?> filter);

        Filtering before(final Before handler);
        /*default Filtering before(final Handler handler) {
            return before((c,ch) -> handler.handle(c));
        }
        default Filtering before(final Runnable handler) {
            return before((c,ch) -> {handler.run(); return ch.next(c);});
        }
        default Filtering before(final Supplier<Result> handler) {
            return before((c,ch) -> handler.get());
        }
        default Filtering before(final Result result) {
            return before((c,ch) -> result);
        }*/
        default Filtering before(Class<?> filter) {
            return filter(filter);
        }

        Filtering after(final After handler);

        /*default Filtering after(final Runnable handler) {
            return after((c,r) -> {handler.run();return r;});
        }
        default Filtering after(final Result result) {
            return after((c,r) -> result);
        }*/
        default Filtering after(Class<?> filter) {
            return filter(filter);
        }
    }
    
    interface Rendering {
        Rendering produces(final MediaType type);
    }
    
    interface Builder {

        Builder produces(MediaType type);


        Builder after(After handler);

        Builder before(Before handler);

        Builder filter(Filter filter);
        Builder filter(Object item);

        Builder renderer(RendererEngine renderer);
        
    }
    
    public static final Route.Handler NOT_FOUND = ctx -> Status.NOT_FOUND;
    
    
    static final RendererEngine JASON = new JsonRendererEngine();
    final class BuilderImpl implements Route.Builder {
        private final static Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");
        /**
         * This regex matches everything in between path slashes.
         */
        private final static String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]+)";
        
        private final HttpMethod method;
        private String uri;
        private Handler handler;
        private RendererEngine renderer = JASON;
        private MediaType produces = MediaType.PLAIN, consumes;
        private final LinkedList<Route.Before> before = new LinkedList<>();
        private final LinkedList<Route.Before> globalBefore = new LinkedList<>();
        private final LinkedList<Route.After> after = new LinkedList<>();
        private final LinkedList<Route.After> globalAfter = new LinkedList<>();
        
        public BuilderImpl(final HttpMethod method, final String path, final Handler handler) {
            this.method = method;
            this.handler = handler;
            
            if (path == null) throw new NullPointerException("Path is null");
            if (path.isEmpty()) throw new IllegalArgumentException("Path is empty");
            
            this.uri = (path.charAt(0) != '/') ? "/" + path : path;
        }
        
        /*public Builder path(final String path) { // path pattern
            if (path == null) throw new NullPointerException("Path is null");
            if (path.isEmpty()) throw new IllegalArgumentException("Path is empty");
            
            this.uri = (path.charAt(0) != '/') ? "/" + path : path;
            return this;
        }
        
        public Builder handler(final Handler handler) {
            this.handler = handler;
            return this;
        }*/
        
        /*public Builder handler(final ZeroArgHandler handler) {
            this.handler = handler;
            return this;
        }*/
        
        @Override
        public Builder produces(final MediaType type) {
            if (type != null) {
                produces = type;
            }
            return this;
        }

        @Override
        public Builder renderer(final RendererEngine renderer) {
            this.renderer = renderer;
            return this;
        }
        
        @Override
        public Builder filter(final Filter filter) {
            this.before.add(filter);
            this.after.addFirst(filter);
            return this;
        }
        
        @Override
        public Builder before(final Before handler) {
            this.before.add(handler);
            return this;
        }
        
        @Override
        public Builder after(final After handler) {
            this.after.add(handler);
            return this;
        }

        Builder globalFilter(final Filter handler) {
            this.globalBefore.add(handler);
            this.globalAfter.addFirst(handler);
            return this;
        }
        
        Builder globalBefore(final Before handler) {
            this.globalBefore.add(handler);
            return this;
        }
        
        Builder globalAfter(final After handler) {
            this.globalAfter.add(handler);
            return this;
        }

        @Override
        public Builder filter(final Object item) {
            if (item instanceof Filter) { //filter is instanceof Before and After, so this has to be first
                filter((Filter) item);
            } else if (item instanceof After) {
                after((After) item);
            } else if (item instanceof Before) {
                before((Before) item);
            }
            return this;
        }
        
        Builder globalFilter(final Object item) {
            if (item instanceof Filter) { //filter is instanceof Before and After, so this has to be first
                globalFilter((Filter) item);
            } else if (item instanceof After) {
                globalAfter((After) item);
            } else if (item instanceof Before) {
                globalBefore((Before) item);
            }
            return this;
        }
        
        /*private Handler _befores(final Handler handler, final Before[] befores) {
            final Handler h;
            if (befores != null) {
                Before before = befores[0];
                for (int i = 1; i < befores.length; i++) {
                    before = before.then(befores[i]);
                }
                h = before.then(handler);
            } else {
                h = handler;
            }
            
            return h.then((ctx, r) -> {if (r == null) throw new Up.BadResult("The execution of the route itself rendered no result"); return r;});
        }*/

        // build pipeline
        private Handler _build(final Handler handler, final Before[] befores, final After[] afters) {
            if (handler == null) return ctx -> { throw new Up.BadResult("The execution of the route itself rendered no result"); };
            
            
            final Handler h;
            if (befores != null) {
                Before b = befores[0];
                for (int i = 1; i < befores.length; i++) b = b.then(befores[i]);
                h = b.then(handler);
            } else {
                h = handler;
            }
            
            
            //final Handler h = _befores(handler, befores);
            
            if (afters == null) return h;
            
            After after = afters[0];
            for (int i = 1; i < afters.length; i++) {
                after = after.then(afters[i]);
            }
            
            return h.then(after);
            
//            final After a = after;
//            return ctx -> a
//                .then((c, r) -> {if (r == null) throw new Up.BadResult("A ("+ Route.After.class.getSimpleName() +") filter rendered a 'null' result"); return r;})
//                .after(ctx, h.handle(ctx));
        }
        
        public Route build(final RendererEngineOrchestrator engines) {
            if (uri == null) throw new NullPointerException("Path is null");
            
            return new Route() {
                private final ArrayList<String> parameters = parseParameters(uri);
                private final Pattern regex = Pattern.compile(convertRawUriToRegex(uri));
                
                private final Before[] befores = before.isEmpty() && globalBefore.isEmpty() ? null : Stream.concat(globalBefore.stream(), before.stream()).toArray(Before[]::new);
                private final After[] afters = after.isEmpty() && globalAfter.isEmpty() ? null : Stream.concat(after.stream(), globalAfter.stream()).toArray(After[]::new);
                
                private final Handler routehandler = _build(handler, befores, afters);
                
                private final RendererEngine r = engines.get(produces);
                
                @Override
                public HttpMethod method() {
                    return method;
                }
                
                @Override
                public String path() {
                    return uri;
                }
                
                @Override
                public Object handle(final Context context) {
                    return routehandler.handle(context);
                }
                
                public void h(final Context context) {
                    try {
                        //renderer.invoke(context, ((Result)routehandler.handle(context)).renderable);
                        r.invoke(context, routehandler.handle(context));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
                @Override
                public boolean isUrlFullyQualified() {
                    return parameters.isEmpty();
                }
                
                @Override
                public String wildcardedPath() {
                    return convertRawUriToWildcard(uri);
                }
                
                @Override
                public Before[] before() {
                    return befores;
                }
                
                @Override
                public After[] after() {
                    return afters;
                }
                
                @Override
                public MediaType produces() {
                    return produces;
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
            ArrayList<String> params = new ArrayList<>(2);
            
            Matcher m = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(uri);
        
            while (m.find()) {
                // group(1) is the name of the group. Must be always there...
                // "/assets/{file}" and "/assets/{file: [a-zA-Z][a-zA-Z_0-9]}" 
                // will return "file".
                params.add(m.group(1));
            }
            params.trimToSize();
            
            return params;
        }
        
        /**
         * Gets a raw uri like "/{name}/id/*" and returns "/([^/]+)/id/.*"
         * 
         * Used to be "/([^/]*)/id/.*", but that made a path like "/{name}"
         * be matched to root, "/".
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
        
        private static String convertRawUriToWildcard(final String rawUri) {
            Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(rawUri);

            StringBuilder stringBuilder = new StringBuilder();

            while (matcher.find()) {
                matcher.appendReplacement(stringBuilder, "*"); // replace with simple wildcard
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
    }
    
    //Result handle(Context context);
    Object handle(Context context);
    void h(Context context);

    /**
     * @return Current HTTP method.
     */
    HttpMethod method();
    
    Before[] before();
    
    After[] after();
    
    /**
     * @return Current request path.
     */
    String path();
    
    MediaType produces();

    boolean isUrlFullyQualified();
    
    String wildcardedPath();
    
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
