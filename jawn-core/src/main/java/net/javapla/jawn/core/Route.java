package net.javapla.jawn.core;

import java.lang.reflect.Method;

import net.javapla.jawn.core.internal.ReadOnlyContext;

public interface Route {
    

    boolean matches(String requestUri);

    HttpMethod method();

    String wildcardedPath();

    boolean isUrlFullyQualified();

    String path();
    
    
    Handler handler();
    
    MediaType produces();
    
    void execute(Context ctx);

    // TODO create static route which listens on /favicon.ico
    //public static final Handler FAVICON = ctx -> ctx.resp().respond(Status.NOT_FOUND);
    
    interface Handler {
        Object handle(Context ctx) throws Exception;
        
        default Handler after(After next) {
            return ctx -> {
                Throwable cause = null;
                Object result = null;
                try {
                    result = handle(ctx);
                } catch (Throwable e) {
                    cause = e;
                    ctx.resp().status(Up.error(e));
                }
                
                try {
                    if (ctx.resp().isResponseStarted()) {
                        // TODO ought to be unmodifiable context
                        Context unmodifiable = ctx;
                        next.after(unmodifiable, result, cause);
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
    
    interface Before {
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
    
    interface After {
        void after(Context ctx, Object result, Throwable cause);
        
        default After then(After next) {
            return (ctx, result, cause) -> {
                next.after(ctx, result, cause);
                after(ctx, result, cause);
            };
        }
    }
    
    interface Filter extends Before, After, PostResponse {
        
        /*default Handler apply(Handler next) {
            return ctx -> ()
        }*/
        
        // PostResponse
        @Override
        default void onComplete(Context ctx, Throwable error) {}
    }
    
    // OnComplete
    interface PostResponse {
        void onComplete(Context ctx, Throwable error);
        
        default PostResponse then(PostResponse next) {
            return (ctx, error) -> {
                onComplete(ctx, error);
                next.onComplete(ctx, error);
            };
        }
    }
    
    interface NoResultHandler extends Handler {
        void nothing(Context ctx) throws Exception;
        
        default Object handle(Context ctx) throws Exception {
            nothing(ctx);
            return Status.OK;
        }
    }
    
    
    interface MethodHandler extends Handler {
        // Action
        Method method();
        
        // Route calss
        Class<?> controller();
    }
    
    interface RouteBuilder {
        RouteBuilder before(Before b);
        RouteBuilder after(After a);
        RouteBuilder filter(Filter f);
        RouteBuilder postResponse(PostResponse p);
    }
    
    
    static class Builder implements RouteBuilder {
        
        private final HttpMethod method;
        private final String path;
        private Handler handler;
        private PostResponse post;
        private MediaType responseType = MediaType.PLAIN;
        private Renderer renderer;
        //private ErrorHandler err;

        public Builder(HttpMethod method, String path, Handler handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
        }
        
        public Builder produces(MediaType type) {
            responseType = type;
            return this;
        }
        public MediaType produces() {
            return responseType;
        }
        
        public Builder renderer(Renderer renderer) {
            this.renderer = renderer;
            return this;
        }
        
        public Builder after(After after) {
            handler = handler.after(after);
            return this;
        }
        
        public Builder before(Before before) {
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
        
        public Builder postResponse(PostResponse r) {
            if (post == null)
                post = r;
            else
                post = post.then(r);
            return this;
        }
        
        public Route build() {
            return new Route() {

                @Override
                public boolean matches(String requestUri) {
                    return false;
                }

                @Override
                public HttpMethod method() {
                    return method;
                }

                @Override
                public String wildcardedPath() {
                    return path;
                }

                @Override
                public boolean isUrlFullyQualified() {
                    return true;
                }

                @Override
                public String path() {
                    return path;
                }

                @Override
                public Route.Handler handler() {
                    return handler;
                }
                
                @Override
                public MediaType produces() {
                    return responseType;
                }
                
                
                
                @Override
                public void execute(Context ctx) {
                    Exception x = null;
                        
                    try {
                        Object result = handler.handle(ctx);
                    
                        if (!ctx.resp().isResponseStarted() && result != ctx) {
                        
                            byte[] rendered = renderer.render(ctx, result);
                            
                            if (rendered != null) {
                                System.out.println("Response has not been handled");
                            }
                            
                        }
                    } catch (Exception e) {
                        x = e;
                    }
                    
                    if (post != null) post.onComplete(new ReadOnlyContext(ctx), x);
                }
            };
        }
    }
    
    Route NOT_FOUND = new Route.Builder(HttpMethod.GET, "/", ctx -> ctx.resp().respond(Status.NOT_FOUND)).build();
    Route METHOD_NOT_ALLOWED = new Route.Builder(HttpMethod.GET, "/", ctx -> ctx.resp().respond(Status.METHOD_NOT_ALLOWED)).build();
}

