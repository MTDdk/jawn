package net.javapla.jawn.core.routes;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.http.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InvokerFilterChainEnd implements FilterChain {

//    private final ActionInvoker invoker;

    @Inject
    public InvokerFilterChainEnd(/*ActionInvoker invoker*/) {
//        this.invoker = invoker;
    }
    
    @Override
    public Response before(Context context) {
//        return invoker.executeAction(context);
        return null;
    }

    @Override
    public void after(Context context) {}

    @Override
    public void onException(Exception e) {}

}
