package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.inject.Key;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.View;

public class MvcMethodHandler implements Route.MethodHandler {
    
    private final Method method;
    private final Class<?> routeClass;
    private final Key<?> key;
    

    public MvcMethodHandler(final Method method, final Class<?> routeClass) {
        this.method = method;
        this.routeClass = routeClass;
        this.key = Key.get(routeClass);
    }
    
    @Override
    public Result handle(final Context context) {
        
        /*Class<?> controller;
        Method action;
        if (context.require(Modes.class) == Modes.DEV) {
            try {
                controller = DynamicClassFactory.getCompiledClass(routeClass.getName(), false);
                action = controller.getMethod(method.getName(), method.getParameterTypes());
            } catch (Up.Compilation | Up.UnloadableClass | NoSuchMethodException | SecurityException e) {
                controller = routeClass;
                action = method;
            }
        } else {
            controller = routeClass;
            action = method;
        }
        return _handle(action.invoke(context.require(controller)));
        */
        
        // Whenever the Controller is placed within a sub-package of the Jawn sub-class, 
        // all Controllers will be automatically reloaded by the PackageWatcher in DEV,
        // as long the routes are built within Jawn.
        // Should this ever change, then uncomment the code above 
        
        
        // execute
        try {
            return _handle(method.invoke(context.require(key)));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //return Results.error();
            throw new Up.RenderableError(e);
        }
    }
    
    private Result _handle(Object result) {
        if (method.getReturnType() == void.class) {
            return Results.noContent();
        } else {
            if (method.getReturnType() == Result.class) {
                return (Result) result;
            } else if (method.getReturnType() == View.class) {
                return (View) result;
            } else {
                return Results.ok(result);
            }
        }
    }
    
    @Override
    public Method method() {
        return method;
    }

    @Override
    public Class<?> routeClass() {
        return routeClass;
    }
}
