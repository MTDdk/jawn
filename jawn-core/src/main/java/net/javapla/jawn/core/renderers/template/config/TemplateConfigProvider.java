package net.javapla.jawn.core.renderers.template.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * 
 * @author MTD
 * @param <T> 
 *      Type of the configuration exposed to the user 
 *      and then used by the template renderer to configure it.
 */
public class TemplateConfigProvider<T> implements Provider<TemplateConfig<T>> {

    private static Logger log = LoggerFactory.getLogger(TemplateConfigProvider.class.getName());
    
    private final Injector injector;

    @Inject
    public TemplateConfigProvider(Injector injector) {
        this.injector = injector;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TemplateConfig<T> get() {
        log.debug("????? Trying to find the user specified configuration");
        
        for (Key<?> key : injector.getBindings().keySet()) {
            if (key.getTypeLiteral().getRawType().equals(TemplateConfig.class)) {
                // This is of course not quite the correct way of doing it,
                // if we want to support multiple TemplateConfigs
                
                TypeLiteral<?> literal = key.getTypeLiteral();
                Type type = literal.getType();
                if (type instanceof ParameterizedType) {
                    Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                    if (types != null) {
                        // types[0] needs to be the same as the actual value for T
                    }
                }
                
                return (TemplateConfig<T>) injector.getBinding(key).getProvider().get();
            }
        }
        
        return null;
    }

}
