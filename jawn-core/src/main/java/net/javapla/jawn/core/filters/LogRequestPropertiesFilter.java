package net.javapla.jawn.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route;

@Singleton
public class LogRequestPropertiesFilter implements Route.Before {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Context context) {
        StringBuilder sb = new StringBuilder("\n------------------------------\n");
        sb.append("Method: ").append(context.req().httpMethod()).append("\n");
        sb.append("Request URL: ").append(context.req().path()).append("\n");
        sb.append("Query String: ").append(context.req().queryString().map(q -> "?"+q).orElse("")).append("\n");
        sb.append("ContextPath: ").append(context.req().context()).append("\n");
        sb.append("------------------------------");
        logger.info(sb.toString());
    }
}
