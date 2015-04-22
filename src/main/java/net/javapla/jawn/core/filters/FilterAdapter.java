package net.javapla.jawn.core.filters;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.ControllerResponse;
import net.javapla.jawn.core.Filter;
import net.javapla.jawn.core.FilterChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FilterAdapter implements Filter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public ControllerResponse before(FilterChain chain, Context context) {
        before(context);
        return chain.before(context);
    }
    
    @Override
    public void after(FilterChain chain, Context context) {
        after(context);
        chain.after(context);
    }
    
    @Override
    public void onException(FilterChain chain, Exception e) {
        chain.onException(e);
    }
    
    /**
     * In case you do not want to care about the chaining
     * @param context
     */
    protected void before(Context context) {}
    
    /**
     * In case you do not want to care about the chaining
     * @param context
     */
    protected void after(Context context) {}
}
