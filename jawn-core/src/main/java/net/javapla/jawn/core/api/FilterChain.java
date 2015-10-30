package net.javapla.jawn.core.api;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;


public interface FilterChain {

    /**
     * Pass the request to the next filter
     * 
     * @param context
     *            The context for the request
     */
    Response before(Context context);
    
    void after(Context context);
    
    void onException(Exception e);
}
