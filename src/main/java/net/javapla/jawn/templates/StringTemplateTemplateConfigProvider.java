package net.javapla.jawn.templates;

import net.javapla.jawn.DynamicClassFactory;
import net.javapla.jawn.PropertiesImpl;
import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.exceptions.CompilationException;
import net.javapla.jawn.templatemanagers.AbstractTemplateConfig;
import net.javapla.jawn.templatemanagers.stringtemplate.AbstractStringTemplateConfig;
import net.javapla.jawn.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class StringTemplateTemplateConfigProvider implements Provider<AbstractStringTemplateConfig> {
    
    private static Logger log = LoggerFactory.getLogger(StringTemplateTemplateConfigProvider.class.getName());
    
    private final PropertiesImpl properties;

    private Class<?> userTemplateConfig;
    private boolean tried;
    
    @Inject
    public StringTemplateTemplateConfigProvider(PropertiesImpl properties) {
        this.properties = properties;
    }

    @Override
    public AbstractStringTemplateConfig get() {
        log.debug("????? Trying to find the user specified configuration");
        
        if (!tried) {
        
     // read template package
        // find implementations of AbstractTemplateConfig and TemplateManager
        // (just takes the first in classpath)(if multiple implementations of TemplateManager exists - find the )
        // the implementation of AbstractTemplateConfig have to be consistent with app.config.TemplateConfig (if such exists)
        // if TemplateManager could not be found - throw (a hissy fit)
        
        userTemplateConfig  = null;
        try {
            userTemplateConfig  = DynamicClassFactory.getCompiledClass(properties.get(Constants.Params.templateConfig.toString()), properties.isProd());
//            Package templatePackage = userTemplateConfig.getSuperclass().getPackage();
            
//            frameworkReflections = new Reflections(templatePackage.getName());
        } catch (CompilationException | ClassLoadException e) {
//            e1.printStackTrace();
            
            // default to standard package-lookup
//            frameworkReflections = new Reflections(properties.get(Constants.Params.templates.toString()));
            return null;
        }
        
        
//        Set<Class<? extends TemplateManager>> manager = frameworkReflections.getSubTypesOf(TemplateManager.class);
//        if (manager.isEmpty()) 
//            //README should it be possible to run the framework without a templatemanager? If one wanted to run it just as a service
//            throw new InitException("Failed to find implementation of "+TemplateManager.class+". Can not continue without");
//        Class<? extends TemplateManager> templateEngine = manager.stream().findFirst().get(); // throws if nothing is found
            
//        @SuppressWarnings("rawtypes")
//        Set<Class<? extends AbstractTemplateConfig>> abstractConfigs = frameworkReflections.getSubTypesOf(AbstractTemplateConfig.class);
        
//        Reflections userReflections = new Reflections(get(Params.templateConfig.toString()));
        
        
//        @SuppressWarnings("unchecked")
//        Class<? extends AbstractTemplateConfig<?>> abstractConfig
//            = (Class<? extends AbstractTemplateConfig<?>>) abstractConfigs.stream().findFirst().orElse(null);
        
        tried = true;
        }
        
        if (userTemplateConfig  == null)
            return null;
        else {
            try {
                return (AbstractStringTemplateConfig) userTemplateConfig.newInstance();
//            } catch (ClassLoadException/*ClassCastException*/ e) {
//                LOGGER.warn("The found implementation of '"+Params.templateConfig.toString()+"' were not a subtype of " + userTemplateConfig + ". Proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
            }catch(Exception e){
                log.warn("Failed to find implementation of '" + userTemplateConfig  + "', proceeding without custom configuration of '"+AbstractTemplateConfig.class.getSimpleName()+"'");
                return null;
            }
        }
        
    }

}
