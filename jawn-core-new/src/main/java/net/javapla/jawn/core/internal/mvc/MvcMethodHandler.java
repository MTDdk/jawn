package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.inject.Injector;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.reflection.DynamicClassFactory;
import net.javapla.jawn.core.util.Modes;

public class MvcMethodHandler implements Route.MethodHandler {
    
    private final Method method;
    private final Class<?> routeClass;

    public MvcMethodHandler(final Method method, final Class<?> routeClass) {
        this.method = method;
        this.routeClass = routeClass;
    }
    
    @Override
    public Result handle(final Context context) {
        
        Object result;
        
        try {
            Object instance;
            if (context.require(Modes.class) == Modes.DEV) {
                instance = DynamicClassFactory.createInstance(routeClass.getName(), Object.class, false);
                Method action = instance.getClass().getMethod(method.getName(), method.getParameterTypes());
                
                Injector injector = context.require(Injector.class);
                injector.injectMembers(instance);
                result = action.invoke(instance);
            } else {
                instance = context.require(routeClass);
                result = method.invoke(instance);
            }
            
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return Results.error();
        }
        
        if (method.getReturnType() == void.class) {
            return Results.noContent();
        } else if (method.getReturnType() == Result.class) {
            return (Result) result;
        } else {
            return Results.ok(result);
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
