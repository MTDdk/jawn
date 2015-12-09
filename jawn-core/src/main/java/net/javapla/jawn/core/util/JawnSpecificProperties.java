package net.javapla.jawn.core.util;

import net.javapla.jawn.core.PropertiesImpl;

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
 * Just make sure that this class is never read BEFORE {@link PropertiesImpl}
 * 
 * @author MTD
 *
 */
public interface JawnSpecificProperties {
    
    public static final String CONTROLLER_PACKAGE = System.getProperty(Constants.JAWN_APPLICATION_BASE_PACKAGE) + '.' + Constants.APPLICATION_CONTROLLER_PACKAGE;
    public static final String CONFIG_PACKAGE = System.getProperty(Constants.JAWN_APPLICATION_BASE_PACKAGE) + '.' + Constants.APPLICATION_CONFIGURATION_PACKAGE;
}
