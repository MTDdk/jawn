package net.javapla.jawn.core.templates.config;

import java.util.Iterator;
import java.util.Set;

import org.reflections.Reflections;
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

//    private final PropertiesImpl properties;

//    private Class<?> userTemplateConfig;

//    @Inject
    public TemplateConfigProvider(/*PropertiesImpl properties*/) {
//        this.properties = properties;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public TemplateConfig<T> get() {
        log.debug("????? Trying to find the user specified configuration");

        Reflections reflections = new Reflections("app.config");
        Set<Class<? extends TemplateConfig>> set = reflections.getSubTypesOf(TemplateConfig.class);
        Iterator<Class<? extends TemplateConfig>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Class<? extends TemplateConfig> template = iterator.next();
            try {
                return (TemplateConfig<T>) template.newInstance();
            } catch (InstantiationException | IllegalAccessException ignore) {}
        }
        log.warn("Failed to find implementation of '" + TemplateConfig.class + "', proceeding without custom configuration of '"
                + TemplateConfig.class.getSimpleName() + "'");
        return null;

        // read template package
        // find implementations of AbstractTemplateConfig and TemplateManager
        // (just takes the first in classpath)(if multiple implementations of
        // TemplateManager exists - find the )
        // the implementation of AbstractTemplateConfig have to be consistent
        // with app.config.TemplateConfig (if such exists)
        // if TemplateManager could not be found - throw (a hissy fit)

        /*userTemplateConfig = null;
        try {
            userTemplateConfig = DynamicClassFactory
                    .getCompiledClass(properties.get(Constants.Params.templateConfig.toString()), properties.isProd());
            // Package templatePackage =
            // userTemplateConfig.getSuperclass().getPackage();

            // frameworkReflections = new
            // Reflections(templatePackage.getName());
        } catch (CompilationException | ClassLoadException e) {
            // e1.printStackTrace();

            // default to standard package-lookup
            // frameworkReflections = new
            // Reflections(properties.get(Constants.Params.templates.toString()));
            return null;
        }

        // Set<Class<? extends TemplateManager>> manager =
        // frameworkReflections.getSubTypesOf(TemplateManager.class);
        // if (manager.isEmpty())
        // //README should it be possible to run the framework without a
        // templatemanager? If one wanted to run it just as a service
        // throw new
        // InitException("Failed to find implementation of "+TemplateManager.class+". Can not continue without");
        // Class<? extends TemplateManager> templateEngine =
        // manager.stream().findFirst().get(); // throws if nothing is found

        // @SuppressWarnings("rawtypes")
        // Set<Class<? extends AbstractTemplateConfig>> abstractConfigs =
        // frameworkReflections.getSubTypesOf(AbstractTemplateConfig.class);

        // Reflections userReflections = new
        // Reflections(get(Params.templateConfig.toString()));

        // @SuppressWarnings("unchecked")
        // Class<? extends AbstractTemplateConfig<?>> abstractConfig
        // = (Class<? extends AbstractTemplateConfig<?>>)
        // abstractConfigs.stream().findFirst().orElse(null);

        if (userTemplateConfig == null)
            return null;
        else {
            try {
                return (TemplateConfig<StringTemplateConfiguration>) userTemplateConfig.newInstance();
                // } catch (ClassLoadException e) {
                // LOGGER.warn("The found implementation of '"+Params.templateConfig.toString()+"' were not a subtype of "
                // + userTemplateConfig +
                // ". Proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
            } catch (Exception e) {
                log.warn("Failed to find implementation of '" + userTemplateConfig + "', proceeding without custom configuration of '"
                        + TemplateConfig.class.getSimpleName() + "'");
                return null;
            }
        }*/

    }

}
