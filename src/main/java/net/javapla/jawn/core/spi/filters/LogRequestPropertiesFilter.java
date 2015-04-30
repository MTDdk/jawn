package net.javapla.jawn.core.spi.filters;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.spi.FilterAdapter;

public class LogRequestPropertiesFilter extends FilterAdapter {

    @Override
    protected void before(Context context) {
        StringBuilder sb = new StringBuilder("\n------------------------------\n");
        sb.append("Request URL: ").append(context.requestUrl()).append("\n");
        sb.append("ContextPath: ").append(context.contextPath()).append("\n");
        sb.append("Query String: ").append(context.queryString()).append("\n");
        sb.append("URI Full Path: ").append(context.requestUri()).append("\n");
        sb.append("URI Path: ").append(context.path()).append("\n");
        sb.append("Method: ").append(context.method());
        sb.append("\n------------------------------");
        logger.info(sb.toString());
    }
}
