package net.javapla.jawn.core.routes;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.http.Context;

/**
 * Indicates the end of a filter chain.
 * This should always be the last filter to be called.
 * 
 * This is also the caller of the controller action.
 * 
 * @author MTD
 */
class FilterChainEnd implements FilterChain {

    private final Response response;

    //private final ControllerActionInvoker invoker;
    
    public FilterChainEnd(/*ControllerActionInvoker invoker*/) {
        //this.invoker = invoker;
        this(null);
    }
    public FilterChainEnd(Response response) {
        this.response = response;
    }
    
    @Override
    public Response before(Context context) {
        // When returning null, the ControllerActionInvoker is called instead
        return response;//invoker.executeAction(context);
    }

    @Override
    public void after(Context context) {}
    
    @Override
    public void onException(Exception e) {}
}
