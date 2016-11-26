package net.javapla.jawn.core.cache;

import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.exceptions.ClassLoadException;
import net.javapla.jawn.core.exceptions.CompilationException;
import net.javapla.jawn.core.reflection.DynamicClassFactory;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;
import net.javapla.jawn.core.util.TimeUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CacheProvider implements Provider<Cache> {

    private final Cache cache;
    
    @Inject
    public CacheProvider(JawnConfigurations properties) {
        String cacheClassName = properties.get(Constants.PROPERTY_CACHE_IMPLEMENTATION);
        Cache foundCache = null;
        
        if (!StringUtil.blank(cacheClassName)) {
            try {
                foundCache = DynamicClassFactory.createInstance(cacheClassName, Cache.class, false);
            } catch (CompilationException | ClassLoadException e) {
                System.out.println(Constants.PROPERTY_CACHE_IMPLEMENTATION + " could not be read from properties :: " + cacheClassName);  
            }
        }
        
        if (foundCache == null)
            cache = new ExpiringMapCache();
        else
            cache = foundCache;
        
        String expiration = properties.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION);
        if (!StringUtil.blank(expiration)) {
            cache.setDefaultCacheExpiration(TimeUtil.parse(expiration));
        }
    }
    
    @Override
    public Cache get() {
        return cache;
    }

}
