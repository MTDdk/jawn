package net.javapla.jawn.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.TimeUtil;

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
    
    
    // We probably want to keep this immutable
    /*Config set(final String name, final String value);
    
    default Config set(final String name, final int value) {
        return set(name, String.valueOf(value));
    }
    default Config set(final String name, final boolean value) {
        return set(name, String.valueOf(value));
    }*/
    
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
    
    default long getLong(final String name) throws NumberFormatException {
        return Long.parseLong(get(name));
    }
    
    default Optional<Long> getLongOptionally(final String name) {
        try {
            return Optional.of(getLong(name));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    default long getLongOrDie(final String name) throws RuntimeException {
        return getLongOptionally(name).orElseThrow(exception.apply(name));
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
    
    default Duration getDuration(final String name) {
        return Duration.ofSeconds(TimeUtil.seconds(get(name)));
    }
    
    default Optional<Duration> getDurationOptionally(final String name) {
        return getOptionally(name).map( value -> Duration.ofSeconds(TimeUtil.seconds(value)));
    }
    
    default Duration getDurationOrDie(final String name) throws RuntimeException {
        return getDurationOptionally(name).orElseThrow(exception.apply(name));
    }
    
    default long getDuration(final String name, final TimeUnit unit) {
        return unit.convert(TimeUtil.seconds(get(name)), TimeUnit.SECONDS);
    }
    
    default Optional<Long> getDurationOptionally(final String name, final TimeUnit unit) {
        return getOptionally(name).map( value -> unit.convert(TimeUtil.seconds(value), TimeUnit.SECONDS));
    }
    
    default long getDurationOrDie(final String name, final TimeUnit unit) throws RuntimeException {
        return getDurationOptionally(name, unit).orElseThrow(exception.apply(name));
    }

    Set<String> keys();
    
    /**
     * Answers if the partial path / start of a given key is present in this configuration.
     * <p>
     * E.g.: db.local could translate to
     * <ul>
     * <li>db.local.url
     * <li>db.local.port
     * <li>db.local.driver
     * </ul>
     * <p>Might <b>not</b> be optimised at all for many lookups..!
     * @param partialPath
     * @return
     */
    default boolean hasPath(String partialPath) {
        return keys().parallelStream().filter(s -> s.startsWith(partialPath)).findAny().isPresent();
    }
    
    /**
     * Same functionality as {@link #hasPath(String)}
     * <p>Might also very much not be optimised
     * 
     * @param partialPath
     * @return
     */
    default Set<String> keysOf(String partialPath) {
        return keys().parallelStream().filter(s -> s.startsWith(partialPath)).collect(Collectors.toSet());
    }
    
    
    final class ParseOptions {
        final ClassLoader classLoader;
        final Charset charset;
        
        private ParseOptions(final ClassLoader classLoader, final Charset charset) {
            this.classLoader = classLoader;
            this.charset = charset;
        }
        
        private ParseOptions(final ClassLoader classLoader) {
            this.classLoader = classLoader;
            this.charset = StandardCharsets.UTF_8;
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
        
        public ParseOptions charset(Charset charset) {
            if (charset.equals(charset)) return this;
            return new ParseOptions(classLoader, charset);
        }
        
        public Charset charset() {
            return charset;
        }
    }
    
    final class PropertiesLoader {
        public static Properties parseMap(final Map<String, String> properties) {
            Properties p = new Properties();
            properties.forEach(p::put);
            return p;
        }
        
        public static Properties parseResourse(final String file, final ParseOptions options) {
            
            URL resource = options.classLoader().getResource(file);
            if (resource != null) return _read(resource, options.charset());
            
            if (!file.endsWith(".properties")) {
                resource = options.classLoader().getResource(file + ".properties");
            }
            
            if (resource == null) throw new Up.IO("Resource '"+file+"' was not found");
            
            return _read(resource, options.charset());
        }
        
        private static Properties _read(final URL resource, final Charset charset) {
            try (InputStreamReader stream = new InputStreamReader(resource.openStream(), charset)) {
                Properties p = new Properties();
                p.load(stream);
                return p;
            } catch (IOException e) {
                throw new Up.IO(e);
            }
        }
    }
}
