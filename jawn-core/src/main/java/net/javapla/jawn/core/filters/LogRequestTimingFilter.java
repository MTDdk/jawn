package net.javapla.jawn.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route.Chain;
import net.javapla.jawn.core.Route.Filter;

@Singleton
public class LogRequestTimingFilter implements Filter {
    private final static String filterName = LogRequestTimingFilter.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(filterName);

    @Override
    public Result before(final Context context, final Chain chain) {
        // filters are NOT thread safe!
        context.attribute(filterName, time());
        
        return chain.next(context);
    }

    @Override
    public Result after(final Context context, final Result result) {
        context.attribute(filterName, Long.class).ifPresent(time -> {
            String processingTime = String.valueOf(time() - time);
            context.resp().header("X-Request-Processing-Time", processingTime);
            
            logger.info("Processed request in: {} milliseconds, path: {} , method: {}", processingTime, context.req().fullPath(), context.req().httpMethod());
        });
        
        return result;
    }
    
    private final long time() {
        return System.currentTimeMillis();
    }
}
