package net.javapla.jawn.core.internal;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

final class ConfigImpl implements Config {
    
    private final Properties props;
    
    private final Modes mode;
    private final String modeLowerCased;

    private ConfigImpl(final Properties props) {
        this(Modes.DEV, props);
    }
    
    private ConfigImpl(final Modes mode, final Properties props) {
        this.props = props;
        this.mode = mode;
        this.modeLowerCased = mode.name().toLowerCase() + '.';
    }
    
    static Config parse(final Map<String, String> properties) {
        Properties p = PropertiesLoader.parseMap(properties);
        return new ConfigImpl(p);
    }
    
    static Config parse(final Modes mode, final Map<String, String> properties) {
        return parse(properties);
    }
    
    static Config parse(final Modes mode, final String file) {
        Properties properties = PropertiesLoader.parseResourse(file, ParseOptions.defaults());
        return new ConfigImpl(mode, properties);
    }
    
    static Config framework(final Modes mode) {
        return parse(mode, Constants.PROPERTIES_FILE_FRAMEWORK);
    }
    
    static Config user(final Modes mode) {
        return parse(mode, Constants.PROPERTIES_FILE_USER);
    }
    
    static Config empty() {
        return new ConfigImpl(new Properties());
    }
    
    static Config empty(final Modes mode) {
        return new ConfigImpl(mode, new Properties());
    }
    
    /*@Override
    public Config set(String name, String value) {
        props.put(name, value);
        props.put(modeLowerCased + name, value);
        return this;
    }*/
    
    /**
     * Returns a new {@link ConfigImpl}, which overrides the properties of <b>this</b> with those of <b>that</b>
     * @param that
     */
    Config merge(final Config that) {
        Properties thisCloned = (Properties) this.props.clone();
        that.entrySet().parallelStream().forEach(e -> thisCloned.setProperty(e.getKey(), e.getValue()));
        return new ConfigImpl(that.getMode(), thisCloned);
    }

    @Override
    public Modes getMode() {
        return mode;
    }
    
    /*private Config setMode(Modes mode) {
        this.mode = mode;
        lowerCase();
        return this;
    }*/
    
    @Override
    public String get(final String name) {
        return props.containsKey(modeLowerCased + name) ? props.getProperty(modeLowerCased + name) : props.getProperty(name);
    }
    
    /*@Override
    public Config set(String name, String value) {
        props.put(name, value);
        props.put(modeLowerCased + name, value);
        return this;
    }*/
    
    @Override
    public Set<Entry<String, String>> entrySet() {
        Function<Map.Entry<Object, Object>,Map.Entry<String,String>> convert = 
            e -> new AbstractMap.SimpleImmutableEntry<String,String>(String.valueOf(e.getKey()),String.valueOf(e.getValue()));
        
        return props.entrySet().parallelStream().map(convert).collect(Collectors.toSet());
    }
    
    @Override
    public Set<String> keys() {
        return props.stringPropertyNames();
    }
    
}
