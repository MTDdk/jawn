package net.javapla.jawn.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Chain;

@Singleton
public class LogRequestPropertiesFilter implements Route.Before {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object before(Context context, Chain chain) {
        StringBuilder sb = new StringBuilder("\n------------------------------\n");
        sb.append("Method: ").append(context.req().httpMethod()).append("\n");
        sb.append("Request URL: ").append(context.req().path()).append("\n");
        sb.append("Query String: ").append(context.req().queryString().map(q -> "?"+q).orElse("")).append("\n");
        sb.append("ContextPath: ").append(context.req().context()).append("\n");
        sb.append("------------------------------");
        logger.info(sb.toString());
        return chain.next(context);
    }
}
