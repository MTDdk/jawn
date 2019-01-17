package net.javapla.jawn.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route.Filter;

@Singleton
public class LogRequestTimingFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(LogRequestTimingFilter.class.getSimpleName());

    @Override
    public void before(final Context context) {
        // filters are NOT thread safe!
        context.attribute(getClass().getName(), time());
    }

    @Override
    public void after(final Context context, final Result result) {
        context.attribute(getClass().getName(), Long.class).ifPresent(time -> {
            String processingTime = String.valueOf(time() - time);
            context.resp().header("X-Request-Processing-Time", processingTime);
            logger.info("Processed request in: " + processingTime + " milliseconds, path: " + context.req().path() + ", method: " + context.req().httpMethod());
        });
    }
    
    private final long time() {
        return System.currentTimeMillis();
    }
}
