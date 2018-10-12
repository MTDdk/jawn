package net.javapla.jawn.core.filters;

import com.google.inject.Singleton;

import net.javapla.jawn.core.api.FilterAdapter;
import net.javapla.jawn.core.http.Context;

@Singleton
public class LogRequestTimingFilter extends FilterAdapter {

    @Override
    public void before(Context context) {
        // filters are NOT thread safe!
        context.setAttribute(getClass().getName(), time());
    }

    @Override
    public void after(Context context) {
        Long time = context.getAttribute(getClass().getName(), Long.class);
        if (time != null) {
            String processingTime = String.valueOf(time() - time);
            context.setHeader("X-Request-Processing-Time", processingTime);
            logger.info("Processed request in: " + processingTime + " milliseconds, path: " + context.path() + ", method: " + context.httpMethod());
        }
    }
    
    private final long time() {
        return System.currentTimeMillis();
    }

}
