package net.javapla.jawn.core;

public interface Router {
    
    /*interface Match {
        Route route();
        void execute(Context ctx);
    }*/

    void addRoute(final Route route);
    
    Route retrieve(HttpMethod httpMethod, String requestUri);// throws Up.RouteMissing, Up.RouteFoundWithDifferentMethod;
    
    default void retrieveAndExecute(Context context) {
        retrieve(context.req().httpMethod(), context.req().path()).execute(context);
    }
}
