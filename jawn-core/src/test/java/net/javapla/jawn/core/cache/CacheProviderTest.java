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
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.database.DatabaseConnections;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.routes.RouterImpl;
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
        
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap(configurations, new DeploymentInfo(configurations));
        bootBootstrap(bootstrap);
        
        Cache cache = bootstrap.getInjector().getInstance(Cache.class);
        assertThat(cache, instanceOf(ExpiringMapCache.class));
    }
    
    @Test
    public void configuredImplementation() {
        configurations.set(Constants.PROPERTY_CACHE_IMPLEMENTATION, CacheImpl.class.getName());
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap(configurations, new DeploymentInfo(configurations));
        bootBootstrap(bootstrap);
        
        Cache cache = bootstrap.getInjector().getInstance(Cache.class);
        assertThat(cache, instanceOf(CacheImpl.class));
    }
    
    @Test
    public void missingImplementation_should_default() {
        configurations.set(Constants.PROPERTY_CACHE_IMPLEMENTATION, "nothing here");

        FrameworkBootstrap bootstrap = new FrameworkBootstrap(configurations, new DeploymentInfo(configurations));
        bootBootstrap(bootstrap);
        
        Cache cache = bootstrap.getInjector().getInstance(Cache.class);
        assertThat(cache, instanceOf(ExpiringMapCache.class));
    }
    
    @Test
    public void defaultExpiration() {
        assertThat(configurations.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION), not(emptyOrNullString()));
        assertThat(configurations.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION), is(equalTo("10m")));
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap(configurations, new DeploymentInfo(configurations));
        bootBootstrap(bootstrap);
        
        Cache cache = bootstrap.getInjector().getInstance(Cache.class);
        assertEquals(cache.getDefaultCacheExpiration(), TimeUtil.parse(configurations.get(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION)));
    }
    
    @Test
    public void configuredExpiration() {
        configurations.set(Constants.PROPERTY_CACHE_DEFAULT_EXPIRATION, "1s");
        
        FrameworkBootstrap bootstrap = new FrameworkBootstrap(configurations, new DeploymentInfo(configurations));
        bootBootstrap(bootstrap);
        
        Cache cache = bootstrap.getInjector().getInstance(Cache.class);
        assertEquals(cache.getDefaultCacheExpiration(), 1);
    }
    
    private void bootBootstrap(FrameworkBootstrap bootstrap) {
        RouterImpl router = mock(RouterImpl.class);
        router.compileRoutes(mock(ActionInvoker.class));
        bootstrap.boot(router, mock(DatabaseConnections.class));
    }

}
