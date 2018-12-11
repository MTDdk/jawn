package net.javapla.jawn.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

public interface Config {

    final Function<String, Supplier<? extends RuntimeException>> exception = (name) -> () -> {
        String KEY_NOT_FOUND = "Key %s does not exist. Please include it in your " + Constants.PROPERTIES_FILE_USER + ". Otherwise this app will not work";
        throw new RuntimeException(String.format(KEY_NOT_FOUND, name));
    };
    
    Modes getMode();

    default boolean isProd() {
        return getMode() == Modes.PROD;
    }
    default boolean isTest() {
        return getMode() == Modes.TEST;
    }
    default boolean isDev() {
        return getMode() == Modes.DEV;
    }
    
    String get(final String name);
    Config set(final String name, final String value);
    
    default Config set(final String name, final int value) {
        return set(name, String.valueOf(value));
    }
    default Config set(final String name, final boolean value) {
        return set(name, String.valueOf(value));
    }
    
    default Optional<String> getOptionally(final String name) {
        return Optional.ofNullable(get(name));
    }
    
    default String getOrDie(final String name) throws RuntimeException {
        return getOptionally(name).orElseThrow(exception.apply(name));
    }
    
    default int getInt(final String name) throws NumberFormatException {
        return Integer.parseInt(get(name));
    }
    
    default Optional<Integer> getIntOptionally(final String name) {
        try {
            return Optional.of(getInt(name));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    default int getIntOrDie(final String name) throws RuntimeException {
        return getIntOptionally(name).orElseThrow(exception.apply(name));
    }
    
    default boolean getBoolean(final String name) /*throws IllegalArgumentException*/ {
        return Boolean.parseBoolean(get(name));
    }
    
    default Optional<Boolean> getBooleanOptionally(final String name) {
        return getOptionally(name).map(Boolean::parseBoolean);
    }
    
    default boolean getBooleanOrDie(final String name) throws RuntimeException {
        return getBooleanOptionally(name).orElseThrow(exception.apply(name));
    }
    
    Set<Map.Entry<String, String>> entrySet();
    
    /*private boolean _contains(String name) {
        return props.containsKey(modeLowerCased + name) || props.containsKey(name);
    }*/
    
    final class ParseOptions {
        final ClassLoader classLoader;
        
        private ParseOptions(final ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
        
        public static ParseOptions defaults() {
            return new ParseOptions(null);
        }
        
        public ParseOptions classLoader(ClassLoader loader) {
            if (classLoader == loader) return this;
            return new ParseOptions(loader);
        }
        
        public ClassLoader classLoader() {
            if (classLoader == null) return Thread.currentThread().getContextClassLoader();
            return classLoader;
        }
    }
    
    final class PropertiesLoader {
        public static Properties parseResourse(final String file, final ParseOptions options) {
            
            URL resource = options.classLoader().getResource(file);
            if (resource != null) return _read(resource);
            
            if (!file.endsWith(".properties")) {
                resource = options.classLoader().getResource(file + ".properties");
            }
            
            if (resource == null) throw new Err.IO("Resource '"+file+"' was not found");
            
            return _read(resource);
        }
        
        private static Properties _read(final URL resource) {
            try (InputStream stream = resource.openStream()) {
                Properties p = new Properties();
                p.load(stream);
                return p;
            } catch (IOException e) {
                throw new Err.IO(e);
            }
        }
    }
}
