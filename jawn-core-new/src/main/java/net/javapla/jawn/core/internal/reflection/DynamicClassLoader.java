package net.javapla.jawn.core.internal.reflection;

import net.javapla.jawn.core.util.StreamUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Always loads a class from a file. No caching of any kind.
 * 
 * Dynamic class reloading is a bit more challenging. 
 * Java's builtin Class loaders always checks if a class is already loaded before loading it. 
 * Reloading the class is therefore not possible using Java's built-in class loaders. 
 * To reload a class you will have to implement your own ClassLoader subclass.
 * 
 * Needs to be re-instantiated each time the same class needs to be reloaded.
 * (This actually poses a memory-leak, unless this class does not get loaded by a 'standard' ClassLoader)
 * 
 * http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html#dynamicreloading
 * https://www.toptal.com/java/java-wizardry-101-a-guide-to-java-class-reloading
 */
class DynamicClassLoader extends ClassLoader {

    private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
    private final String allowedPackage;
    
//    private static final String FRAMEWORK_PACKAGE = "net.javapla.jawn";

    /*DynamicClassLoader(ClassLoader parent){
        //super(parent);
    }*/
    
    DynamicClassLoader(final String allowedPackage) {
        this.allowedPackage = allowedPackage;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if (!name.startsWith(allowedPackage, 0)) return loadByParent(name);
        int endIndex = name.indexOf(".class");
        if (endIndex > 0) name = name.substring(0, endIndex);
        final String pathToClassFile = name.replace('.', '/') + ".class";
        
        try{
            /*// net.javapla.jawn
            if (name.startsWith(FRAMEWORK_PACKAGE)) {
                return loadByParent(name);
            }

            // only do reloading on Controllers
            if(name.endsWith("Controller") || name.contains("Controller$")){

                String pathToClassFile = name.replace('.', '/') + ".class";

                byte[] classBytes = StreamUtil.bytes(getResourceAsStream(pathToClassFile));
                Class<?> daClass = defineClass(name, classBytes, 0, classBytes.length);

                logger.debug("Loaded class: " + name);
                return daClass;
            }else{
                return loadByParent(name);
            }*/
            
            
            
            

            byte[] classBytes = StreamUtil.bytes(getResourceAsStream(pathToClassFile));
            Class<?> daClass = defineClass(name, classBytes, 0, classBytes.length, null);

            logger.debug("Loaded class: " + pathToClassFile);
            return daClass;
        } catch(Exception e){
            logger.debug("Failed to dynamically load class: " + pathToClassFile + ". Loading by parent class loader.");
            try {
                return loadByParent(name);
            } catch (ClassNotFoundException e1) {
                return super.loadClass(name);
            }
        }
    }

    private Class<?> loadByParent(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

}
