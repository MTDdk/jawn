/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.javapla.jawn.db.JdbcDatabaseSpec;
import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.exceptions.CompilationException;
import net.javapla.jawn.exceptions.InitException;
import net.javapla.jawn.templatemanagers.AbstractTemplateConfig;
import net.javapla.jawn.templatemanagers.TemplateManager;
import net.javapla.jawn.util.ConvertUtil;
import net.javapla.jawn.util.StringUtil;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Igor Polevoy
 * @author MTD
 */
@Deprecated
public class Configuration {
    private static Logger LOGGER = LoggerFactory.getLogger(Configuration.class.getName());

    enum Params {
        /*templateManager, */templates, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, controllerConfig, rollback,
        templateConfig, route_config, maxUploadSize, imageUploadFolder/*, supportedLanguages*/
    }

//    private static final Configuration instance = new Configuration();
    private static Properties props = new Properties();
    private static TemplateManager templateManager;
//    private static HashMap<String, List<ConnectionSpecWrapper>> connectionWrappers = new HashMap<String, List<ConnectionSpecWrapper>>();
    private static Map<String, List<JdbcDatabaseSpec>> databaseSpecs = new HashMap<>();
    private static boolean testing = false;
    private static String ENV;
    private static boolean activeReload = !StringUtil.blank(System.getProperty("jawn_reload")) && ConvertUtil.toBoolean(System.getProperty("jawn_reload"));
    private static boolean useDefaultLayoutForErrors = true;
    
    private static AbstractTemplateConfig<?> abstractTemplateConfig;
    
    static {
        try {
            
            //read defaults
            props = new Properties();
            InputStream in1 = Configuration.class.getClassLoader().getResourceAsStream("jawn_defaults.properties");
            props.load(in1);

            //override defaults
            Properties overrides = new Properties();
            InputStream in2 = Configuration.class.getClassLoader().getResourceAsStream("/jawn.properties");
            if(in2 != null){
                overrides.load(in2);
            }

            for (Object name : overrides.keySet()) {
                props.put(name, overrides.get(name));
            }
            checkInitProperties();
            initTemplateManager();
        } catch (InitException e ) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitException(e);
        }
    }

    /**
     * Set to true if you want ActiveWeb to wrap the errors, such as 404, and 500 in a default layout.
     * False will ensure that these pages will render without default layout.
     *
     * <h2>System errors</h2>
     * The following system errors are affected by this setting:
     *
     * <ul>
     *     <li>CompilationException - when there are compilation errors in controller</li>
     *     <li>ClassLoadException - failure to load a controller class for any reason </li>
     *     <li>ActionNotFoundException - action method is not found in controller class</li>
     *     <li>ViewMissingException - corresponding view is missing</li>
     *     <li>ViewException - FreeMarker barfed on the view</li>
     * </ul>
     *
     * If you need custom dynamic layout for error.ftl and 404.ftl, use "@wrap" tag and conditions inside the error templates.
     *
     * <h2>Application level</h2>
     * This method does <em>not</em> affect application errors (exceptions thrown by your code).
     * However, it is typical for an ActiveWeb project to define a top controller filter called CatchAllFilter and process
     * application level exceptions in that filter:
     * <pre>
            package app.controllers;
            import org.javalite.activeweb.controller_filters.HttpSupportFilter;
            import static org.javalite.common.Collections.map;
            public class CatchAllFilter extends HttpSupportFilter {
                public void onException(Exception e) {
                    render("/system/error", map("message", e.getMessage())).layout("error_layout");
                }
            }
     * </pre>
     *
     * This way you have a complete control over how your error messages are displayed.
     *
     * @param useDefaultLayoutForErrors true to use default layout, false no not to use it.
     */
    public static void setUseDefaultLayoutForErrors(boolean useDefaultLayoutForErrors) {
        Configuration.useDefaultLayoutForErrors = useDefaultLayoutForErrors;
    }

    /**
     * True to use default layout for error pages, false not to.
     * @return true to use default layout for error pages, false not to.
     */
    protected static boolean useDefaultLayoutForErrors() {
        return useDefaultLayoutForErrors;
    }

    public static boolean logRequestParams() {
        String logRequest = System.getProperty("jawn.log.request");
        return logRequest != null && logRequest.equals("true");
    }
    
    public static String getEnv(){
        if(ENV == null){
            ENV = System.getProperty("JAWN_ENV") == null? System.getenv().get("JAWN_ENV"): System.getProperty("JAWN_ENV");
            if(StringUtil.blank(ENV)){                
                ENV = "development";
                LOGGER.warn("Environment variable JAWN_ENV not provided, defaulting to '" + ENV + "'");
            }
        }
        return ENV;
    }

    //only for testing!
    protected static void setEnv(String env){
        ENV = env;
    }
    
    public static boolean isTesting() {
        return testing;
    }

    protected static void setTesting(boolean testing) {
        Configuration.testing = testing;
    }


    private static void checkInitProperties(){
        for(Params param: Params.values()){
            if(props.get(param.toString()) == null){
                throw new InitException("Must provide property: " + param);
            }
        }
    }

