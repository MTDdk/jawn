package net.javapla.jawn;

import net.javapla.jawn.i18n.Lang;
import net.javapla.jawn.parsers.JsonMapperProvider;
import net.javapla.jawn.parsers.ParserEngineManager;
import net.javapla.jawn.parsers.ParserEngineManagerImpl;
import net.javapla.jawn.parsers.XmlMapperProvider;
import net.javapla.jawn.templates.TemplateEngineManager;
import net.javapla.jawn.templates.TemplateEngineManagerImpl;
import net.javapla.jawn.templates.stringtemplateconfiguration.ConfigurationReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class CoreModule extends AbstractModule {
    
    private final PropertiesImpl properties;
    private final Router router;
    
    CoreModule(PropertiesImpl properties, Router router) {
        this.properties = properties;
        this.router = router;
    }

    @Override
    protected void configure() {
        bind(PropertiesImpl.class).toInstance(properties);
        bind(Lang.class).in(Singleton.class);
        
        bind(Router.class).toInstance(router);
        
        // Marshallers
        bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
        bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
        
        bind(ConfigurationReader.class).in(Singleton.class);
        
        bind(TemplateEngineManager.class).to(TemplateEngineManagerImpl.class).in(Singleton.class);
        bind(ParserEngineManager.class).to(ParserEngineManagerImpl.class).in(Singleton.class);
        bind(ResponseRunner.class).in(Singleton.class);
        
        bind(Context.class).to(ContextImpl.class);
        
        bind(ControllerActionInvoker.class).in(Singleton.class);
        bind(FilterChainEnd.class).in(Singleton.class);
        
        bind(FrameworkEngine.class).in(Singleton.class);
    }

    
}
