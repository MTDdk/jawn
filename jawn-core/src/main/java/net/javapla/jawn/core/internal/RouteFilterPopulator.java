package net.javapla.jawn.core.internal;

import java.util.LinkedList;
import java.util.function.Consumer;

import com.google.inject.Injector;
import com.google.inject.Key;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Filtering;

//FilterHolder
public class RouteFilterPopulator implements Route.Filtering {
    protected final LinkedList<Object> bagOFilters;
    protected final LinkedList<RouteBag> bagORoutes;
    
    public RouteFilterPopulator() {
        bagOFilters = new LinkedList<>();
        bagORoutes = new LinkedList<>();
    }
    
    @Override
    public RouteFilterPopulator filter(final Class<?> f) {
        bagOFilters.add(f);
        return this;
    }
    
    @Override
    public RouteFilterPopulator filter(final Route.Filter filter) {
        bagOFilters.add(filter);
        return this;
    }

    @Override
    public RouteFilterPopulator before(final Route.Before handler) {
        bagOFilters.add(handler);
        return this;
    }

    @Override
    public RouteFilterPopulator after(final Route.After handler) {
        bagOFilters.add(handler);
        return this;
    }
    
    public int size() {
        return bagOFilters.size();
    }
    
    public void hold(final Route.Builder builder) {
        bagORoutes.add(new RouteBag(builder, bagOFilters.size()));
    }
    
    public void populate(final Injector injector, Consumer<Object> consumer) {
        bagOFilters.forEach(item -> {
            if (item instanceof Class<?>) {
                Class<?> d = (Class<?>)item;
                Object g = injector.getInstance(Key.get(d));//.getInstance( d);
                consumer.accept(g);
            } else {
                consumer.accept(item);
            }
        });
    }
    
    private static class RouteBag {
        
        final Route.Builder route;
        final int filterPosition;

        RouteBag(Route.Builder route, int filterPosition) {
            this.route = route;
            this.filterPosition = filterPosition;
        }
    }
}