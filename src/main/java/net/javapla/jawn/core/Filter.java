package net.javapla.jawn.core;


public interface Filter {

    /**
     * Filter the request. Filters should invoke the filterChain.nextFilter()
     * method if they wish the request to proceed.
     * 
     * @param chain
     *            The filter chain
     * @param context
     *            The context
     */
    ControllerResponse before(FilterChain chain, Context context);
    
    /**
     * Called by framework after executing a controller
     */
    void after(FilterChain chain, Context context);
    
    /**
     * Called by framework in case there was an exception inside a controller
     *
     * @param e exception.
     */
    void onException(FilterChain chain, Exception e);
}
