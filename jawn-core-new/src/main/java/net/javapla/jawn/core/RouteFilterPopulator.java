package net.javapla.jawn.core;

import java.util.LinkedList;
import java.util.List;

import com.google.inject.Injector;

import net.javapla.jawn.core.Route.After;
import net.javapla.jawn.core.Route.Before;
import net.javapla.jawn.core.Route.Filter;
import net.javapla.jawn.core.internal.reflection.ReflectionMetadata;

class RouteFilterPopulator implements Route.Filtering/*<RouteFilterPopulator>*/ {
    private final LinkedList<Object> bagOFilters;
    
    RouteFilterPopulator() {
        bagOFilters = new LinkedList<>();
    }
    
    /*void filter(final Route.Filter f) {
        bagOFilters.add(f);
    }*/
    
    void filter(final Route.Before f) {
        bagOFilters.add(f);
    }
    
    void filter(final Route.After f) {
        bagOFilters.add(f);
    }
    
    void filter(final Class<?> f) {
        bagOFilters.add(f);
    }
    
    @Override
    public RouteFilterPopulator filter(Filter filter) {
        bagOFilters.add(filter);
        return this;
    }

    @Override
    public RouteFilterPopulator before(Before handler) {
        bagOFilters.add(handler);
        return this;
    }

    @Override
    public RouteFilterPopulator after(After handler) {
        bagOFilters.add(handler);
        return this;
    }
    
    /**
     * Add global filters to all routes
     * The notion of route specific filters, is, that they are the innermost, and
     * global filters are wrapping around them
     * 
     * Should add the filters in a layered manner, and in the order
     * they are written in the code
     * 
     * Example:
     * jawn.filter(filter1);
     * jawn.filter(filter2);
     * 
     * Results in following execution order:
     * filter1.before -> filter2.before -> execute handler -> filter2.after -> filter1.after
     * 
     * Example2:
     * jawn.get("/",work).before(beforeFilter).after(afterFilter);
     * jawn.filter(filter1);
     * jawn.filter(filter2);
     * 
     * Execution order:
     * filter1.before -> filter2.before -> beforeFilter -> execute handler -> afterFilter -> filter2.after -> filter1.after
     * 
     * @param routes
     */
    void populate(final List<Route.Builder> routes, final Injector injector) {
        bagOFilters.forEach(item -> {
            if (item instanceof Route.Filter) { //filter is instanceof Before and After, so this has to be first
                filter(routes, item);
            } else if (item instanceof Route.After) {
                after(routes, item);
            } else if (item instanceof Route.Before) {
                before(routes, item);
            } else if (item instanceof Class<?>) {
                Class<?> d = (Class<?>)item;
                
                if (ReflectionMetadata.isAssignableFrom(d, Route.Filter.class)) {
                    filter(routes, injector.getInstance(d));
                } else if (ReflectionMetadata.isAssignableFrom(d, Route.After.class)) {
                    after(routes, injector.getInstance(d));
                } else if (ReflectionMetadata.isAssignableFrom(d, Route.Before.class)) {
                    before(routes, injector.getInstance(d));
                }
            }
        });
    }
    
    private void before(final List<Route.Builder> routes, Object item) {
        routes.forEach(r -> r.globalBefore((Route.Before) item));
    }
    
    private void after(final List<Route.Builder> routes, Object item) {
        routes.forEach(r -> r.globalAfter((Route.After) item));
    }
    
    private void filter(final List<Route.Builder> routes, Object item) {
        routes.forEach(r -> r.globalFilter((Route.Filter) item));
    }
    
    void populate(Route.Builder route, final Injector injector) {
        bagOFilters.forEach(item -> {
            if (item instanceof Route.Filter) { //filter is instanceof Before and After, so this has to be first
                route.filter((Route.Filter) item);
            } else if (item instanceof Route.After) {
                route.after((Route.After) item);
            } else if (item instanceof Route.Before) {
                route.before((Route.Before) item);
            } else if (item instanceof Class<?>) {
                Class<?> d = (Class<?>)item;
                
                if (ReflectionMetadata.isAssignableFrom(d, Route.Filter.class)) {
                    route.filter((Route.Filter) injector.getInstance(d));
                } else if (ReflectionMetadata.isAssignableFrom(d, Route.After.class)) {
                    route.after((Route.After) injector.getInstance(d));
                } else if (ReflectionMetadata.isAssignableFrom(d, Route.Before.class)) {
                    route.before((Route.Before) injector.getInstance(d));
                }
            }
        });
    }
}