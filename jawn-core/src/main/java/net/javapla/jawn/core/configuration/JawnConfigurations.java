package net.javapla.jawn.core.configuration;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.exceptions.InitException;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.StringUtil;

/**
 * Read configurations
 * 
 * @author MTD
 */
public class JawnConfigurations implements Configurations {
    
    private static final Logger logger = LoggerFactory.getLogger(JawnConfigurations.class);
    
    private final Properties props;
    private final Modes mode;
    private final String modeLowerCased;
    
    
    public JawnConfigurations(Modes mode) throws InitException {
        this.mode = mode;
        this.modeLowerCased = mode.name().toLowerCase() + '.';
        this.props = new Properties();
        
        readProperties();
        
        String basePackage = props.getProperty(Constants.PROPERTY_APPLICATION_BASE_PACKAGE, Constants.APPLICATION_STANDARD_PACKAGE);
        System.setProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE, basePackage);
        
        ConfigurationsHelper.check(this);
    }
    
    protected void readProperties() throws InitException {
        try {
            
            //read defaults
            try (InputStream in1 = JawnConfigurations.class.getClassLoader().getResourceAsStream(Constants.PROPERTIES_FILE_DEFAULT)){
                props.load(in1);
            }

            //override defaults
            try (InputStream in2 = JawnConfigurations.class.getClassLoader().getResourceAsStream(Constants.PROPERTIES_FILE_USER)) {
                if(in2 != null){
                    Properties overrides = new Properties();
                    overrides.load(in2);
                    for (Object name : overrides.keySet()) {
                        props.put(name, overrides.get(name));
                    }
                }
            }

            checkInitProperties();
            
        } catch (InitException e ) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitException(e);
        }
    }
    
    
    private void checkInitProperties() throws InitException {
        for (String param: PROPERTY_PARAMS) {
            if (getObject(param) == null) {
                throw new InitException("Must provide property: " + param);
            }
        }
    }
    
    public Modes getMode() {
        return mode;
    }
    
    public boolean isProd() {
        return mode == Modes.PROD;
    }
    public boolean isTest() {
        return mode == Modes.TEST;
    }
    public boolean isDev() {
        return mode == Modes.DEV;
    }
    /*public void set(Modes mode) {
        this.mode = mode;
    }*/
    
    
    /**
     * Bind all the properties read into the Named("key")
     * @param binder
     */
    /*public void bindProperties(Binder binder) {
        Names.bindProperties(binder, props);
    }*/
    
    
    public String get(String name) {
        return _getString(name);
    }
    
    public Optional<String> getSecure(String name) {
        return Optional.ofNullable(_getString(name));
    }
    
    public String getOrDie(String name) {
        //if (props.containsKey(name)) return get(name);
        String s = _getString(name);
        if (s != null) return s;
        
        String KEY_NOT_FOUND = "Key %s does not exist. Please include it in your " + Constants.PROPERTIES_FILE_USER + ". Otherwise this app will not work";
        logger.error(String.format(KEY_NOT_FOUND, name));
        throw new RuntimeException(String.format(KEY_NOT_FOUND, name));
    }
    
    public String[] getStringArray(String name) {
        String prop = _getString(name);
        if (prop == null) return null;
        String[] arr = StringUtil.split(prop, ',');
        return arr;
    }
    
    public int getInt(String name) {
        return Integer.parseInt(_getString(name));
    }
    
    /**
     * Safely read and parses a boolean from properties.
     * If the value is not set, it defaults to <code>false</code>
     * @param name the property containing the boolean representation to be parsed
     * @return If the value is not set, it defaults to <code>false</code>
     */
    public boolean getBoolean(String name) {
        return Boolean.parseBoolean(_getString(name));
    }
    public Optional<Boolean> getBooleanSecure(String name) {
        return _contains(name) ? Optional.of(getBoolean(name)) : Optional.empty();
    }
    
    
    @Override
    public void set(String name, String value) {
        props.setProperty(name, value);
    }
    
    void set(String name, Object value) {
        props.put(name, value);
    }
    public Object getObject(String name) {
        return _getObject(name);
    }
    
    
    public void setSupportedLanguages(String[] languages) {
        if (languages != null)
            set(Constants.SUPPORTED_LANGUAGES, languages);
    }
    /**
     * First element is the default language
     * @return
     */
    public String[] getSupportedLanguages() {
        return get(Constants.SUPPORTED_LANGUAGES, String[].class);
    }
    
    /**
     * Retrieves object by name. Convenience generic method. 
     *
     * @param name name of object
     * @param type type requested.
     * @return object by name
     */
    public <T>  T get(String name, Class<T> type){
        Object o = _getObject(name);
        return o == null ? null : type.cast(o);
    }
    
    private boolean _contains(String name) {
        return props.containsKey(modeLowerCased + name) || props.containsKey(name);
    }
    
    private String _getString(String name) {
        return props.containsKey(modeLowerCased + name) ? props.getProperty(modeLowerCased + name) : props.getProperty(name);
    }
    
    private Object _getObject(String name) {
        return props.containsKey(modeLowerCased + name) ? props.get(modeLowerCased + name) : props.get(name);
    }
}
