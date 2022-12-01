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
    
    interface RoutePath extends Route.Execution {
        Route route();
        
        /**
         * Path parameters
         * @return
         */
        Map<String, String> pathParameters();
        
        @Override
        default void execute(Context ctx) {
            ((AbstractContext)ctx).routePath = this;
            Route route = route();
            
            /*try {
            
            } catch (Exception e) {
                //x = e;
            }*/
            route.exec.execute(ctx);
            
            if (route.post != null) route.post.complete(new ReadOnlyContext(ctx));
        }
    }
}
