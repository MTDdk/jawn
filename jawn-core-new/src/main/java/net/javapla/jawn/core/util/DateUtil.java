package net.javapla.jawn.core.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.javapla.jawn.core.Context;

/**
 * <p>Borrowed with minor alterations from Undertow Server
 * <p>io.undertow.util.DateUtils
 * <p>By Stuart Douglas
 * <p>
 * <p>Some methods have been omitted
 */
public abstract class DateUtil {

    private static final ZoneId GMT_ZONE = ZoneId.of("GMT");

    /**
     * This is the most common date format, which is why we cache it.
     */
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    private static final AtomicReference<String> cachedDateString = new AtomicReference<>();

    /**
     * 
     */
    private static final DateTimeFormatter RFC1123_PATTERN_FORMAT = DateTimeFormatter
        .ofPattern(RFC1123_PATTERN, Locale.ENGLISH)
        .withZone(GMT_ZONE);

    /**
     * Converts a date to a format suitable for use in a HTTP request
     *
     * @param time The time / date
     * @return The RFC-1123 formatted date
     */
    public static String toDateString(final Temporal time) {
        return RFC1123_PATTERN_FORMAT.format(time);
    }
    
    public static void addDateHeaderIfRequired(final Context.Response response) {
        addDateHeaderIfRequired(ForkJoinPool.commonPool(), response);
    }

    public static void addDateHeaderIfRequired(final Executor executor, final Context.Response response) {
        if (!response.header("Date").isPresent()) {
            String dateString = getCurrentDateTime(executor);
            response.header("Date", dateString);
        }
    }

    public static String getCurrentDateTime(final Executor executor) {
        String dateString = cachedDateString.get();
        if (dateString == null) {
            //set the time and register a timer to invalidate it
            //note that this is racey, it does not matter if multiple threads do this
            //the perf cost of synchronizing would be more than the perf cost of multiple threads running it
            final long realTime = System.currentTimeMillis();
            final long mod = realTime % 1000;
            final long toGo = 1000 - mod;
            
            dateString = RFC1123_PATTERN_FORMAT.format(Instant.ofEpochMilli(realTime));
            
            if (cachedDateString.compareAndSet(null, dateString)) {
                // Could perhaps be a static final Runnable
                executor.execute(() -> {
                    try {
                        // Execute after
                        TimeUnit.MILLISECONDS.sleep(toGo);
                        // Invalidates the current date
                        cachedDateString.set(null);
                    } catch (InterruptedException e) {}
                });
            }
        }
        return dateString;
    }
}
