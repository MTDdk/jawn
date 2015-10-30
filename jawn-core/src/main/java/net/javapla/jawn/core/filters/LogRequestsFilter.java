package net.javapla.jawn.core.filters;

import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.api.FilterAdapter;
import net.javapla.jawn.core.http.Context;

public class LogRequestsFilter extends FilterAdapter {

    @Override
    protected void before(Context context) {
        Route route = context.getRoute();
        logger.info("================ New request {}.{}() ================", route.getController(), route.getAction());
    }
}
