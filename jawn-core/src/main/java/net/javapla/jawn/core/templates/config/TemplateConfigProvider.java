package net.javapla.jawn.core.templates.config;

import java.util.Iterator;
import java.util.Set;

import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.reflection.ClassLocator;
import net.javapla.jawn.core.reflection.DynamicClassFactory;
import net.javapla.jawn.core.util.PropertiesConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

/**
 * 
 * @author MTD
 * @param <T> 
 *      Type of the configuration exposed to the user 
 *      and then used by the template renderer to configure it.
 */
public class TemplateConfigProvider<T> implements Provider<TemplateConfig<T>> {

    private static Logger log = LoggerFactory.getLogger(TemplateConfigProvider.class.getName());

    public TemplateConfigProvider() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TemplateConfig<T> locateTemplateConfig() {
        log.debug("????? Trying to find the user specified configuration");
        
        try {
            Set<Class<? extends TemplateConfig>> set = new ClassLocator(PropertiesConstants.CONFIG_PACKAGE).subtypeOf(TemplateConfig.class);
            Iterator<Class<? extends TemplateConfig>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Class<? extends TemplateConfig> template = iterator.next();
                try {
                    return DynamicClassFactory.createInstance(template);
                } catch (ClassLoadException ignore) {}
            }
        } catch (IllegalArgumentException ignoreAndContinue) {}
        
        log.warn("Failed to find implementation of '" + TemplateConfig.class + "', proceeding without custom configuration of '"
                + TemplateConfig.class.getSimpleName() + "'");
        return null;
    }

    @Override
    public TemplateConfig<T> get() {
        return locateTemplateConfig();
    }

}
