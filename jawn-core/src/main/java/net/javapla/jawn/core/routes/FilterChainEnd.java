package net.javapla.jawn.core.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.http.Context;

/**
 * Indicates the end of a filter chain.
 * This should always be the last filter to be called.
 * 
 * @author MTD
 */
class FilterChainEnd implements FilterChain {
    private final Logger log = LoggerFactory.getLogger(FilterChainEnd.class);

    
    @Override
    public Result before(Context context) {
        // When returning null, the ControllerActionInvoker is called instead
        return null;
    }

    @Override
    public void after(Context context) {
        //if (context instanceof Context.Internal2) ((Context.Internal2) context).response().end();
    }
    
    @Override
    public void onException(Exception e) {
        log.error("Filter chain broke", e);
    }
}
