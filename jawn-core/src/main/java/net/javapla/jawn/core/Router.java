package net.javapla.jawn.core;

public interface Router {
    
    /*interface Match {
        Route route();
        void execute(Context ctx);
    }*/

    void addRoute(final Route route);
    
    Route retrieve(Context context);// throws Up.RouteMissing, Up.RouteFoundWithDifferentMethod;
    
    default void retrieveAndExecute(Context context) {
        retrieve(context).execute(context);
    }
}
