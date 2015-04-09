package net.javapla.jawn;


class FilterChainImpl implements FilterChain {

    private final Filter filter;
    private final FilterChain next;
    
    public FilterChainImpl(Filter filter, FilterChain next) {
        this.filter = filter;
        this.next = next;
    }

    @Override
    public ControllerResponse before(Context context) {
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