//    private static void initTemplateManager() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        try{
//            String className = get(Params.templateManager.toString());
//            Class<?> templateClass = Class.forName(className);
//            templateManager = (TemplateManager)templateClass.newInstance();
//        }catch(RuntimeException e){
//            e.printStackTrace();
//            throw e;
//        }catch(Exception e){
//            e.printStackTrace();
//            throw new InitException(e);
//        }
//    }
    private static void initTemplateManager() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // read template package
        // find implementations of AbstractTemplateConfig and TemplateManager
        // (just takes the first in classpath)(if multiple implementations of TemplateManager exists - find the )
        // the implementation of AbstractTemplateConfig have to be consistent with app.config.TemplateConfig (if such exists)
        // if TemplateManager could not be found - throw (a hissy fit)
        
        // locate the user-defined TemplateConfig
        // if it exists, use its super-type to base the templateEngine lookup
        // else, take the first, and therefore the best, TemplateManager
        Reflections frameworkReflections;
        Class<?> userTemplateConfig = null;
        try {
            userTemplateConfig = DynamicClassFactory.getCompiledClass(get(Params.templateConfig.toString()), !activeReload());
            Package templatePackage = userTemplateConfig.getSuperclass().getPackage();
            
            frameworkReflections = new Reflections(templatePackage.getName());
        } catch (CompilationException | ClassLoadException e) {
//            e1.printStackTrace();
            
            // default to standard package-lookup
            frameworkReflections = new Reflections(get(Params.templates.toString()));
        }
        
        
        Set<Class<? extends TemplateManager>> manager = frameworkReflections.getSubTypesOf(TemplateManager.class);
        if (manager.isEmpty()) 
            //README should it be possible to run the framework without a templatemanager? If one wanted to run it just as a service
            throw new InitException("Failed to find implementation of "+TemplateManager.class+". Can not continue without");
        Class<? extends TemplateManager> templateEngine = manager.stream().findFirst().get(); // throws if nothing is found
            
        @SuppressWarnings("rawtypes")
        Set<Class<? extends AbstractTemplateConfig>> abstractConfigs = frameworkReflections.getSubTypesOf(AbstractTemplateConfig.class);
        
