package net.javapla.jawn.core.filters;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.util.DateUtil;

public class DateHeaderBefore implements Route.Before {

    private final Timer t = new Timer();
    private String DATE;
    
    public DateHeaderBefore() {
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                DATE = DateUtil.toDateString(Instant.now());
            }
        }, 0, 1000);
    }

    @Override
    public Result before(Context context, Route.Chain chain) {
        context.resp().header("Date", DATE);
        return chain.next();
    }
    
    public void stop() {
        t.cancel();
    }

}
