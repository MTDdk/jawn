package net.javapla.jawn.core.filters;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Chain;
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
    public Object before(Context context, Chain chain) {
        context.resp().header("Date", DATE);
        return chain.next(context);
    }
    
    public void stop() {
        t.cancel();
    }

}
