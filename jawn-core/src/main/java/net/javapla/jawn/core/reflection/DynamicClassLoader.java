package net.javapla.jawn.core.reflection;

import net.javapla.jawn.core.util.StreamUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Always loads a class from a file. No caching of any kind.
 * Needs to be re-instantiated each time the same class needs to be reloaded.
 * 
 * Dynamic class reloading is a bit more challenging. 
 * Java's builtin Class loaders always checks if a class is already loaded before loading it. 
 * Reloading the class is therefore not possible using Java's builtin class loaders. 
 * To reload a class you will have to implement your own ClassLoader subclass.
 * http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html#dynamicreloading
 */
class DynamicClassLoader extends ClassLoader {

    private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
    
    private static final String FRAMEWORK_PACKAGE = "net.javapla.jawn";

    DynamicClassLoader(ClassLoader parent){
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        try{
            // net.javapla.jawn
            if (name.startsWith(FRAMEWORK_PACKAGE)) {
                return loadByParent(name);
            }

            // only do reloading on Controllers
            //README this currently also looks for RouteConfig - might need to be removed
            if(name.endsWith("Controller") || name.contains("Controller$")
                    /*|| name.equals(Configuration.getRouteConfigClassName())*/){

                String pathToClassFile = name.replace('.', '/') + ".class";

                byte[] classBytes = StreamUtil.bytes(getResourceAsStream(pathToClassFile));
                Class<?> daClass = defineClass(name, classBytes, 0, classBytes.length);

                logger.debug("Loaded class: " + name);
                return daClass;
            }else{
                return loadByParent(name);
            }
        } catch(Exception e){
            logger.debug("Failed to dynamically load class: " + name + ". Loading by parent class loader.");
            return loadByParent(name);
        }
    }

    private Class<?> loadByParent(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

}
