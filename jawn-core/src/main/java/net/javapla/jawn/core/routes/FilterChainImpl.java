package net.javapla.jawn.core.routes;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.FilterChain;
import net.javapla.jawn.core.http.Context;


class FilterChainImpl implements FilterChain {

    private final Filter filter;
    private final FilterChain next;
    
    public FilterChainImpl(Filter filter, FilterChain next) {
        this.filter = filter;
        this.next = next;
    }

    @Override
    public Result before(Context context) {
        return filter.before(next, context);
    }
    
    @Override
    public void after(Context context) {
        filter.after(next, context);
    }
    
    @Override
    public void onException(Exception e) {
        filter.onException(next, e);
    }
}
