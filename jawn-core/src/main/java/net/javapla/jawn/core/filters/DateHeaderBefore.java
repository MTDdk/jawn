package net.javapla.jawn.core.filters;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.util.DateUtil;

public class DateHeaderBefore implements Route.Before {

    private final Timer t = new Timer();
    private String DATE;
    
    public DateHeaderBefore() {
        DATE = DateUtil.toDateString(Instant.now());
        
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                DATE = DateUtil.toDateString(Instant.now());
            }
        }, 1000, 1000);
    }
    
    /*@Override
    public Result before(Context context, Chain chain) {
        context.resp().header("Date", DATE);
        return chain.next(context);
    }*/
    @Override
    public void before(Context context) {
        context.resp().header("Date", DATE);
    }
    
    public void stop() {
        t.cancel();
    }

}
