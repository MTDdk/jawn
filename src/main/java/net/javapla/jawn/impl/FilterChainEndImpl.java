package net.javapla.jawn.impl;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.reflection.ControllerActionInvoker;
import net.javapla.jawn.core.spi.FilterChainEnd;

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
//        long time = System.currentTimeMillis();
//        try {
            return invoker.executeAction(context);
//        } finally {
//            System.out.println("____  ControllerActionInvoker ... " + (System.currentTimeMillis()- time));
//        }
    }

    @Override
    public void after(Context context) {}
    
    @Override
    public void onException(Exception e) {}
}
