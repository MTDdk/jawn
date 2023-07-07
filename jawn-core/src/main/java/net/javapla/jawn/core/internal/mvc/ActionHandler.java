package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Method;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.internal.injection.Provider;

class ActionHandler implements Route.Handler {
    private static final long serialVersionUID = 2099971055978418962L;
    
    private final Method action;

    private final Provider<?> provider; // save the provider, as the class might not be singleton
    private final ActionParameter[] params;
    private final Route.Handler handler;

    public ActionHandler(Method action, Provider<?> controllerProvider, ActionParameterExtractor extractor) {
        this.action = action;
        
        this.provider = controllerProvider;
        this.params = extractor.parameters(action); // returns null if params.length == 0
        this.handler = compileHandler();
    }
    
    private Route.Handler compileHandler() {
        
        // NO ARGS
        if (params == null) {
            return (ctx) -> {
                return action.invoke(provider.get());
            };
        }
        
        // WITH ARGS
        return (ctx) -> {
            try {
                return action.invoke(provider.get(), applyArguments(ctx));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            
            return Status.BAD_REQUEST;
        };
    }

    private Object[] applyArguments(Context context) {
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
    
    /*@Override
    public Method method() {
        return method;
    }

    @Override
    public Class<?> controller() {
        return controller;
    }*/
}
