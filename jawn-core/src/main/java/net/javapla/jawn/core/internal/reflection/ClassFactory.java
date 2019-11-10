package net.javapla.jawn.core.internal.reflection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.javapla.jawn.core.Up;

/**
 * @author MTD
 */
public abstract class ClassFactory {
    private final static Map<String, Class<?>> CACHED_CONTROLLERS = new ConcurrentHashMap<>();
    
    private ClassFactory() {}

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
    public final static <T> T createInstance(String className, Class<T> expectedType, boolean useCache) throws Up.Compilation, Up.UnloadableClass {
        try {
            Object o = createInstance(getCompiledClass(className, useCache)); // a check to see if the class exists
            T instance = expectedType.cast(o); // a check to see if the class is actually a correct subclass
            return instance ;
        } catch (Up.Compilation | Up.UnloadableClass e) {
            throw e;
        } catch (ClassCastException e) {
            //from cast()
            throw new Up.UnloadableClass("Class: " + className + " is not the expected type, are you sure it extends " + expectedType.getName() + "?", e);
        } catch (Exception e) {
            throw new Up.UnloadableClass(e);
        }
    }
    
    public final static <T> T createInstance(Class<?> clazz, Class<T> expectedType) throws Up.UnloadableClass {
        try {
            Object o = createInstance(clazz);
            return expectedType.cast(o);
        } catch (ClassCastException e) {
            //from cast()
            throw new Up.UnloadableClass("Class: " + clazz + " is not the expected type, are you sure it extends " + expectedType.getName() + "?", e);
        }
    }
    
    public final static <T> T createInstance(Class<? extends T> clazz) throws Up.UnloadableClass {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Up.UnloadableClass(e);
        } catch (Exception e) {
            throw new Up.UnloadableClass(e);
        }
    }

    /**
     * Handles caching of classes if not useCache
     * @param fullClassName including package name
     * @param useCache flag to specify whether to cache the controller or not
     * @return
     * @throws Up.Compilation
     * @throws Up.UnloadableClass
     */
    public final static Class<?> getCompiledClass(String fullClassName, boolean useCache) throws Up.Compilation, Up.UnloadableClass {
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
    }
    
    /*public final static <T> Class<? extends T> getCompiledClass(String className, Class<T> expected, boolean useCache) throws Up.Compilation, Up.UnloadableClass {
        Class<?> compiledClass = getCompiledClass(className, useCache);
        return compiledClass.asSubclass(expected);
    }*/
    
    public final static Class<?> recompileClass(String clzz) throws Up.Compilation, Up.UnloadableClass {
        return getCompiledClass(clzz, false);
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
            throw new Up.Compilation(className, e);
        }
    };
    
    static String packageName(final String fullClassName) {
        return fullClassName.substring(0, removeClassEnding(fullClassName).lastIndexOf('.'));
    }
    
    static String removeClassEnding(final String fullClassName) {
        if (fullClassName.endsWith(".class")) // 6 chars
            return fullClassName.substring(0, fullClassName.length() - 6);
        return fullClassName;
    }
}