//        Reflections userReflections = new Reflections(get(Params.templateConfig.toString()));
        
        
        @SuppressWarnings("unchecked")
        Class<? extends AbstractTemplateConfig<?>> abstractConfig
            = (Class<? extends AbstractTemplateConfig<?>>) abstractConfigs.stream().findFirst().orElse(null);
        
        if (userTemplateConfig == null)
            abstractTemplateConfig = null;
        else {
            try {
            abstractTemplateConfig = (AbstractTemplateConfig<?>) userTemplateConfig.newInstance();
//            } catch (ClassLoadException/*ClassCastException*/ e) {
//                LOGGER.warn("The found implementation of '"+Params.templateConfig.toString()+"' were not a subtype of " + userTemplateConfig + ". Proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
            }catch(Exception e){
                LOGGER.warn("Failed to find implementation of '" + userTemplateConfig + "', proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
            }
        }
            
//        abstractTemplateConfig = initTemplateConfig(abstractConfig); // can be null
        
        try {
            Constructor<? extends TemplateManager> templateEngineConstructor = templateEngine.getConstructor(abstractConfig);
            templateManager = templateEngineConstructor.newInstance(abstractTemplateConfig);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
            templateManager = templateEngine.newInstance();
            // if this throws - so be it - someone has made a mistake.
        }
    }
    /*private static AbstractTemplateConfig<?> initTemplateConfig(Class<? extends AbstractTemplateConfig<?>> templateConfigClass){
        try{
            String className = get(Params.templateConfig.toString());
            AbstractTemplateConfig<?> templateConfig = DynamicClassFactory.createInstance(className, templateConfigClass, !activeReload());
            return templateConfig;
        } catch (ClassLoadExceptionClassCastException e) {
            LOGGER.warn("The found implementation of '"+Params.templateConfig.toString()+"' were not a subtype of " + templateConfigClass + ". Proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
        }catch(Exception e){
            LOGGER.warn("Failed to find implementation of '" + templateConfigClass + "', proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
        }
        return null;
    }*/
    

    public synchronized static AbstractTemplateConfig<?> getTemplateConfig(){
//        if(templateManager == null) throw new InitException("The templatemanager has not yet been initialised");

//        return injector.getBinding(AbstractTemplateConfig.class).toString();
//        return getTemplateConfig(templateManager.getTemplateConfigClass());
        return abstractTemplateConfig;
        /*try{
            String className = get(Params.templateConfig.toString());
            Object instance = Class.forName(className).newInstance();
            instance.getClass().asSubclass(templateManager.getTemplateConfigClass());
            AbstractTemplateConfig<?> templateConfig = templateManager.getTemplateConfigClass().cast(instance);
            
            
            return freeMarkerConfig = (AbstractFreeMarkerConfig)templateConfig;
        }catch(Exception e){
            LOGGER.warn("Failed to find implementation of '" + templateManager.getTemplateConfigClass() + "', proceeding without custom configuration of " + templateManager);
            return null;
        }*/
    }


    public static TemplateManager getTemplateManager(){
        return templateManager;
    }

    public static String get(String name){return props.getProperty(name);}

    public static String getDefaultLayout() {
        return get(Params.defaultLayout.toString());
    }

    public static String getBootstrapClassName() {
        return get(Params.bootstrap.toString());
    }

    public static String getControllerConfigClassName(){
        return get(Params.controllerConfig.toString());
    }

    public static String getDbConfigClassName(){
        return get(Params.dbconfig.toString());
    }

    public static String getRouteConfigClassName(){
        return get(Params.route_config.toString());
    }
    
    public static String getTargetDir() {
        return get(Params.targetDir.toString());
    }

    public static String getRootPackage() {
        return get(Params.rootPackage.toString());  
    }

    public static boolean rollback() {
        return Boolean.parseBoolean(get(Params.rollback.toString().trim()));  
    }
    

    

    public static boolean activeReload(){
        return activeReload;
    }

    public static int getMaxUploadSize() {
        return Integer.parseInt(get(Params.maxUploadSize.toString()));
    }

    public static File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

//    private static List<IgnoreSpec> ignoreSpecs = new ArrayList<IgnoreSpec>();
    
    
    /*
     * MTD section
     */
    
    public static String imageUploadFolder() {
        return get(Params.imageUploadFolder.toString());
    }


    protected static void addDatabaseSpec(String environment, JdbcDatabaseSpec connection) {
        List<JdbcDatabaseSpec> list = databaseSpecs.get(environment);
        if (list == null) {
            list = new ArrayList<>();
            databaseSpecs.put(environment, list);
        }
        list.add(connection);
    }

    /**
     * Provides a list of all database connections corresponding to a current environment.
     *
     * @return  a list of all database connections corresponding to a current environment.
     */
    public static List<JdbcDatabaseSpec> getDatabaseSpecs() {
        return getDatabaseSpecs(getEnv());
    }

    /**
     * Provides a list of all database connections corresponding to a current environment.
     *
     * @param env name of environment, such as "development", "production", etc.
     * 
     * @return  a list of all database connections corresponding to a given environment.
     */
    public static List<JdbcDatabaseSpec> getDatabaseSpecs(String env) {
        return databaseSpecs.getOrDefault(env, new ArrayList<>());
    }
    
    protected static void clearDatabaseSpecs(String env) {
        if (databaseSpecs.containsKey(env)) databaseSpecs.get(env).clear();
    }
}
