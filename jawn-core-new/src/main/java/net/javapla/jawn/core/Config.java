package net.javapla.jawn.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    
    private final Function<String, Supplier<? extends RuntimeException>> exception = (name) -> () -> {
        String KEY_NOT_FOUND = "Key %s does not exist. Please include it in your " + Constants.PROPERTIES_FILE_USER + ". Otherwise this app will not work";
        logger.error(String.format(KEY_NOT_FOUND, name));
        throw new RuntimeException(String.format(KEY_NOT_FOUND, name));
    };
    
    private final Modes mode;
    private final String modeLowerCased;
    private final Properties props;

    private Config(final Modes mode, final Properties props) {
        this.mode = mode;
        this.modeLowerCased = mode.name().toLowerCase() + '.';
        this.props = props;
    }
    
    static Config parse(final Modes mode, final String file) {
        Properties properties = PropertiesLoader.parseResourse(file, ParseOptions.defaults());
        return new Config(mode, properties);
    }
    
    static Config framework(final Modes mode) {
        return parse(mode, Constants.PROPERTIES_FILE_FRAMEWORK);
    }
    
    static Config user(final Modes mode) {
        return parse(mode, Constants.PROPERTIES_FILE_USER);
    }
    
    static Config empty(final Modes mode) {
        return new Config(mode, new Properties());
    }
    
    public Modes getMode() {
        return mode;
    }

    public boolean isProd() {
        return getMode() == Modes.PROD;
    }
    public boolean isTest() {
        return getMode() == Modes.TEST;
    }
    public boolean isDev() {
        return getMode() == Modes.DEV;
    }
    
    public String get(final String name) {
        return props.containsKey(modeLowerCased + name) ? props.getProperty(modeLowerCased + name) : props.getProperty(name);
    }
    
    public Optional<String> getOptionally(final String name) {
        return Optional.ofNullable(get(name));
    }
    
    public String getOrDie(final String name) throws RuntimeException {
        return getOptionally(name).orElseThrow(exception.apply(name));
    }
    
    public int getInt(final String name) throws NumberFormatException {
        return Integer.parseInt(get(name));
    }
    
    public Optional<Integer> getIntOptionally(final String name) {
        try {
            return Optional.of(getInt(name));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    public int getIntOrDie(final String name) throws RuntimeException {
        return getIntOptionally(name).orElseThrow(exception.apply(name));
    }
    
    public boolean getBoolean(final String name) /*throws IllegalArgumentException*/ {
        return Boolean.parseBoolean(get(name));
    }
    
    public Optional<Boolean> getBooleanOptionally(final String name) {
        return getOptionally(name).map(Boolean::parseBoolean);
    }
    
    public boolean getBooleanOrDie(final String name) throws RuntimeException {
        return getBooleanOptionally(name).orElseThrow(exception.apply(name));
    }
    
    /*private boolean _contains(String name) {
        return props.containsKey(modeLowerCased + name) || props.containsKey(name);
    }*/
    
    public static final class ParseOptions {
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
    
    static final class PropertiesLoader {
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
                logger.error("", e);
                throw new Err.IO(e);
            }
        }
    }
}
