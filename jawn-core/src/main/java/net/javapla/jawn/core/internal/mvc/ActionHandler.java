package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Method;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.internal.injection.Provider;

class ActionHandler implements Route.MethodHandler {
    private static final long serialVersionUID = 2099971055978418962L;
    //private static final Object[] NO_ARGS = {};
    
    private final Method method;
    private final Class<?> controller;
    private final Registry.Key<?> key;

    private final Provider<?> provider; // save the provider, as the class might not be singleton
    private final ActionParameter[] params;
    private final Route.Handler handler;

    public ActionHandler(Method action, Class<?> controller, Registry registry, ActionParameterExtractor extractor) {
        this.method = action;
        this.controller = controller;
        this.key = Registry.Key.of(controller);
        
        this.provider = registry.provider(key);
        this.params = extractor.parameters(action); // returns null if params.length == 0
        this.handler = compileHandler();
    }
    
    private Route.Handler compileHandler() {
        if (params == null) {
            return (ctx) -> {
                return method.invoke(provider.get());
            };
        }
        
        return (ctx) -> {
            try {
                return method.invoke(provider.get(), provideArguments(ctx));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            
            return Status.BAD_REQUEST;
        };
    }

    private Object[] provideArguments(Context context) {
        Object[] n = new Object[params.length];
        
        for (int i = 0; i < params.length; i++) {
            try {
                n[i] = params[i].apply(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return n;
    }

    @Override
    public Object handle(Context ctx) throws Exception {
        return handler.handle(ctx);
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
