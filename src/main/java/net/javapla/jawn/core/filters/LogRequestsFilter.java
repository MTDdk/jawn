package net.javapla.jawn.core.filters;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route;

public class LogRequestsFilter extends FilterAdapter {

    @Override
    protected void before(Context context) {
        Route route = context.getRoute();
        logger.info("================ New request {}.{}() ================", route.getController(), route.getAction());
    }
}
