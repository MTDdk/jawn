package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Method;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.TypeLiteral;

class ActionHandler implements Route.MethodHandler {
    private static final long serialVersionUID = 2099971055978418962L;
    
    private final Method method;
    private final Class<?> controller;
    private final Registry registry;
    private final TypeLiteral<?> key;

    public ActionHandler(Method action, Class<?> controller, Registry registry) {
        this.method = action;
        this.controller = controller;
        this.registry = registry;
        this.key = TypeLiteral.get(controller);
        
    }

    @Override
    public Object handle(Context ctx) throws Exception {
        
        
        return null;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Class<?> controller() {
        return controller;
    }
}
