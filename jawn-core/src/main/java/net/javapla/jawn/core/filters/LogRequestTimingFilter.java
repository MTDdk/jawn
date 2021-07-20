package net.javapla.jawn.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route.Filter;

@Singleton
public class LogRequestTimingFilter implements Filter {
    private static final String filterName = LogRequestTimingFilter.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(filterName);
    static final String X_REQUEST_PROCESSING_TIME = "X-Request-Processing-Time";

    @Override
    public void before(final Context context) {
        // filters are NOT thread safe!
        context.attribute(filterName, time());
    }

    @Override
    public void after(final Context context, final Object result) {
        context.attribute(filterName, Long.class).ifPresent(time -> {
            String processingTime = String.valueOf(time() - time);
            context.resp().header(X_REQUEST_PROCESSING_TIME, processingTime);
            
            logger.info("Processed request from [{}] in: {} milliseconds, path: {} , method: {}", context.req().remoteIp(), processingTime, context.req().fullPath(), context.req().httpMethod());
        });
    }
    
    private final long time() {
        return System.currentTimeMillis();
    }
}
