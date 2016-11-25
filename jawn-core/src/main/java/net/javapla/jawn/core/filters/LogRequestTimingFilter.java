package net.javapla.jawn.core.filters;

import net.javapla.jawn.core.api.FilterAdapter;
import net.javapla.jawn.core.http.Context;

public class LogRequestTimingFilter extends FilterAdapter {

    // must be threadlocal - filters are NOT thread safe!
    private static ThreadLocal<Long> time = new ThreadLocal<Long>();

    @Override
    public void before(Context context) {
        time.set(System.currentTimeMillis());
    }

    @Override
    public void after(Context context) {
        if (time.get() != null) {
            String processingTime = String.valueOf(System.currentTimeMillis() - time.get());
            context.addResponseHeader("X-Request-Processing-Time", processingTime);
            logger.info("Processed request in: " + processingTime + " milliseconds, path: " + context.path() + ", method: " + context.httpMethod());
        }
    }

}
