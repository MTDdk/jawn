package net.javapla.jawn.core.util;

import java.nio.charset.StandardCharsets;

public interface Constants {

    enum Params {
        /*templateManager, */templates, bootstrap, defaultLayout, targetDir, rootPackage, dbconfig, controllerConfig, rollback,
        templateConfig, route_config, maxUploadSize, imageUploadFolder/*, supportedLanguages*/
    }
    
    /**
     * The package controllers should be placed in
     */
    public static final String CONTROLLER_PACKAGE = "app.controllers";
    
    /**
     * Default method name of controllers if nothing else is specified
     */
    public static final String DEFAULT_ACTION_NAME = "index";
    
    /**
     * Default name of a controller if nothing else is specified.
     * Of course still with the extension 'Controller'
     */
    public static final String ROOT_CONTROLLER_NAME = "index";
    
    /**
     * Standard in the resources
     */
    public static final String PROPERTIES_FILE = "jawn_defaults.properties";
    
    /**
     * Overrides standard 
     * @see {@link Constants#PROPERTIES_FILE}
     */
    public static final String USER_PROPERTIES_FILE = "/jawn.properties";
    
    
    /**
     * Should the framework always be compiling the controllers
     */
    public static final String ACTIVE_RELOAD = "jawn_reload";
    
    
    /**
     * The defined languages available in the app
     */
    public static final String SUPPORTED_LANGUAGES = "languages";
    
    /**
     * The defined encoding used for all responses
     */
    public static final String DEFINED_ENCODING = "encoding";
    
    /**
     * Default framework encoding = UTF-8
     */
    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.displayName();
    
    
    /**
     * Database configurations
     */
    public static final String DATABASE_SPECS = "databaseSpecs";
    
    
/* ************* 
 *    Modes
 * ************* */
    public static final String MODE_PRODUCTION  = "production";
    public static final String MODE_DEVELOPMENT = "development";
    public static final String MODE_TEST        = "test";
}
