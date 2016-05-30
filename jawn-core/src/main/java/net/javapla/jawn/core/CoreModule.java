package net.javapla.jawn.core;

import net.javapla.jawn.core.api.Router;
import net.javapla.jawn.core.cache.Cache;
import net.javapla.jawn.core.cache.CacheProvider;
import net.javapla.jawn.core.i18n.Lang;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.parsers.ParserEngineManagerImpl;
import net.javapla.jawn.core.parsers.XmlMapperProvider;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.routes.RouterImpl;
import net.javapla.jawn.core.templates.TemplateEngineOrchestrator;
import net.javapla.jawn.core.templates.TemplateEngineOrchestratorImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class CoreModule extends AbstractModule {
    
    
    private final PropertiesImpl properties;
    private final RouterImpl router;
    CoreModule(PropertiesImpl properties, RouterImpl router) {
        this.properties = properties;
        this.router = router;
    }

    @Override
    protected void configure() {
        bind(PropertiesImpl.class).toInstance(properties);
        
        // Marshallers
        bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
        bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
        
        bind(Cache.class).toProvider(CacheProvider.class).in(Singleton.class);
        
        bind(TemplateEngineOrchestrator.class).to(TemplateEngineOrchestratorImpl.class).in(Singleton.class);
        bind(ParserEngineManager.class).to(ParserEngineManagerImpl.class).in(Singleton.class);
        bind(ResponseRunner.class).in(Singleton.class);
        
        bind(ActionInvoker.class).in(Singleton.class);
        //bind(FilterChain.class).to(InvokerFilterChainEnd.class);
        
        // initiate all read properties as something injectable
        //properties.bindProperties(binder());
        bind(FrameworkEngine.class).in(Singleton.class);
        
        bind(Router.class).toInstance(router);
        bind(Lang.class).in(Singleton.class);
    }

    
}
