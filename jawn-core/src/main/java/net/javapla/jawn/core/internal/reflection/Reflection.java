package net.javapla.jawn.core.internal.reflection;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;

public final class Reflection {

    @SuppressWarnings("unchecked")
    public static final <T> Class<T> callingClass(Class<T> assignableFrom) {
        
        /* 
         * https://stackoverflow.com/a/34948763
         *  - index 0 = Thread
         *  - index 1 = this
         *  - index 2 = direct caller, can be self.
         *  - index 3 ... n = classes and methods that called each other to get to the index 2 and below.
         */
        
        StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
        return (Class<T>) walker.walk( frame -> {
            return frame
                .map(StackFrame::getDeclaringClass)
                .skip(2)
                .filter(clz -> assignableFrom.isAssignableFrom(((Class<?>)clz.getGenericSuperclass())))
                .findFirst();
        }).get();
        
        /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stack.length; i++) {
            
            //Class<?> compiledClass = getCompiledClass(stack[i].getClassName(), false);
            try {
                Class<?> compiledClass = Reflection.class.getClassLoader().loadClass(stack[i].getClassName());
                if (assignableFrom.isAssignableFrom(compiledClass)) {
                    return (Class<T>) compiledClass;
                }
            } catch (ClassNotFoundException e) {
                throw Up.IO(e);
            }
        }
        
        return null;*/
    }
    
    /**
     * Handles caching of classes if not useCache
     * @param fullClassName including package name
     * @param useCache flag to specify whether to cache the controller or not
     * @return
     * @throws Up.Compilation
     * @throws Up.UnloadableClass
     */
    /*public final static Class<?> getCompiledClass(String fullClassName, boolean useCache) throws Up.Compilation, Up.UnloadableClass {
        try {
            if (! useCache) {
                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(packageName(fullClassName));
                Class<?> cl = dynamicClassLoader.loadClass(fullClassName);
                dynamicClassLoader = null;
                return cl;
            } else {
                return CACHED_CONTROLLERS.computeIfAbsent(removeClassEnding(fullClassName), WRAP_FORNAME);
            }
        } catch (ClassNotFoundException e) {
            throw new Up.Compilation(fullClassName, e);
        } catch (Up.Compilation e) {
            throw e;
        } catch (Exception e) {
            throw new Up.UnloadableClass(fullClassName, e);
        }
    }*/
    
}

