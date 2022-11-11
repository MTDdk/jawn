package net.javapla.jawn.core;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import net.javapla.jawn.core.internal.ReadOnlyContext;

public final class Route {
    

    private final HttpMethod method;
    private final String path;
    private final Handler handler;
    private final OnComplete post;
    private final MediaType consumes;
    //private final List<MediaType> produces;
    //private final Renderer renderer;
    private final Type returnType;
    private final Execution exec;
    final Parser.ParserProvider parsers;
    
    Route(
            HttpMethod method,
            String path,
            Handler handler,
            OnComplete post,
            //List<MediaType> responseType, 
            MediaType consumes,
            //Renderer renderer,
            Type returnType, Execution exec, 
            Parser.ParserProvider parsers) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.post = post;
        //this.produces = responseType;
        this.consumes = consumes;
        //this.renderer = renderer;
        this.returnType = returnType;
        this.exec = exec; 
        this.parsers = parsers;
    }

    public boolean matches(String uri) {
        return path.equals(uri);
    }

    public HttpMethod method() {
        return method;
    }

    public String wildcardedPath() {
        return path;
    }

    public boolean isUrlFullyQualified() {
        return true;
    }

    public String path() {
        return path;
    }

    public Route.Handler handler() {
        return handler;
    }
    
    /*public List<MediaType> produces() {
        return produces;
    }*/

    public boolean consuming(MediaType type) {
        return consumes.matches(type);
    }
    
    public Type returnType() {
        return returnType;
    }
    
    public void execute(Context ctx) {
        ((AbstractContext)ctx).route = this;
            
        /*try {
        
        } catch (Exception e) {
            //x = e;
        }*/
        exec.execute(ctx);
        
        if (post != null) post.complete(new ReadOnlyContext(ctx));
    }
    

    // TODO create static route which listens on /favicon.ico
    //public static final Handler FAVICON = ctx -> ctx.resp().respond(Status.NOT_FOUND);
    
    public static interface Handler extends Serializable {
        Object handle(Context ctx) throws Exception;
        
        default Handler after(After next) {
            return ctx -> {
                Throwable cause = null;
                Object result = null;
                try {
                    result = handle(ctx);
                } catch (Throwable e) {
                    cause = e;
                    ctx.resp().status(Up.error(e).value());
                }
                
                try {
                    if (ctx.resp().isResponseStarted()) {
                        next.after(new ReadOnlyContext(ctx), result, cause);
                        result = ctx;
                    } else {
                        next.after(ctx, result, cause);
                    }
                } catch (Throwable e) {
                    result = null;
                    if (cause == null)
                        cause = e;
                    else
                        cause.addSuppressed(e);
                }
                
                if (cause == null) return result;
                else {
                    if (ctx.resp().isResponseStarted())
                        return ctx;
                    throw Up.IO(cause);
                }
            };
        }

    }
    
    public static interface Before {
        void before(Context ctx) throws Up;
        
        default Before then(Before next) {
            return ctx -> {
                before(ctx);
                if (!ctx.resp().isResponseStarted()) {
                    next.before(ctx);
                }
            };
        }
        
        default Handler then(Handler next) {
            return ctx -> {
                before(ctx);
                if (!ctx.resp().isResponseStarted()) {
                    return next.handle(ctx);
                }
                return ctx;
            };
        }
    }
    
    public static interface After {
        void after(Context ctx, Object result, Throwable cause);
        
        default After then(After next) {
            return (ctx, result, cause) -> {
                next.after(ctx, result, cause);
                after(ctx, result, cause);
            };
        }
    }
    
    public static abstract class Filter implements Before, After, OnComplete {
        @Override
        public void before(Context ctx) throws Up {}
        @Override
        public void after(Context ctx, Object result, Throwable cause) {}
        @Override
        public void complete(Context ctx) {}
    }
    
    // OnComplete
    public static interface OnComplete {
        void complete(Context ctx/*, Throwable error*/);
        
        default OnComplete then(OnComplete next) {
            return (ctx) -> {
                complete(ctx);
                next.complete(ctx);
            };
        }
    }
    
    public static interface NoResultHandler extends Handler {
        void nothing(Context ctx) throws Exception;
        
        default Object handle(Context ctx) throws Exception {
            nothing(ctx);
            return null;
        }
    }
    
    public static interface ZeroArgHandler extends Handler {
        Object zero() throws Exception;
        
        default Object handle(Context ctx) throws Exception {
            return zero();
        }
    }
    
    
    public static interface MethodHandler extends Handler {
        // Action
        Method method();
        
        // Route class
        Class<?> controller();
    }
    
    /**
     * Promise to handle all exceptions
     */
    public static interface Execution {
        void execute(Context ctx);
    }
    
    public static interface RouteBuilder {
        RouteBuilder before(Before b);
        RouteBuilder after(After a);
        RouteBuilder filter(Filter f);
        RouteBuilder postResponse(OnComplete p);
        
        RouteBuilder produces(MediaType type);
    }
    
    
    public static class Builder implements RouteBuilder {
        
        public final HttpMethod method;
        public final String path;
        public final Handler originalHandler; // action
        private Handler handler; // pipeline
        private OnComplete post;
        private MediaType responseType = MediaType.PLAIN, consumes = MediaType.WILDCARD;
        private final List<MediaType> responseTypes = new LinkedList<>();
        //private Renderer renderer;
        private Type returnType;
        //private ErrorHandler err;

        public Builder(HttpMethod method, String path, Handler handler) {
            this.method = method;
            this.path = path;
            this.originalHandler = handler;
            this.handler = handler;
            //this.responseTypes.add(responseType);
        }
        
        public Builder consumes(MediaType type) {
            this.consumes = type;
            return this;
        }
        public MediaType consumes() {
            return consumes;
        }
        
        public Builder produces(MediaType type) {
            responseType = type;
            responseTypes.add(type);
            
            // Prepend the pipeline of handlers with setting the contentType
            // of the response, when setting it to something non-default
            //before(ctx -> ctx.resp().contentType(type));
            return this;
        }
        public MediaType fallbackResponseType() {
            return responseType;
        }
        public List<MediaType> produces() {
            return responseTypes;
        }
        
        /*public Builder renderer(Renderer renderer) {
            this.renderer = renderer;
            return this;
        }*/
        /*public Renderer renderer() {
            return this.renderer;
        }*/
        
        //private After after;
        public Builder after(After after) {
            //if (this.after == null) this.after = after;
            //else this.after = this.after.then(after);
            handler = handler.after(after);
            return this;
        }
        
        //private Before before;
        public Builder before(Before before) {
            //if (this.before == null) this.before = before;
            //else this.before = before.then(this.before);
            handler = before.then(handler);
            return this;
        }
        
        public Builder filter(Filter filter) {
            before(filter);
            after(filter);
            
            if (post != null) post = post.then(filter);
            else post = filter;
            
            return this;
        }
        
        public Builder handler(Handler handler) {
            this.handler = handler;
            return this;
        }
        public Handler handler() {
            return handler;
        }
        Execution exec;
        public void execution(Execution exec) {
            this.exec = exec;
        }
        
        public Builder postResponse(OnComplete r) {
            if (post == null)
                post = r;
            else
                post = post.then(r);
            return this;
        }
        
        public Builder returnType(Type type) {
            this.returnType = type;
            return this;
        }
        public Type returnType() {
            return returnType;
        }
        
        private Parser.ParserProvider parsers;
        public Builder parsers(Parser.ParserProvider provider) {
            this.parsers = provider;
            return this;
        }
        
        public Route build() {
            return 
                new Route(
                    method,
                    path,
                    handler,
                    post,
                    //responseType,
                    //responseTypes,
                    consumes,
                    //renderer,
                    returnType,
                    exec,
                    parsers
                );
        }
    }
    
    public static final Route NOT_FOUND = new Route.Builder(HttpMethod.GET, "/", ctx -> ctx.resp().respond(Status.NOT_FOUND)).build();
    public static final Route METHOD_NOT_ALLOWED = new Route.Builder(HttpMethod.GET, "/", ctx -> ctx.resp().respond(Status.METHOD_NOT_ALLOWED)).build();
    
    /** Set the response type based on what the request asked for in the ACCEPT-header  */
    /*public static final Route.Before RESPONSE_CONTENT_TYPE = (ctx) -> {
        AbstractContext ac = (AbstractContext) ctx;
        MediaType acceptable = ac.accept();
        ctx.resp().contentType(acceptable);
    };*/
    public static final Function<List<MediaType>, Route.Before> RESPONSE_CONTENT_TYPE = (responseTypes) -> (ctx) -> {
        AbstractContext ac = (AbstractContext) ctx;
        MediaType acceptable = ac.accept(responseTypes);
        ctx.resp().contentType(acceptable);
    };
    
}

