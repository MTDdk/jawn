package net.javapla.jawn.core.internal.reflection;

/**
 * @deprecated Made obsolete by using java.util.ServiceLoader
 */
public class ModuleLoader {
    
    /*private static final Package MODULE_PACKAGE = Thread.currentThread().getContextClassLoader().getDefinedPackage("net.javapla.");

    
    public void loadModule() {
        
    }
    
    static class ClassLocator {
        
        private final String packageToScan;
        protected final Map<String, Set<Class<?>>> store = new HashMap<>();
        
        ClassLocator(String modulePackage) {
            packageToScan = modulePackage;
        }
        
        public <T> Set<Class<? extends T>> subtypesOf(Class<T> clazz) {
            return store
                .getOrDefault(clazz.getName(), Collections.emptySet())
                .stream()
                .map(cls -> (Class<? extends T>)cls.asSubclass(clazz))
                .collect(Collectors.toSet());
        }
        
        private void readAllResourcesFrom() {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        }
    }*/
}
