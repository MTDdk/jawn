package net.javapla.jawn.filters;

import net.javapla.jawn.Context;

public class LogRequestTimingFilter extends FilterAdapter {

    //must be threadlocal - filters are NOT thread safe!
    private static ThreadLocal<Long> time = new ThreadLocal<Long>();
    
    @Override
    public void before(Context context) {
        time.set(System.currentTimeMillis());
    }

    @Override
    public void after(Context context) {
        logger.info("Processed request in: " + (System.currentTimeMillis() - time.get() + " milliseconds, path: " + context.path() + ", method: " + context.method()));
    }

}
