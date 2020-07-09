package net.javapla.jawn.core.internal;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.util.Constants;

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
    
    static ConfigImpl parse(final Map<String, String> properties) {
        Properties p = PropertiesLoader.parseMap(properties);
        return new ConfigImpl(p);
    }
    
    static ConfigImpl parse(final Modes mode, final Map<String, String> properties) {
        Properties p = PropertiesLoader.parseMap(properties);
        return new ConfigImpl(mode, p);
    }
    
    static ConfigImpl parse(final Modes mode, final String file) {
        return parse(mode, file, ParseOptions.defaults());
    }
    
    static ConfigImpl parse(final Modes mode, final String file, final ParseOptions options) {
        Properties properties = PropertiesLoader.parseResource(file, options);
        return new ConfigImpl(mode, properties);
    }
    
    static ConfigImpl parseAll(final Modes mode, final String file) {
        Properties properties = PropertiesLoader.parseMultipleResources(file, ParseOptions.defaults());
        return new ConfigImpl(mode, properties);
    }
    
    static ConfigImpl framework(final Modes mode) {
        return parseAll(mode, Constants.PROPERTIES_FILE_FRAMEWORK);
    }
    
    static ConfigImpl user(final Modes mode) {
        return parse(mode, Constants.PROPERTIES_FILE_USER);
    }
    
    static ConfigImpl empty() {
        return new ConfigImpl(new Properties());
    }
    
    static ConfigImpl empty(final Modes mode) {
        return new ConfigImpl(mode, new Properties());
    }
    
    /**
     * Returns a new {@link ConfigImpl}, which overrides the properties of <b>this</b> with those of <b>that</b>
     * @param that
     */
    Config merge(final ConfigImpl that) {
        Properties thisCloned = (Properties) this.props.clone();
        //that.props.entrySet().parallelStream().forEach(e -> thisCloned.setProperty(e.getKey().toString(), e.getValue().toString()));
        return new ConfigImpl(that.getMode(), PropertiesLoader.merge(that.props, thisCloned));//thisCloned);
    }

    @Override
    public Modes getMode() {
        return mode;
    }
    
    @Override
    public String get(final String name) {
        return props.containsKey(modeLowerCased + name) ? props.getProperty(modeLowerCased + name) : props.getProperty(name);
    }
    
    @Override
    public Set<String> keys() {
        return props.stringPropertyNames().stream().map(s -> s.startsWith(modeLowerCased) ? s.substring(modeLowerCased.length()) : s ).collect(Collectors.toSet());
    }
    
    /*@Override
    public Config set(String name, String value) {
        props.put(name, value);
        props.put(modeLowerCased + name, value);
        return this;
    }*/
    
    /*private Config setMode(Modes mode) {
        this.mode = mode;
        lowerCase();
        return this;
    }*/
    
    /*@Override
    public Config set(String name, String value) {
        props.put(name, value);
        props.put(modeLowerCased + name, value);
        return this;
    }*/
    
    /*@Override
    public Set<Entry<String, String>> entrySet() {
        Function<Map.Entry<Object, Object>,Map.Entry<String,String>> convert = 
            e -> new AbstractMap.SimpleImmutableEntry<String,String>(String.valueOf(e.getKey()),String.valueOf(e.getValue()));
        
        return props.entrySet().parallelStream().map(convert).collect(Collectors.toSet());
    }*/
    
    @Override
    public String toString() {
        return props.toString();
    }
}
