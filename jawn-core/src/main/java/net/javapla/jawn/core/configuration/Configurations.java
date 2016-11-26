package net.javapla.jawn.core.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.javapla.jawn.core.util.Constants;

public interface Configurations {

    Set<String> PROPERTY_PARAMS = 
		new HashSet<>(
		    Arrays.asList(
		        Constants.PROPERTY_APPLICATION_BASE_PACKAGE,
		        Constants.PROPERTY_APPLICATION_PLUGINS_PACKAGE,
		        Constants.PROPERTY_UPLOADS_MAX_SIZE,
		        Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH
		    ));

	void set(String name, String value);

    
}
