package net.javapla.jawn.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Constants {

    /**
     * The package controllers should be placed in relative to base package
     * defined in the property file
     */
    public static final String APPLICATION_CONTROLLER_PACKAGE = "controllers";//"app.controllers";
    public static final String APPLICATION_CONFIGURATION_PACKAGE = "config";
    
    
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
     * Name of the framework (used in response headers)
     */
    public static final String FRAMEWORK_NAME = "java-web-planet / jawn";
    
    public static final String SESSION_SUFFIX = "_SESSION";
    
    
/* ************* 
 *    Modes
 * ************* */
    public static final String MODE_PRODUCTION  = "production";
    public static final String MODE_DEVELOPMENT = "development";
    public static final String MODE_TEST        = "test";

    
/* ************** 
 *   Properties
 * ************** */
    public static final String PROPERTIES_FILE_DEFAULT = "jawn_defaults.properties"; // Standard in the resources
    public static final String PROPERTIES_FILE_USER = "jawn.properties"; // Overrides standard 
    
    public static final String PROPERTY_APPLICATION_BASE_PACKAGE = "application.base_package";
    public static final String PROPERTY_APPLICATION_PLUGINS_PACKAGE = "application.plugins_package";
    
    public static final String PROPERTY_CACHE_IMPLEMENTATION = "cache.implementation";
    public static final String PROPERTY_CACHE_DEFAULT_EXPIRATION = "cache.default_expiration";
    public static final String PROPERTY_UPLOADS_MAX_SIZE = "uploads.max_file_size";
    
    /**
     * Used to set a single System property to be used immediately after reading the property files.
     * This might need to be this way, as the System properties *can* have some of the same keys.
     * So always prepend with something application specific.
     */
    static final String SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE = "jawn." + PROPERTY_APPLICATION_BASE_PACKAGE;
    
    public static final Set<String> PROPERTY_PARAMS = 
        new HashSet<>(
            Arrays.asList(
                PROPERTY_APPLICATION_BASE_PACKAGE,
                PROPERTY_APPLICATION_PLUGINS_PACKAGE,
                PROPERTY_UPLOADS_MAX_SIZE
            ));
}
