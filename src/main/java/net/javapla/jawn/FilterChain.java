package net.javapla.jawn;


public interface FilterChain {

    /**
     * Pass the request to the next filter
     * 
     * @param context
     *            The context for the request
     */
    ControllerResponse before(Context context);
    
    void after(Context context);
    
    void onException(Exception e);
}
