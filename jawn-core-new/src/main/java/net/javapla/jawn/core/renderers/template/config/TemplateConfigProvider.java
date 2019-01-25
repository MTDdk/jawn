package net.javapla.jawn.core.renderers.template.config;

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

    private TemplateConfig<T> locateTemplateConfig() {
        log.debug("????? Trying to find the user specified configuration");
        
        //TODO
        /*try {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Set<Class<? extends TemplateConfig>> set = new ClassLocator(PropertiesConstants.CONFIG_PACKAGE).subtypeOf(TemplateConfig.class);
            Iterator<Class<? extends TemplateConfig>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Class<? extends TemplateConfig> template = iterator.next();
                try {
                    return DynamicClassFactory.createInstance(template);
                } catch (ClassLoadException ignore) {}
            }
        } catch (IllegalArgumentException ignoreAndContinue) {}*/
        
        log.warn("Failed to find implementation of '" + TemplateConfig.class + "', proceeding without custom configuration of '"
                + TemplateConfig.class.getSimpleName() + "'");
        return null;
    }

    @Override
    public TemplateConfig<T> get() {
        return locateTemplateConfig();
    }

}
