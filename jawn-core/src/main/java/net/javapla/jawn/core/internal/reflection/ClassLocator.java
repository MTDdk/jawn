package net.javapla.jawn.core.internal.reflection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.javapla.jawn.core.util.URLCodec;

public class ClassLocator {
    
    public static List<Class<?>> list(String packageToScan, ClassLoader loader) {
        String scanning = packageToScan.replace('.', '/');
        
        URL resource = loader.getResource(scanning);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Unable to get resources from path '%s'. Are you sure the given '%s' package exists?", scanning, packageToScan));
        }

        
        if (isJar(resource)) {
            
            return JarLoader.readClassesFromJar(scanning, loader);
            
        } else {
            
            LinkedList<Class<?>> classes = new LinkedList<>();
            
            try (InputStream stream = loader.getResourceAsStream(scanning);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                
                reader
                    .lines()
                    .filter(line -> line.endsWith(".class"))
                    .map(line -> toClass(packageToScan, line))
                    .filter(Objects::nonNull)
                    .forEach(classes::add);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return classes;
            
        }
    }
    
    static Class<?> toClass(String packageName, String className) {
        try {
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
            System.out.println("Class not found: " + className);
        }
        return null;
    }
    
    private static boolean isJar(URL resource) {
        return resource.toExternalForm().startsWith("jar:file");
    }
    

    static abstract class JarLoader {
        static LinkedList<Class<?>> readClassesFromJar(String scanning, ClassLoader loader) {
            LinkedList<Class<?>> classes = new LinkedList<>();
            
            loader.resources(scanning).forEach(url -> {
                String jarFile = url.getFile();
                
                // Perhaps this is only necessary on windows systems,
                // but it seems to be crucial to decode spaces
                jarFile = URLCodec.decode(jarFile, StandardCharsets.UTF_8);
                
                jarFile = extractJarFileFromClassPathFilename(jarFile);
                
                try {
                    classes.addAll( listClassesInJar(scanning, jarFile) );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            return classes;
        }
        
        private static String extractJarFileFromClassPathFilename(String file) {
            return file.substring("file:".length(), file.indexOf('!'));
        }
        
        private static List<Class<?>> listClassesInJar(String scanning, String jarFile) throws IOException {
            LinkedList<Class<?>> classes = new LinkedList<>();
            
            try (JarFile jar = new JarFile(jarFile)) {
                
                jar.stream()
                    .filter(Predicate.not(JarEntry::isDirectory))
                    .map(JarEntry::getRealName)
                    .filter(entry -> entry.startsWith(scanning) && entry.endsWith(".class"))
                    .map(entry -> entry.replace('/', '.')) // package-ify
                    .map(JarLoader::toClass)
                    .filter(Objects::nonNull)
                    .forEach(classes::add)
                    //.forEach(System.out::println)
                ;
                
            }
            
            return classes;
        }
        
        static Class<?> toClass(String className) {
            try {
                return Class.forName(className.substring(0, className.lastIndexOf('.')));
            } catch (ClassNotFoundException e) {
                // handle the exception
                System.out.println("Class not found: " + className);
            }
            return null;
        }
        
        /*private static String jarify(String jarFile, String className) {
            return String.format("jar:file:%s!/%s", jarFile, className);
        }*/
    }
    
}
