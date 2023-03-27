package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Method;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.TypeLiteral;

public class ActionHandler implements Route.MethodHandler {
    
    private final Method method;
    private final Class<?> controller;
    private final TypeLiteral<?> key;

    public ActionHandler(Method action, Class<?> controller, Registry registry) {
        this.method = action;
        this.controller = controller;
        this.key = TypeLiteral.get(controller);
        
    }

    @Override
    public Object handle(Context ctx) throws Exception {
        
        
        return null;
    }

    @Override
    public Method method() {
        return null;
    }

    @Override
    public Class<?> controller() {
        return null;
    }
}
