package net.javapla.jawn.core.api;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.http.Context;


public interface FilterChain {

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
