package net.javapla.jawn.core.configuration;

import java.io.InputStream;
import java.util.Properties;

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
    
    private final Properties props;
    
    private Modes mode;
    
    
    public JawnConfigurations(Modes mode) throws InitException {
        this.mode = mode;
        this.props = new Properties();
        
        readProperties();
        
        String basePackage = props.getProperty(Constants.PROPERTY_APPLICATION_BASE_PACKAGE);
        System.setProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE, basePackage);
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
            if (props.get(param) == null) {
                throw new InitException("Must provide property: " + param);
            }
        }
    }
    
    public Modes getMode() {
        return mode;
    }
    
    public boolean isProd() {
        return mode.equals(Modes.PROD);
    }
    public boolean isTest() {
        return mode.equals(Modes.TEST);
    }
    public boolean isDev() {
        return mode.equals(Modes.DEV);
    }
    public void set(Modes mode) {
        this.mode = mode;
    }
    
    
    /**
     * Bind all the properties read into the Named("key")
     * @param binder
     */
    /*public void bindProperties(Binder binder) {
        Names.bindProperties(binder, props);
    }*/
    
    
    public String get(String name) {
        return props.getProperty(name);
    }
    public String[] getStringArray(String name) {
        String prop = props.getProperty(name);
        if (prop == null) return null;
        String[] arr = StringUtil.split(prop, ',');
        return arr;
    }
    public int getInt(String name) {
        return Integer.parseInt(get(name));
    }
    public boolean getBoolean(String name) {
        return Boolean.parseBoolean(get(name));
    }
    
    @Override
    public void set(String name, String value) {
        props.setProperty(name, value);
    }
    
    void set(String name, Object value) {
        props.put(name, value);
    }
    public Object getObject(String name) {
        return props.get(name);
    }
    
    
    public void setSupportedLanguages(String[] languages) {
        if (languages != null)
            props.put(Constants.SUPPORTED_LANGUAGES, languages);
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
        Object o = props.get(name);
        return o == null? null : type.cast(o);
    }
}
