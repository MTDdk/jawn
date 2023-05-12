package net.javapla.jawn.core;

import java.util.Map;

import net.javapla.jawn.core.internal.ReadOnlyContext;

public interface Router {
    
    /*interface Match {
        Route route();
        void execute(Context ctx);
    }*/

    void addRoute(final Route route);
    
    RoutePath retrieve(final int httpMethod, final String path);
    
    default RoutePath retrieve(final HttpMethod httpMethod, final String path) {
        return retrieve(httpMethod.ordinal(), path);
    }
    
    /*default void retrieveAndExecute(Context context) {
        //retrieve(context).execute(context);
        //((Route.RouteImpl)retrieve(context.req().httpMethod(), context.req().path())).exec.execute(context);
    }*/
    
//    interface RoutePath extends Route.Execution {
//        Route route();
//        
//        /**
//         * Path parameters
//         * @return
//         */
//        Map<String, String> pathParameters();
//        
//        @Override
//        default void execute(Context ctx) {
//            ((AbstractContext)ctx).routePath = this;
//            route().execute(ctx);
//        }
//    }

    static abstract class RoutePath implements Route.Execution {
        
        public final Route route;
        
        /**
         * If the original route contains path parameters and this is a parsed RoutePath,
         * this map will contain the "path parameter name" -> "concrete value in the URI" pairs.
         * 
         * Otherwise map is null.
         */
        public final Map<String, String> pathParameters;
        
        
        
//        private final Route.Execution exec;
//        private final Route.OnComplete post;
        
        public RoutePath(Route route) {
            this.route = route;
            this.pathParameters = null;
            
//            exec = route.exec;
//            post = route.post;
        }
        public RoutePath(RoutePath p, Map<String, String> pathParameters) {
            this.route = p.route;
            this.pathParameters = pathParameters;
            
//            exec = route.exec;
//            post = route.post;
        }
        
        // 235
        /*@Override
        public void execute(Context ctx) {
            ((AbstractContext)ctx).routePath = this;
            exec.execute(ctx);
            if (post != null) post.complete(new ReadOnlyContext(ctx));
        }*/
        // 239
        /*@Override
        public void execute(Context ctx) {
            ((AbstractContext)ctx).routePath = this;
            route.execute(ctx);
        }*/
        // 246
        @Override
        public void execute(Context ctx) {
            ((AbstractContext)ctx).routePath = this;
            route.exec.execute(ctx);
            if (route.post != null) {
                try {
                    route.post.complete(new ReadOnlyContext(ctx));
                } catch (Exception e) {
                    ctx.error("When executing 'complete'", e);
                }
            }
        }
        
        @Override
        public String toString() {
            return route + " " + pathParameters;
        }
    }
}
