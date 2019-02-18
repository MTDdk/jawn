package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import com.google.inject.Injector;
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
    private final Injector injector;
    private final ActionParameterProvider provider;
    

    public MvcMethodHandler(final Method method, final Class<?> routeClass, final ActionParameterProvider provider, final Injector injector) {
        this.method = method;
        this.routeClass = routeClass;
        this.provider = provider;
        this.key = Key.get(routeClass);
        this.injector = injector;
    }
    
    @Override
    public Result handle(final Context context) {
        
        /*Class<?> controller;
        Method action;
        if (context.require(Modes.class) == Modes.DEV) {
            try {
                controller = ClassFactory.getCompiledClass(routeClass.getName(), false);
                action = controller.getMethod(method.getName(), method.getParameterTypes());
            } catch (Up.Compilation | Up.UnloadableClass | NoSuchMethodException | SecurityException e) {
                controller = routeClass;
                action = method;
            }
        } else {
            controller = routeClass;
            action = method;
        }
        try {
            return _handleReturnType(action.invoke(context.require(controller), _provideParameters(method, context)));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //return Results.error();
            throw new Up.RenderableError(e);
        }*/
        
        
        // Whenever the Controller is placed within a sub-package of the Jawn sub-class, 
        // all Controllers will be automatically reloaded by the PackageWatcher in DEV,
        // as long the routes are built within Jawn.
        // Should this ever change, then uncomment the code above 
        
        
        // execute
        try {
            return _handleReturnType(method.invoke(injector.getProvider(key).get(), _provideParameters(method, context)));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //return Results.error();
            throw new Up.RenderableError(e);
        }
    }
    
    //might be its own class
    private Object[] _provideParameters(final Method action, final Context context) {
        List<ActionParameter> params = provider.parameters(action);
        
        
        Parameter[] parameters = action.getParameters();
        
        if (parameters.length == 1 && parameters[0].getType() == Context.class) return new Object[] {context};
        
        /*if (parameters.length == 0)*/ return new Object[0];
        
        
//        Object[] arguments = new Object[parameters.length];
//        return arguments;
    }
    
    private Result _handleReturnType(Object result) {
//        ViewController view = routeClass.getAnnotation(ViewController.class);
        
        
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
