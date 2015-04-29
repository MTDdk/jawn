package net.javapla.jawn.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.exceptions.CompilationException;
import net.javapla.jawn.core.util.StringUtil;

/**
 * Created By
 * User: evan
 * Date: 4/30/13
 * 
 * @author MTD (16/01/15 - dd/MM/yy)
 * Added caching of controllers when active-reload is not set
 */
public abstract class DynamicClassFactory {
    private final static Map<String, Class<?>> CACHED_CONTROLLERS = new ConcurrentHashMap<>();

    /**
     * Loads and instantiates the class into the stated <code>expectedType</code>.
     * 
     * @param className Full name of the class including package name
     * @param expectedType The type to convert the class into
     * @param useCache flag to specify whether to cache the controller or not
     * @return The newly instantiated class
     * @throws CompilationException If the class could not be successfully compiled
     * @throws ClassLoadException 
     */
    public static <T> T createInstance(String className, Class<T> expectedType, boolean useCache) throws CompilationException, ClassLoadException {
        try {
            Object o = getCompiledClass(className, useCache).newInstance(); // a check to see if the class exists
            T instance = expectedType.cast(o); // a check to see if the class is actually a correct subclass
            return instance ;
        } catch (CompilationException e) {
            throw e;
        } catch (ClassLoadException e) {
            throw e;
        } catch (ClassCastException e) {
            //from cast()
            throw new ClassLoadException("Class: " + className + " is not the expected type, are you sure it extends " + expectedType.getName() + "?");
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }
    
    public static <T> T createInstance(Class<?> clazz, Class<T> expectedType) throws ClassLoadException {
        try {
            Object o = clazz.newInstance();
            return expectedType.cast(o);
        } catch (ClassCastException e) {
            //from cast()
            throw new ClassLoadException("Class: " + clazz + " is not the expected type, are you sure it extends " + expectedType.getName() + "?");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ClassLoadException(e);
        }
    }
    
    public static <T> T createInstance(Class<? extends T> clazz) throws ClassLoadException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ClassLoadException(e);
        }
    }

    /**
     * Handles caching of classes if not active_reload
     * @param className
     * @param useCache flag to specify whether to cache the controller or not
     * @return
     * @throws CompilationException
     * @throws ClassLoadException
     */
    public static Class<?> getCompiledClass(String className, boolean useCache) throws CompilationException, ClassLoadException {
        try {
            if (! useCache) {
                
                String compilationResult = compileClass(className);
                if (compilationResult.contains("cannot read")) {
                    throw new ClassLoadException(compilationResult);
                }
                if (compilationResult.contains("error")) {
                    throw new CompilationException(compilationResult);
                }

                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(DynamicClassFactory.class.getClassLoader());
                return dynamicClassLoader.loadClass(className);
            } else {
                return CACHED_CONTROLLERS.computeIfAbsent(className, WRAP_FORNAME);
            }
        } catch (CompilationException e) {
            throw e; // so the exception doesn't get caught by the more general catch(Exception)
        } catch (Exception e) {
            throw new ClassLoadException(e);
        }
    }
    
    public static <T> Class<? extends T> getCompiledClass(String className, Class<T> expected, boolean useCache) throws CompilationException, ClassLoadException {
        return getCompiledClass(className, useCache).asSubclass(expected);
    }
    
    
    /**
     * Wraps {@link Class#forName(String)} and recast any potential exception into a {@link RuntimeException}.
     * @param className
     * @return
     * @throws
     */
    protected static final Function<String, Class<?>> WRAP_FORNAME = className -> {
        try {
//            System.out.println("CACHED_CONTROLLERS ????? " + className);
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    };

    protected synchronized static String compileClass(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String controllerFileName = className.replace(".", System.getProperty("file.separator")) + ".java";

        URLClassLoader loader = ((URLClassLoader) Thread.currentThread().getContextClassLoader());
        URL[] urls = loader.getURLs();

        String classpath = getClasspath(urls);

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        String targetClasses = StringUtil.join(System.getProperty("file.separator"), "target", "classes");
        String srcMainJava = StringUtil.join(System.getProperty("file.separator"), "src", "main", "java");

        String[] args = {"-g:lines,source,vars", "-d", targetClasses, "-cp", classpath, srcMainJava + System.getProperty("file.separator") + controllerFileName};

        Class<?> cl = Class.forName("com.sun.tools.javac.Main");
        Method compile = cl.getMethod("compile", String[].class, PrintWriter.class);
        compile.invoke(null, args, out);
        out.flush();
        return writer.toString();
    }

    private static String getClasspath(URL[] urls) {
        String classpath = "";
        for (URL url : urls) {
            String path = url.getPath();
            if(System.getProperty("os.name").contains("Windows")){
                if(path.startsWith("/")){
                    path = path.substring(1);//loose leading slash
                }
                try{
                    path = URLDecoder.decode(path, "UTF-8");// fill in the spaces
                }catch(java.io.UnsupportedEncodingException e){/*ignore*/}
                path = path.replace("/", "\\");//boy, do I dislike windoz!
            }
            classpath += path + System.getProperty("path.separator");
        }

        return classpath;
    }
}
