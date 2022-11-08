package net.javapla.jawn.core;

public interface Router {
    
    /*interface Match {
        Route route();
        void execute(Context ctx);
    }*/

    void addRoute(final Route route);
    
    Route retrieve(final int httpMethod, final String path);
    
    default Route retrieve(final HttpMethod httpMethod, final String path) {
        return retrieve(httpMethod.ordinal(), path);
    }
    
    /*default void retrieveAndExecute(Context context) {
        //retrieve(context).execute(context);
        //((Route.RouteImpl)retrieve(context.req().httpMethod(), context.req().path())).exec.execute(context);
    }*/
}
