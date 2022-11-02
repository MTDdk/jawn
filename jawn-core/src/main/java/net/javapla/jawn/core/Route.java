package net.javapla.jawn.core;

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
        Object handle(Context ctx) throws Up;
        
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
                        next.apply(unmodifiable, result, cause);
                        result = ctx;
                    } else {
                        next.apply(ctx, result, cause);
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
        void apply(Context ctx) throws Up;
        
        default Before then(Before next) {
            return ctx -> {
                apply(ctx);
                if (!ctx.resp().isResponseStarted()) {
                    next.apply(ctx);
                }
            };
        }
        
        default Handler then(Handler next) {
            return ctx -> {
                apply(ctx);
                if (!ctx.resp().isResponseStarted()) {
                    return next.handle(ctx);
                }
                return ctx;
            };
        }
    }
    
    interface After {
        void apply(Context ctx, Object result, Throwable cause);
        
        default After then(After next) {
            return (ctx, result, cause) -> {
                next.apply(ctx, result, cause);
                apply(ctx, result, cause);
            };
        }
    }
    
    
    static class Builder {
        
        private final HttpMethod method;
        private final String path;
        private Handler handler;
        private MediaType responseType = MediaType.PLAIN;
        private Renderer renderer;

        public Builder(HttpMethod method, String path, Handler handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
        }
        
        public Builder produces(MediaType type) {
            responseType = type;
            return this;
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
        
        public Builder filter() {
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
                    Object result = handler.handle(ctx);
                    
                    if (!ctx.resp().isResponseStarted()) {
                        
                        try {
                            byte[] rendered = renderer.render(ctx, result);
                            
                            if (rendered != null) {
                                System.out.println("Response has not been handled");
                            }
                            
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    
                }
            };
        }
    }
    
    Route NOT_FOUND = new Route.Builder(HttpMethod.GET, "/", ctx -> ctx.resp().respond(Status.NOT_FOUND)).build();
    Route METHOD_NOT_ALLOWED = new Route.Builder(HttpMethod.GET, "/", ctx -> ctx.resp().respond(Status.METHOD_NOT_ALLOWED)).build();
}

