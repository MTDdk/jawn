package net.javapla.jawn.core.internal;

import java.util.LinkedList;
import java.util.function.Consumer;

import com.google.inject.Injector;

import net.javapla.jawn.core.Route;

//FilterHolder
public class RouteFilterPopulator implements Route.Filtering {
    protected final LinkedList<Object> bagOFilters;
    
    public RouteFilterPopulator() {
        bagOFilters = new LinkedList<>();
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
    
    public void populate(final Injector injector, Consumer<Object> consumer) {
        bagOFilters.forEach(item -> {
            if (item instanceof Class<?>) {
                Class<?> d = (Class<?>)item;
                Object g = injector.getInstance(d);
                consumer.accept(g);
            } else {
                consumer.accept(item);
            }
        });
    }
}