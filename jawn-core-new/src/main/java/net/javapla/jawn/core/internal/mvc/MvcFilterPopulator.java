package net.javapla.jawn.core.internal.mvc;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.inject.Injector;

import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.RouteFilterPopulator;

// ControllerFilterPopulator
public class MvcFilterPopulator extends RouteFilterPopulator {
    private Class<?> controller;
    
    public MvcFilterPopulator(final Class<?> controller) {
        this.controller = controller;
    }
    
    public void replace(Class<?> c) {
        controller = c;
    }
    
    public List<Route.Builder> populate(final Injector injector, final ActionParameterProvider provider, BiConsumer<Route.Builder, Object> work) {
        List<Route.Builder> list = MvcRouter.extract(controller, provider, injector);
        
        list.forEach(builder -> populate(injector, (item) -> work.accept(builder, item)));
        
        return list;
    }
}