package net.javapla.jawn.core.internal.reflection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.javapla.jawn.core.Err;

/**
 * @author MTD
 */
public abstract class DynamicClassFactory {
    private final static Map<String, Class<?>> CACHED_CONTROLLERS = new ConcurrentHashMap<>();

    /**
     * Loads and instantiates the class into the stated <code>expectedType</code>.
     * 
     * @param className Full name of the class including package name
     * @param expectedType The type to convert the class into
     * @param useCache flag to specify whether to cache the class or not
     * @return The newly instantiated class
     * @throws CompilationException If the class could not be successfully compiled
     * @throws ClassLoadException 
     */
    public final static <T> T createInstance(String className, Class<T> expectedType, boolean useCache) throws Err.Compilation, Err.UnloadableClass {
        try {
            Object o = createInstance(getCompiledClass(className, useCache)); // a check to see if the class exists
            T instance = expectedType.cast(o); // a check to see if the class is actually a correct subclass
            return instance ;
        } catch (Err.Compilation | Err.UnloadableClass e) {
            throw e;
        } catch (ClassCastException e) {
            //from cast()
            throw new Err.UnloadableClass("Class: " + className + " is not the expected type, are you sure it extends " + expectedType.getName() + "?");
        } catch (Exception e) {
            throw new Err.UnloadableClass(e);
        }
    }
    
    public final static <T> T createInstance(Class<?> clazz, Class<T> expectedType) throws Err.UnloadableClass {
        try {
            Object o = createInstance(clazz);
            return expectedType.cast(o);
        } catch (ClassCastException e) {
            //from cast()
            throw new Err.UnloadableClass("Class: " + clazz + " is not the expected type, are you sure it extends " + expectedType.getName() + "?");
        }
    }
    
    public final static <T> T createInstance(Class<? extends T> clazz) throws Err.UnloadableClass {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Err.UnloadableClass(e);
        } catch (Exception e) {
            throw new Err.UnloadableClass(e);
        }
    }

    /**
     * Handles caching of classes if not useCache
     * @param fullClassName including package name
     * @param useCache flag to specify whether to cache the controller or not
     * @return
     * @throws CompilationException
     * @throws ClassLoadException
     */
    public final static Class<?> getCompiledClass(String fullClassName, boolean useCache) throws Err.Compilation, Err.UnloadableClass {
        try {
            if (! useCache) {
                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(fullClassName.substring(0, fullClassName.lastIndexOf('.')));
                Class<?> cl = dynamicClassLoader.loadClass(fullClassName);
                dynamicClassLoader = null;
                return cl;
            } else {
                return CACHED_CONTROLLERS.computeIfAbsent(fullClassName, WRAP_FORNAME);
            }
        } catch (Exception e) {
            throw new Err.UnloadableClass(e);
        }
    }
    
    public final static <T> Class<? extends T> getCompiledClass(String className, Class<T> expected, boolean useCache) throws Err.Compilation, Err.UnloadableClass {
        Class<?> compiledClass = getCompiledClass(className, useCache);
        return compiledClass.asSubclass(expected);
    }
    
    
    /**
     * Wraps {@link Class#forName(String)} and recast any potential exception into a {@link RuntimeException}.
     * @param className
     * @return
     * @throws
     */
    protected static final Function<String, Class<?>> WRAP_FORNAME = className -> {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    };
}
