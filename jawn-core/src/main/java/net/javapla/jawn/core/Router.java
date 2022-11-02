package net.javapla.jawn.core;

public interface Router {
    
    /*interface Match {

        void execute(Context ctx);
    }*/

    Route retrieve(HttpMethod httpMethod, String requestUri);// throws Up.RouteMissing, Up.RouteFoundWithDifferentMethod;

}
