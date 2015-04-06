package net.javapla.jawn;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.javapla.jawn.db.ConnectionSpec;
import net.javapla.jawn.db.JdbcConnectionSpec;
import net.javapla.jawn.exceptions.InitException;
import net.javapla.jawn.util.Constants;
import net.javapla.jawn.util.StringUtil;

import com.google.inject.Binder;
import com.google.inject.name.Names;

/**
 * Read configurations
 * 
 * @author MTD
 */
public class PropertiesImpl {
    
    private final Properties props;
    
    private final Modes mode;
    
    private final Map<Modes, ConnectionSpec<JdbcConnectionSpec>> databaseSpecs;
    
    
    public PropertiesImpl(Modes mode) {
        this.mode = mode;
        databaseSpecs = new HashMap<>();
        
        try {
            
            //read defaults
            props = new Properties();
            InputStream in1 = PropertiesImpl.class.getClassLoader().getResourceAsStream(Constants.PROPERTIES_FILE);
            props.load(in1);

            //override defaults
            Properties overrides = new Properties();
            InputStream in2 = PropertiesImpl.class.getClassLoader().getResourceAsStream(Constants.USER_PROPERTIES_FILE);
            if(in2 != null){
                overrides.load(in2);
            }

            for (Object name : overrides.keySet()) {
                props.put(name, overrides.get(name));
            }
            checkInitProperties();
//            initTemplateManager();
            
            String logRequest = System.getProperty("jawn.log.request");
            props.put(Constants.LOG_REQUESTS, logRequest);
            
        } catch (InitException e ) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitException(e);
        }
    }
    
    
    private void checkInitProperties(){
        for(Constants.Params param: Constants.Params.values()){
            if(props.get(param.toString()) == null){
                throw new InitException("Must provide property: " + param);
            }
        }
    }
    
    public Modes getMode() {
        return mode;
    }
    
    public boolean isProd() {
        return mode.equals(Modes.prod);
    }
    public boolean isDev() {
        return mode.equals(Modes.dev);
    }
    public boolean isTest() {
        return mode.equals(Modes.test);
    }
    
    
    /**
     * Bind all the properties read into the Named("key")
     * @param binder
     */
    public void bindProperties(Binder binder) {
        Names.bindProperties(binder, props);
    }
    
    
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
    
    void set(String name, String value) {
        props.setProperty(name, value);
    }
    
    void set(String name, Object value) {
        props.put(name, value);
    }
    public Object getObject(String name) {
        return props.get(name);
    }
    
    
    public void setSupportedLanguages(String... languages) {
        props.put(Constants.SUPPORTED_LANGUAGES, languages);
    }
    /**
     * First element is the default language
     * @return
     */
    public String[] getSupportedLanguages() {
        return get(Constants.SUPPORTED_LANGUAGES, String[].class);
    }
    
    public ConnectionSpec<JdbcConnectionSpec> getDatabaseSpec() {
        return databaseSpecs.get(getMode());
    }
    public void putDatabaseSpec(Modes mode, ConnectionSpec<JdbcConnectionSpec> spec) {
        databaseSpecs.put(mode, spec);
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
