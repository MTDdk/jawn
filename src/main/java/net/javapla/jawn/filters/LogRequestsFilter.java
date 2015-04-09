package net.javapla.jawn.filters;

import net.javapla.jawn.Context;
import net.javapla.jawn.Route;

public class LogRequestsFilter extends FilterAdapter {

    @Override
    protected void before(Context context) {
        Route route = context.getRoute();
        logger.info("================ New request {}.{}() ================", route.getController(), route.getAction());
    }
}
