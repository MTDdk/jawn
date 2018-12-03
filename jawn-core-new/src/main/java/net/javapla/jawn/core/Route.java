package net.javapla.jawn.core;


public interface Route {

    
    
    public interface Filter {
        //void handle(Request req, Response resp, Route.Chain chain);
        /**
         * Filter the request. Filters should invoke the {@link FilterChain#before(Context)}
         * method if they wish the request to proceed.
         * 
         * @param chain
         *      The filter chain
         * @param context
         *      The context
         * @return
         *      A response if anything needs to be redirected or 404'd
         */
        Result before(Route.Chain chain, Context context);
        
        /**
         * Called by framework after executing a controller.
         * 
         * <p>Response headers and the like should be added in {@linkplain #before(FilterChain, Context)}
         * or by the controller, as the response is most likely already started at this point, which
         * means that headers are already sent to the browser/caller.
         */
        void after(Route.Chain chain, Context context);
        
        /**
         * Called by framework in case there was an exception inside a controller
         *
         * @param e exception.
         */
        void onException(Route.Chain chain, Exception e);
    }
    
    interface Chain {
        /**
         * Pass the request to the next filter
         * 
         * @param context
         *          The context for the request
         */
        Result before(Context context);
        
        /**
         * Remember that you cannot effectively add headers after the response has been sent.
         * 
         * @param context
         *          The context for the request
         */
        void after(Context context);
        
        void onException(Exception e);
    }
}
