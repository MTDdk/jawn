package net.javapla.jawn.core;

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

    //private final ControllerActionInvoker invoker;
    
    public FilterChainEnd(/*ControllerActionInvoker invoker*/) {
        //this.invoker = invoker;
    }
    
    @Override
    public Response before(Context context) {
        return null;//invoker.executeAction(context);
    }

    @Override
    public void after(Context context) {}
    
    @Override
    public void onException(Exception e) {}
}
