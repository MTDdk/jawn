package net.javapla.jawn.core;

import net.javapla.jawn.core.api.FilterChainEnd;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.reflection.ControllerActionInvoker;

import com.google.inject.Inject;

/**
 * Indicates the end of a filter chain.
 * This should always be the last filter to be called.
 * 
 * This is also the caller of the controller action.
 * 
 * @author MTD
 */
class FilterChainEndImpl implements FilterChainEnd {

    private final ControllerActionInvoker invoker;
    
    @Inject
    public FilterChainEndImpl(ControllerActionInvoker invoker) {
        this.invoker = invoker;
    }
    
    @Override
    public Response before(Context context) {
        return invoker.executeAction(context);
    }

    @Override
    public void after(Context context) {}
    
    @Override
    public void onException(Exception e) {}
}
