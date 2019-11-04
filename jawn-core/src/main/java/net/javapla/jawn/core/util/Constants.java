package net.javapla.jawn.core.util;

public interface Constants {

    /**
     * The package controllers should be placed in relative to base package
     * defined in the property file
     */
    String APPLICATION_CONTROLLER_PACKAGE = "controllers";//"app.controllers";
    String APPLICATION_CONFIGURATION_PACKAGE = "config";
    String APPLICATION_STANDARD_PACKAGE = "app";
    
    
    /**
     * Default method name of controllers if nothing else is specified
     */
    String DEFAULT_ACTION_NAME = "index";
    
    /**
     * Default name of a controller if nothing else is specified.
     * Of course still with the extension 'Controller'
     */
    String ROOT_CONTROLLER_NAME = "index";
    
    
    /**
     * The defined languages available in the app
     */
    String SUPPORTED_LANGUAGES = "languages";
    
    /**
     * Name of the framework (used in response headers)
     */
    String FRAMEWORK_NAME = "java-web-planet / jawn";
    
    String SESSION_SUFFIX = "_SESSION";
    String FLASH_SUFFIX = "_FLASH";
    
    
/* ************* 
 *    Modes
 * ************* */
    String MODE_PRODUCTION  = "production";
    String MODE_DEVELOPMENT = "development";
    String MODE_TEST        = "test";

    
/* ************** 
 *   Properties
 * ************** */
    String PROPERTIES_FILE_FRAMEWORK = "jawn_defaults.properties"; // Standard in the resources
    String PROPERTIES_FILE_USER = "jawn.properties"; // Overrides standard 
    
    //String PROPERTY_APPLICATION_BASE_PACKAGE = "application.base.package"; // TODO should be configurable in Jawn#basePackage
    String PROPERTY_APPLICATION_PLUGINS_PACKAGE = "application.plugins.package";
    
    String PROPERTY_CACHE_IMPLEMENTATION = "cache.implementation";
    String PROPERTY_CACHE_DEFAULT_EXPIRATION = "cache.default_expiration";
    String PROPERTY_UPLOADS_MAX_SIZE = "uploads.max_file_size";
    
    String PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH = "deploymentinfo.webapp.path";
    String PROPERTY_DEPLOYMENT_INFO_TEMPLATES_PATH = "deploymentinfo.webapp.templates.path";
    //String PROPERTY_DEPLOYMENT_INFO_CONTEXT_PATH = "deploymentinfo.context.path";
    
    String PROPERTY_SECURITY_SECRET = "security.secret"; // holds the key of 64 characters
    String PROPERTY_SECURITY_COOKIE_ENCRYPTION = "security.cookie.encryption"; // if true, encrypt the cookies
    
    String PROPERTY_COOKIE_PREFIX = "cookie.prefix";
    String PROPERTY_SESSION_COOKIE_ONLY = "session.cookie_only"; // if true the session will store everything in the cookie
    
    /**
     * Used to set a single System property to be used immediately after reading the property files.
     * This might need to be this way, as the System properties *can* have some of the same keys.
     * So always prepend with something application specific.
     */
    //static final String SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE = "jawn." + PROPERTY_APPLICATION_BASE_PACKAGE;
}
