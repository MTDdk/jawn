package net.javapla.jawn.core.internal.mvc;

import java.util.List;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.After;
import net.javapla.jawn.core.Route.Before;
import net.javapla.jawn.core.Route.Filter;
import net.javapla.jawn.core.Route.OnComplete;

public class MvcRouteBuilder implements Route.RouteBuilder {
    
    private final Object controller;
    
    public MvcRouteBuilder(final Object controller) {
        this.controller = controller;
    }
    
    public List<Route.Builder> build(Registry registry) {
        List<Route.Builder> list;
        if (controller instanceof Class<?>) {
            list = MvcCompiler.compile((Class<?>)controller, registry);
        } else {
            list = MvcCompiler.compile(controller.getClass(), () -> controller, registry);
        }
        return applyFilters(list);
    }
    
    private List<Route.Builder> applyFilters(List<Route.Builder> list) {
        list.forEach(bob -> {
            if (before != null) bob.before(before);
            if (after != null) bob.after(after);
            if (oncomplete != null) bob.postResponse(oncomplete);
            
            if (consumes != null) bob.consumes(consumes);
            if (produces != null) bob.produces(produces);
            
        });
        return list;
    }

    @Override
    public Route.RouteBuilder before(Before b) {
        if (before == null) {
            before = b;
        } else {
            before = before.then(b);
        }
        return this;
    }
    private Route.Before before = null;
    

    @Override
    public Route.RouteBuilder after(After a) {
        if (after == null) {
            after = a;
        } else {
            after = after.then(a);
        }
        return this;
    }
    private Route.After after = null;

    @Override
    public Route.RouteBuilder postResponse(OnComplete p) {
        if (oncomplete == null) {
            oncomplete = p;
        } else {
            oncomplete = oncomplete.then(p);
        }
        return this;
    }
    private Route.OnComplete oncomplete = null;
    
    @Override
    public Route.RouteBuilder filter(Filter f) {
        before(f).after(f).postResponse(f);
        return this;
    }

    private MediaType produces = null, consumes = null;
    @Override
    public Route.RouteBuilder produces(MediaType type) {
        produces = type;
        return this;
    }

    @Override
    public Route.RouteBuilder consumes(MediaType type) {
        consumes = type;
        return this;
    }

}
