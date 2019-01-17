package net.javapla.jawn.core.cache;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import net.javapla.jawn.core.FrameworkBootstrap;
import net.javapla.jawn.core.api.Filters;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.database.DatabaseConnections;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.routes.RouterImpl;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.TimeUtil;

public class CacheProviderTest {

    private JawnConfigurations configurations;

    @Before
    public void setUp() throws Exception {
        configurations = new JawnConfigurations(Modes.TEST);
    }

    @Test
    public void defaultImplementation() {
        assertThat(configurations.get(Constants.PROPERTY_CACHE_IMPLEMENTATION), not(emptyOrNullString()));
        assertThat(configurations.get(Constants.PROPERTY_CACHE_IMPLEMENTATION), is(equalTo(ExpiringMapCache.class.getName())));
        
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap();
        Cache cache = bootBootstrap(bootstrap);
        
        assertThat(cache, instanceOf(ExpiringMapCache.class));
    }
    
    @Test
    public void configuredImplementation() {
        configurations.set(Constants.PROPERTY_CACHE_IMPLEMENTATION, CacheTestImpl.class.getName());
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap();
        Cache cache = bootBootstrap(bootstrap);
        
        assertThat(cache, instanceOf(CacheTestImpl.class));
    }
    
    @Test
    public void missingImplementation_should_default() {
        configurations.set(Constants.PROPERTY_CACHE_IMPLEMENTATION, "nothing here");

        FrameworkBootstrap bootstrap = new FrameworkBootstrap();
        Cache cache = bootBootstrap(bootstrap);
        
        assertThat(cache, instanceOf(ExpiringMapCache.class));
    }
    
    @Test
    public void defaultExpiration() {
        assertThat(configurations.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION), not(emptyOrNullString()));
        assertThat(configurations.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION), is(equalTo("10m")));
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap();
        Cache cache = bootBootstrap(bootstrap);
        
        assertEquals(cache.getDefaultCacheExpiration(), TimeUtil.parse(configurations.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION)));
    }
    
    @Test
    public void configuredExpiration() {
        configurations.set(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION, "1s");
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap();
        Cache cache = bootBootstrap(bootstrap);
        
        assertEquals(cache.getDefaultCacheExpiration(), 1);
    }
    
    @Test
    public void missingConfigurations_should_default() {
        int TEN_MINUTES = 60 * 10;
        configurations.set(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION, "");
        configurations.set(Constants.PROPERTY_CACHE_IMPLEMENTATION, "");
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap();
        Cache cache = bootBootstrap(bootstrap);
        
        assertThat(cache, instanceOf(ExpiringMapCache.class));
        assertEquals(cache.getDefaultCacheExpiration(), TEN_MINUTES);
    }
    
    private Cache bootBootstrap(FrameworkBootstrap bootstrap) {
        RouterImpl router = mock(RouterImpl.class);
        router.compileRoutes(mock(ActionInvoker.class));
        bootstrap.boot(configurations, mock(Filters.class), router, new ServerConfig(), mock(DatabaseConnections.class));
        
        return bootstrap.getInjector().getInstance(Cache.class);
    }

}
