package net.javapla.jawn.core.api;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.http.Context;


public interface Filter {

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
    Result before(FilterChain chain, Context context);
    
    /**
     * Called by framework after executing a controller.
     * 
     * <p>Response headers and the like should be added in {@linkplain #before(FilterChain, Context)}
     * or by the controller, as the response is most likely already started at this point, which
     * means that headers are already sent to the browser/caller.
     */
    void after(FilterChain chain, Context context);
    
    /**
     * Called by framework in case there was an exception inside a controller
     *
     * @param e exception.
     */
    void onException(FilterChain chain, Exception e);
}
