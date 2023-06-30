package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Method;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.injection.Provider;

class ActionHandler implements Route.MethodHandler {
    private static final long serialVersionUID = 2099971055978418962L;
    
    private final Method method;
    private final Class<?> controller;
    private final Registry.Key<?> key;

    private final Provider<?> provider;

    public ActionHandler(Method action, Class<?> controller, Registry registry) {
        this.method = action;
        this.controller = controller;
        this.key = Registry.Key.of(controller);
        
        this.provider = registry.provider(key);
    }

    @Override
    public Object handle(Context ctx) throws Exception {
        Object controller = provider.get();
        //Object[] actionArguments;
        Object result = method.invoke(controller/*, actionArguments*/);
        
        return result;
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
