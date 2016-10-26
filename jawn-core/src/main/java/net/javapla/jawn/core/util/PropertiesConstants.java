package net.javapla.jawn.core.util;

import net.javapla.jawn.core.configuration.JawnConfigurations;

/**
 * These constants cannot be a part of {@link Constants}
 * as Constants are used before reading property files,
 * and the property files may contain useful properties such as 
 * the base package for this application, which is needed for building
 * several constants for the framework.
 * 
 * This means, that this interface will first be read and stored
 * in memory after bootstrapping of the framework, which gives
 * us the advantage that certain read properties can be
 * available to us at runtime.
 * 
 * Just make sure that this class is never read BEFORE {@link JawnConfigurations}
 * 
 * @author MTD
 *
 */
public interface PropertiesConstants {
    
    public static final String CONTROLLER_PACKAGE = 
            System.getProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE) + '.' + Constants.APPLICATION_CONTROLLER_PACKAGE;
    public static final String CONFIG_PACKAGE = 
            System.getProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE) + '.' + Constants.APPLICATION_CONFIGURATION_PACKAGE;
}
