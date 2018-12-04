package net.javapla.jawn.core.internal.reflection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.util.URLCodec;

/**
 * Using reflection to locate classes in a package, and get the classes of a certain type.
 * 
 * <p>
 * <i>This class is very much <strong>not</strong> optimised for speed</i>
 * 
 * @author MTD
 */
public class ClassLocator {
    private static final Logger log = LoggerFactory.getLogger(ClassLocator.class);
    
    protected final static char DOT = '.';
    protected final static char SLASH = '/';
    protected final static String CLASS_SUFFIX = ".class";
    protected final static int CLASS_SUFFIX_LENGTH = CLASS_SUFFIX.length();
    
    protected final String packageToScan;
    protected final Map<String, Set<Class<?>>> store;
    
    public ClassLocator(String packageToScan) throws IllegalArgumentException {
        this.packageToScan = packageToScan;
        log.debug("package to scan {}", packageToScan);
        this.store = new HashMap<>();
        
        final String scannedPath = packageToScan.replace(DOT, SLASH);
        
        final Enumeration<URL> resources = retrieveResourcesFromPath(packageToScan, scannedPath);
        while (resources.hasMoreElements()) {
            processResource(new File(URLCodec.decode(resources.nextElement().getFile(), StandardCharsets.UTF_8)));
        }
    }
    
    private Enumeration<URL> retrieveResourcesFromPath(final String scannedPackage, final String scannedPath) throws IllegalArgumentException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(scannedPath);
        if (resource == null) 
            throw new IllegalArgumentException(String.format("Unable to get resources from path '%s'. Are you sure the given '%s' package exists?", scannedPath, scannedPackage));
        
        try {
            File scannedFile = new File(URLCodec.decode(resource.getFile(), StandardCharsets.UTF_8));
            if (scannedFile.isDirectory())
                return classLoader.getResources(scannedPath);
            else  {//we assume the classpath is inside a jar
                try (JarLoader jar = new JarLoader(scannedPath)) {
                    return jar.getResourcesFromJarFile();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to get resources from path '%s'. Are you sure the given '%s' package exists?", scannedPath, scannedPackage)
                    , e);
        }
    }
    
    private void processResource(final File file) {
        if (file != null) {
            if (file.isDirectory())
                findClassInDir(file);
            else
                findClass(file, "");
        }
    }
    
    private final <T> void findClassInDir(final File file) {
        File[] files = file.listFiles();
        if (files != null)
            for (File nestedFile : files) {
                findClass(nestedFile, "");
            }
    }
    
    private final <T> void findClass(final File file, final String subpackage) {
        final String resource = packageToScan + DOT + subpackage + file.getName();
        if (file.isDirectory()) {
            // file is a package
            for (File nestedFile : file.listFiles()) {
                findClass(nestedFile, subpackage + file.getName()+ DOT );
            }
        } else {
            final String className = extractClassName(resource);
            
            try {
                Class<?> c = Class.forName(className);
                
                Optional<Class<?>> superclass = ReflectionMetadata.getSuperclass(c);
                while (superclass.isPresent()) {
                    putIntoStore(superclass.get().getName(), c);
                    superclass = ReflectionMetadata.getSuperclass(superclass.get());
                }
                
                //README: ought the interfaces also be recursive as the superclass?
                for (String interfaceName : ReflectionMetadata.getInterfacesNames(c)) {
                    putIntoStore(interfaceName, c);
                }
            } catch (ClassNotFoundException ignore) { log.debug("Class not found: {}", className); }
        }
    }
    
    private final void putIntoStore(String superClassName, Class<?> clazz) {
        store.putIfAbsent(superClassName, new HashSet<>());
        store.get(superClassName).add(clazz);
    }
    
    public <T> Set<Class<? extends T>> subtypeOf(Class<T> clazz) {
        return store
                .getOrDefault(clazz.getName(), Collections.emptySet())
                .stream()
                .map(cls -> (Class<? extends T>)cls.asSubclass(clazz))
                .collect(Collectors.toSet());
    }
    
    public Set<Class<?>> foundClasses() {
        return store.values().stream().flatMap(cls -> cls.stream()).collect(Collectors.toSet());
    }
    
    public Set<Class<?>> foundClassesWithSuffix(String suffix) {
        return extractClassesWithSuffix(foundClasses(), suffix);
    }
    
    public static final <T> Set<Class<? extends T>> subtypesFromPackage(Class<T> clazz, String packageToScan) {
        return new ClassLocator(packageToScan).subtypeOf(clazz);
    }
    
    protected static final <T> Set<Class<? extends T>> extractClassesWithSuffix(Set<Class<? extends T>> classes, String suffix) {
        return classes.stream().filter(cls -> cls.getName().endsWith(suffix)).collect(Collectors.toSet());
    }
    
    
    private final static String extractClassName(String resource) {
        final int classEndIndex = resource.length() - CLASS_SUFFIX_LENGTH;
        return resource.substring(0, classEndIndex);
    }
    
}
