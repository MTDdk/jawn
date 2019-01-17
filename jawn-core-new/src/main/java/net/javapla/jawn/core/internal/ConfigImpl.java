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

class ConfigImpl implements Config {
    
    private final Properties props;
    
    private Modes mode;
    private String modeLowerCased;

    private ConfigImpl(final Properties props) {
        this.props = props;
        setMode(Modes.DEV);
    }
    
    private Config lowerCase() {
        modeLowerCased = mode.name().toLowerCase() + '.';
        return this;
    }
    
    static Config parse(final Modes mode, final String file) {
        Properties properties = PropertiesLoader.parseResourse(file, ParseOptions.defaults());
        return new ConfigImpl(properties).setMode(mode);
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
        return new ConfigImpl(new Properties()).setMode(mode);
    }
    
    @Override
    public Modes getMode() {
        return mode;
    }
    
    Config setMode(Modes mode) {
        this.mode = mode;
        lowerCase();
        return this;
    }
    
    @Override
    public String get(final String name) {
        return props.containsKey(modeLowerCased + name) ? props.getProperty(modeLowerCased + name) : props.getProperty(name);
    }
    
    @Override
    public Config set(String name, String value) {
        props.put(name, value);
        props.put(modeLowerCased + name, value);
        return this;
    }
    
    /**
     * Overrides the properties of <b>this</b> with those of <b>that</b>
     * @param that
     */
    void merge(final Config that) {
        that.entrySet().parallelStream().forEach(e -> props.setProperty(e.getKey(), e.getValue()));
        setMode(that.getMode());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Function<Map.Entry<Object, Object>,Map.Entry<String,String>> convert = 
            e -> new AbstractMap.SimpleImmutableEntry<String,String>(String.valueOf(e.getKey()),String.valueOf(e.getValue()));
        
        return props.entrySet().parallelStream().map(convert).collect(Collectors.toSet());
    }
    
    /*private boolean _contains(String name) {
        return props.containsKey(modeLowerCased + name) || props.containsKey(name);
    }*/
}
